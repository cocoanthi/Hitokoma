package jp.pinolab.hitokoma.core.time

import kotlinx.datetime.LocalDate

// 例: 2026-09-23 -> "2026年9月23日"
fun LocalDate.toJapaneseString(): String = "${year}年${monthNumber}月${dayOfMonth}日"
