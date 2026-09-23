package jp.pinolab.hitokoma.feature.gallery.presentation

import jp.pinolab.hitokoma.domain.model.DailyPhoto

data class PhotoListUiState(
    val photos: List<DailyPhoto> = emptyList(), // 登録済みの写真（新しい日付順）
    val isLoading: Boolean = false              // 初回読み込み中フラグ
) {
    // 空状態の案内を表示するかどうかの判定
    val isEmpty: Boolean
        get() = !isLoading && photos.isEmpty()
}
