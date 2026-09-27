package jp.pinolab.hitokoma.feature.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.pinolab.hitokoma.core.time.todayFlow
import jp.pinolab.hitokoma.domain.model.DailyPhoto
import jp.pinolab.hitokoma.feature.gallery.domain.DeleteDailyPhotoUseCase
import jp.pinolab.hitokoma.feature.gallery.domain.ObserveAllPhotosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

class PhotoCalendarViewModel(
    observeAllPhotosUseCase: ObserveAllPhotosUseCase,
    private val deleteDailyPhotoUseCase: DeleteDailyPhotoUseCase
) : ViewModel() {

    private val initialToday = Clock.System.todayIn(TimeZone.currentSystemDefault())

    // 表示中の月（その月の1日）。初期値は今月
    private val displayedMonth = MutableStateFlow(initialToday.firstOfMonth())

    // シート・ダイアログ表示など、画面操作による状態
    private val dialogState = MutableStateFlow(DialogState())

    // DB の変更（新規登録・上書き・削除）と日付の切り替わりがそのままカレンダーに反映される
    val uiState: StateFlow<PhotoCalendarUiState> = combine(
        observeAllPhotosUseCase(),
        todayFlow(),
        displayedMonth,
        dialogState
    ) { photos, today, month, dialog ->
        PhotoCalendarUiState(
            displayedMonth = month,
            today = today,
            photosByDate = photos.associateBy { it.date },
            isLoading = false,
            selectedPhoto = dialog.selectedPhoto,
            photoPendingDelete = dialog.photoPendingDelete,
            errorMessage = dialog.errorMessage
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

    private data class DialogState(
        val selectedPhoto: DailyPhoto? = null,
        val photoPendingDelete: DailyPhoto? = null,
        val errorMessage: String? = null
    )
}
