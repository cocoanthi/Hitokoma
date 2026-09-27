package jp.pinolab.hitokoma.feature.gallery.presentation

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus

// kotlinx-datetime 0.6.1 には YearMonth がないため、「月」はその月の1日の LocalDate で表す

/**
 * その月の1日を返す
 */
fun LocalDate.firstOfMonth(): LocalDate = LocalDate(year, monthNumber, 1)

/**
 * 前月の1日を返す
 */
fun LocalDate.previousMonth(): LocalDate = firstOfMonth().minus(1, DateTimeUnit.MONTH)

/**
 * 翌月の1日を返す
 */
fun LocalDate.nextMonth(): LocalDate = firstOfMonth().plus(1, DateTimeUnit.MONTH)

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
