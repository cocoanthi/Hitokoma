package jp.pinolab.hitokoma.feature.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.pinolab.hitokoma.core.file.LocalImageStorage
import jp.pinolab.hitokoma.core.time.firstOfMonth
import jp.pinolab.hitokoma.core.time.nextMonth
import jp.pinolab.hitokoma.core.time.previousMonth
import jp.pinolab.hitokoma.core.time.todayFlow
import jp.pinolab.hitokoma.domain.model.DailyPhoto
import jp.pinolab.hitokoma.domain.repository.MonthlyVideoRepository
import jp.pinolab.hitokoma.feature.gallery.domain.DeleteDailyPhotoUseCase
import jp.pinolab.hitokoma.feature.gallery.domain.ObserveAllPhotosUseCase
import jp.pinolab.hitokoma.feature.monthlyvideo.domain.CreateMonthlyVideoUseCase
import jp.pinolab.hitokoma.feature.selector.domain.SaveDailyPhotoUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoCalendarViewModel(
    observeAllPhotosUseCase: ObserveAllPhotosUseCase,
    private val deleteDailyPhotoUseCase: DeleteDailyPhotoUseCase,
    monthlyVideoRepository: MonthlyVideoRepository,
    // 以下はデバッグモード（写真の追加・動画の手動作成）用
    private val saveDailyPhotoUseCase: SaveDailyPhotoUseCase,
    private val createMonthlyVideoUseCase: CreateMonthlyVideoUseCase,
    private val imageStorage: LocalImageStorage
) : ViewModel() {

    private val initialToday = Clock.System.todayIn(TimeZone.currentSystemDefault())

    // 表示中の月（その月の1日）。初期値は今月
    private val displayedMonth = MutableStateFlow(initialToday.firstOfMonth())

    // シート・ダイアログ表示など、画面操作による状態
    private val dialogState = MutableStateFlow(DialogState())

    // DB の変更（新規登録・上書き・削除）・日付の切り替わり・動画の生成完了がそのままカレンダーに反映される
    val uiState: StateFlow<PhotoCalendarUiState> = combine(
        observeAllPhotosUseCase(),
        todayFlow(),
        displayedMonth,
        dialogState,
        displayedMonth.flatMapLatest { monthlyVideoRepository.observeVideoPath(it) }
    ) { photos, today, month, dialog, videoPath ->
        PhotoCalendarUiState(
            displayedMonth = month,
            today = today,
            photosByDate = photos.associateBy { it.date },
            isLoading = false,
            selectedPhoto = dialog.selectedPhoto,
            photoPendingDelete = dialog.photoPendingDelete,
            errorMessage = dialog.errorMessage,
            videoPath = videoPath,
            isVideoPlaying = dialog.isVideoPlaying && videoPath != null,
            debugAddPhoto = dialog.debugAddPhoto,
            isCreatingVideo = dialog.isCreatingVideo
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PhotoCalendarUiState(
            displayedMonth = displayedMonth.value,
            today = initialToday,
            isLoading = true
        )
    )

    /**
     * ヘッダーの「‹」が押されたとき
     */
    fun onPreviousMonth() {
        displayedMonth.update { it.previousMonth() }
    }

    /**
     * ヘッダーの「›」が押されたとき（未来の月へは進めない）
     */
    fun onNextMonth() {
        if (!uiState.value.canGoNextMonth) return
        displayedMonth.update { it.nextMonth() }
    }

    /**
     * 「◯月のストーリーを見る」が押されたとき
     */
    fun onPlayVideo() {
        dialogState.update { it.copy(isVideoPlaying = true) }
    }

    /**
     * ストーリー動画のプレイヤーを閉じたとき
     */
    fun onDismissVideo() {
        dialogState.update { it.copy(isVideoPlaying = false) }
    }

    /**
     * 通知から起動したとき（その月を表示して動画を再生する）
     */
    fun openVideo(month: LocalDate) {
        displayedMonth.value = month.firstOfMonth()
        dialogState.update { it.copy(selectedPhoto = null, isVideoPlaying = true) }
    }

    /**
     * 写真のある日付がタップされたとき（詳細シートを表示）
     */
    fun onDayClicked(photo: DailyPhoto) {
        dialogState.update { it.copy(selectedPhoto = photo) }
    }

    /**
     * 詳細シートを閉じたとき
     */
    fun onDismissDetail() {
        dialogState.update { it.copy(selectedPhoto = null) }
    }

    /**
     * 詳細シートの削除ボタンが押されたとき（確認ダイアログを表示）
     */
    fun onDeleteClicked(photo: DailyPhoto) {
        dialogState.update { it.copy(photoPendingDelete = photo) }
    }

    /**
     * 削除確認ダイアログをキャンセルしたとき
     */
    fun onDismissDeleteDialog() {
        dialogState.update { it.copy(photoPendingDelete = null) }
    }

    /**
     * 削除確認ダイアログで「削除」を選択したとき（詳細シートも閉じる）
     */
    fun onConfirmDelete() {
        val photo = dialogState.value.photoPendingDelete ?: return
        dialogState.update { it.copy(photoPendingDelete = null, selectedPhoto = null) }

        viewModelScope.launch {
            deleteDailyPhotoUseCase(photo).onFailure {
                dialogState.update { it.copy(errorMessage = "写真の削除に失敗しました。") }
            }
        }
    }

    /**
     * エラーメッセージを閉じたとき
     */
    fun onErrorDismissed() {
        dialogState.update { it.copy(errorMessage = null) }
    }

    /**
     * デバッグ用: 写真のない日付がタップされたとき（写真追加シートを表示）
     */
    fun onDebugEmptyDayClicked(date: LocalDate) {
        dialogState.update { it.copy(debugAddPhoto = DebugAddPhotoState(date = date)) }
    }

    /**
     * デバッグ用: 写真追加シートで画像が選択されたとき（内部ストレージへ保存してプレビューする）
     */
    fun onDebugImagePicked(bytes: ByteArray) {
        viewModelScope.launch {
            val fileName = "photo_${Clock.System.now().toEpochMilliseconds()}.jpg"
            val savedPath = runCatching { imageStorage.saveImage(bytes, fileName) }.getOrElse {
                dialogState.update { it.copy(errorMessage = "画像の保存に失敗しました。") }
                return@launch
            }

            // 選び直した場合は、前に選んだ未登録の画像を消す
            val previousPath = dialogState.value.debugAddPhoto?.imagePath
            dialogState.update { state ->
                state.copy(debugAddPhoto = state.debugAddPhoto?.copy(imagePath = savedPath))
            }
            previousPath?.let { imageStorage.deleteImage(it) }
        }
    }

    /**
     * デバッグ用: 写真追加シートのコメントが変更されたとき
     */
    fun onDebugCommentChanged(comment: String) {
        dialogState.update { state ->
            state.copy(debugAddPhoto = state.debugAddPhoto?.copy(comment = comment))
        }
    }

    /**
     * デバッグ用: 写真追加シートの「登録」が押されたとき
     */
    fun onDebugSaveClicked() {
        val addPhoto = dialogState.value.debugAddPhoto ?: return
        val imagePath = addPhoto.imagePath ?: return
        dialogState.update { it.copy(debugAddPhoto = addPhoto.copy(isSaving = true)) }

        viewModelScope.launch {
            val photo = DailyPhoto(
                date = addPhoto.date,
                imagePath = imagePath,
                comment = addPhoto.comment,
                createdAtEpochMillis = Clock.System.now().toEpochMilliseconds()
            )
            saveDailyPhotoUseCase(photo).fold(
                onSuccess = {
                    // 登録した写真はカレンダーにそのまま反映される
                    dialogState.update { it.copy(debugAddPhoto = null) }
                },
                onFailure = {
                    dialogState.update { state ->
                        state.copy(
                            debugAddPhoto = state.debugAddPhoto?.copy(isSaving = false),
                            errorMessage = "写真の登録に失敗しました。"
                        )
                    }
                }
            )
        }
    }

    /**
     * デバッグ用: 写真追加シートを登録せずに閉じたとき（選択済みの画像は消す）
     */
    fun onDismissDebugAdd() {
        val addPhoto = dialogState.value.debugAddPhoto ?: return
        if (addPhoto.isSaving) return
        dialogState.update { it.copy(debugAddPhoto = null) }

        addPhoto.imagePath?.let { path ->
            viewModelScope.launch { imageStorage.deleteImage(path) }
        }
    }

    /**
     * デバッグ用: 「この月の動画を作成」が押されたとき（生成済みでも作り直し、完成したら再生する）
     */
    fun onDebugCreateVideoClicked() {
        if (dialogState.value.isCreatingVideo) return
        val month = displayedMonth.value
        dialogState.update { it.copy(isCreatingVideo = true) }

        viewModelScope.launch {
            createMonthlyVideoUseCase(month, overwrite = true).fold(
                onSuccess = { video ->
                    dialogState.update {
                        if (video != null) {
                            it.copy(isCreatingVideo = false, isVideoPlaying = true)
                        } else {
                            it.copy(isCreatingVideo = false, errorMessage = "この月には写真がありません。")
                        }
                    }
                },
                onFailure = {
                    dialogState.update {
                        it.copy(isCreatingVideo = false, errorMessage = "動画の作成に失敗しました。")
                    }
                }
            )
        }
    }

    private data class DialogState(
        val selectedPhoto: DailyPhoto? = null,
        val photoPendingDelete: DailyPhoto? = null,
        val errorMessage: String? = null,
        val isVideoPlaying: Boolean = false,
        val debugAddPhoto: DebugAddPhotoState? = null,
        val isCreatingVideo: Boolean = false
    )
}
