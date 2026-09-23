package jp.pinolab.hitokoma.feature.gallery.presentation

import jp.pinolab.hitokoma.domain.model.DailyPhoto

data class PhotoListUiState(
    val photos: List<DailyPhoto> = emptyList(), // 登録済みの写真（新しい日付順）
    val isLoading: Boolean = false,             // 初回読み込み中フラグ
    val photoPendingDelete: DailyPhoto? = null, // 削除確認ダイアログの対象（null ならダイアログ非表示）
    val errorMessage: String? = null            // 削除失敗時のエラーメッセージ
) {
    // 空状態の案内を表示するかどうかの判定
    val isEmpty: Boolean
        get() = !isLoading && photos.isEmpty()
}
