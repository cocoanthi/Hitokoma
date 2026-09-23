package jp.pinolab.hitokoma.feature.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class PhotoListViewModel(
    observeAllPhotosUseCase: ObserveAllPhotosUseCase,
    private val deleteDailyPhotoUseCase: DeleteDailyPhotoUseCase
) : ViewModel() {

    // ダイアログ表示など、画面操作による状態
    private val dialogState = MutableStateFlow(DialogState())

    // DB の変更（新規登録・上書き・削除）がそのまま一覧に反映される
    val uiState: StateFlow<PhotoListUiState> = combine(
        observeAllPhotosUseCase(),
        dialogState
    ) { photos, dialog ->
        PhotoListUiState(
            photos = photos,
            isLoading = false,
            photoPendingDelete = dialog.photoPendingDelete,
            errorMessage = dialog.errorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PhotoListUiState(isLoading = true)
    )

    /**
     * カードの削除ボタンが押されたとき（確認ダイアログを表示）
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
     * 削除確認ダイアログで「削除」を選択したとき
     */
    fun onConfirmDelete() {
        val photo = dialogState.value.photoPendingDelete ?: return
        dialogState.update { it.copy(photoPendingDelete = null) }

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
        val photoPendingDelete: DailyPhoto? = null,
        val errorMessage: String? = null
    )
}
