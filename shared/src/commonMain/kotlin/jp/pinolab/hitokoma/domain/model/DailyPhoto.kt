package jp.pinolab.hitokoma.domain.model

import kotlinx.datetime.LocalDate

data class DailyPhoto(
    val date: LocalDate,              // 一意キーとなる日付 (例: 2026-08-01)
    val imagePath: String,           // ローカルストレージ内の画像ファイルパス
    val comment: String,              // 短いコメント
    val createdAtEpochMillis: Long   // 保存日時のタイムスタンプ
)