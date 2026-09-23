package jp.pinolab.hitokoma.feature.selector.presentation

import jp.pinolab.hitokoma.domain.model.DailyPhoto
import kotlinx.datetime.LocalDate

data class PhotoSelectorUiState(
    val today: LocalDate? = null,           // 今日の日付（日付が変わると更新される）
    val todayPhoto: DailyPhoto? = null,     // 今日すでに登録済みの写真（未登録なら null）
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