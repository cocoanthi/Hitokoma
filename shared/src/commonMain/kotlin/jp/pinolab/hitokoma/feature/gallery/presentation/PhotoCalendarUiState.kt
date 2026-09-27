package jp.pinolab.hitokoma.feature.gallery.presentation

import jp.pinolab.hitokoma.core.time.firstOfMonth
import jp.pinolab.hitokoma.domain.model.DailyPhoto
import kotlinx.datetime.LocalDate

data class PhotoCalendarUiState(
    val displayedMonth: LocalDate,                          // 表示中の月（その月の1日）
    val today: LocalDate,                                   // 今日（セルの強調・翌月ボタンの制御用）
    val photosByDate: Map<LocalDate, DailyPhoto> = emptyMap(), // 登録済みの写真（日付で引く）
    val isLoading: Boolean = false,                         // 初回読み込み中フラグ
    val selectedPhoto: DailyPhoto? = null,                  // 詳細シートの対象（null ならシート非表示）
    val photoPendingDelete: DailyPhoto? = null,             // 削除確認ダイアログの対象（null ならダイアログ非表示）
    val errorMessage: String? = null,                       // 削除失敗時のエラーメッセージ
    val videoPath: String? = null,                          // 表示中の月のストーリー動画（未生成なら null）
    val isVideoPlaying: Boolean = false                     // ストーリー動画のプレイヤーを表示中か
) {
    // 表示中の月のカレンダーのセル（日曜始まり、空白は null）
    val cells: List<LocalDate?>
        get() = buildMonthCells(displayedMonth)

    // 未来の月へは進めない
    val canGoNextMonth: Boolean
        get() = displayedMonth < today.firstOfMonth()
}
