package jp.pinolab.hitokoma.feature.gallery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.pinolab.hitokoma.feature.gallery.domain.ObserveAllPhotosUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PhotoListViewModel(
    observeAllPhotosUseCase: ObserveAllPhotosUseCase
) : ViewModel() {

    // DB の変更（新規登録・上書き）がそのまま一覧に反映される
    val uiState: StateFlow<PhotoListUiState> = observeAllPhotosUseCase()
        .map { photos -> PhotoListUiState(photos = photos, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PhotoListUiState(isLoading = true)
        )
}
