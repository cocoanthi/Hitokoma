package jp.pinolab.hitokoma.feature.selector.presentation

data class PhotoSelectorUiState(
    val selectedImagePath: String? = null, // 選択中の画像パス
    val comment: String = "",               // コメント入力値
    val isSaving: Boolean = false,          // 保存中プログレス表示フラグ
    val isSaveSuccess: Boolean = false,     // 保存成功フラグ（画面遷移のトリガー）
    val errorMessage: String? = null,       // 一般的なエラーメッセージ
    val showOverwriteDialog: Boolean = false // 「すでに写真が存在します。上書きしますか？」ダイアログ表示フラグ
) {
    // 保存ボタンが押せるかどうかの判定
    val canSave: Boolean
        get() = !selectedImagePath.isNullOrBlank() && !isSaving
}