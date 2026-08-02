package jp.pinolab.hitokoma.feature.selector.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.pinolab.hitokoma.domain.model.DailyPhoto
import jp.pinolab.hitokoma.feature.selector.domain.PhotoAlreadyExistsException
import jp.pinolab.hitokoma.feature.selector.domain.SaveDailyPhotoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class PhotoSelectorViewModel(
    private val saveDailyPhotoUseCase: SaveDailyPhotoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoSelectorUiState())
    val uiState: StateFlow<PhotoSelectorUiState> = _uiState.asStateFlow()

    /**
     * 端末のギャラリーから写真が選択されたとき
     */
    fun onImageSelected(imagePath: String) {
        _uiState.update {
            it.copy(
                selectedImagePath = imagePath,
                errorMessage = null
            )
        }
    }

    /**
     * コメント入力値が変更されたとき
     */
    fun onCommentChanged(newComment: String) {
        _uiState.update { it.copy(comment = newComment) }
    }

    /**
     * 保存ボタン押下時、または上書き確認ダイアログで「上書き」を選択したとき
     */
    fun onSaveClicked(allowOverwrite: Boolean = false) {
        val currentImagePath = _uiState.value.selectedImagePath ?: return
        val currentComment = _uiState.value.comment

        viewModelScope.launch {
            // 保存処理中状態へ更新（ダイアログは閉じる）
            _uiState.update {
                it.copy(
                    isSaving = true,
                    errorMessage = null,
                    showOverwriteDialog = false
                )
            }

            // 本日の日付（LocalDate）と現在のエポック秒を取得
            val now = Clock.System.now()
            val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

            val photoToSave = DailyPhoto(
                date = today,
                imagePath = currentImagePath,
                comment = currentComment,
                createdAtEpochMillis = now.toEpochMilliseconds()
            )

            // UseCaseを実行
            val result = saveDailyPhotoUseCase(photoToSave, allowOverwrite = allowOverwrite)

            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isSaveSuccess = true
                        )
                    }
                },
                onFailure = { exception ->
                    when (exception) {
                        // 1日1枚制限に引っかかった場合 -> 上書き確認ダイアログを表示
                        is PhotoAlreadyExistsException -> {
                            _uiState.update {
                                it.copy(
                                    isSaving = false,
                                    showOverwriteDialog = true
                                )
                            }
                        }
                        // その他のエラー（ストレージエラーなど）
                        else -> {
                            _uiState.update {
                                it.copy(
                                    isSaving = false,
                                    errorMessage = exception.message ?: "画像の保存に失敗しました。"
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    /**
     * 上書き確認ダイアログをキャンセルしたとき
     */
    fun onDismissOverwriteDialog() {
        _uiState.update { it.copy(showOverwriteDialog = false) }
    }

    /**
     * エラーメッセージを閉じたとき
     */
    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}