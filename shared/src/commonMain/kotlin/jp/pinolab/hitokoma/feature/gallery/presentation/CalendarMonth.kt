package jp.pinolab.hitokoma.feature.gallery.presentation

import jp.pinolab.hitokoma.core.time.firstOfMonth
import jp.pinolab.hitokoma.core.time.nextMonth
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus

/**
 * 日曜始まりの月カレンダーのセルを返す。月初前・月末後の空白は null で埋め、要素数は7の倍数になる
 */
fun buildMonthCells(firstOfMonth: LocalDate): List<LocalDate?> {
    val first = firstOfMonth.firstOfMonth()
    val daysInMonth = first.daysUntil(first.nextMonth())

    // ISO の曜日番号（月曜 = 1, ..., 日曜 = 7）を日曜 = 0 始まりに変換
    val leadingBlanks = first.dayOfWeek.isoDayNumber % 7
    val trailingBlanks = (7 - (leadingBlanks + daysInMonth) % 7) % 7

    return buildList {
        repeat(leadingBlanks) { add(null) }
        repeat(daysInMonth) { add(first.plus(it, DateTimeUnit.DAY)) }
        repeat(trailingBlanks) { add(null) }
    }
}
