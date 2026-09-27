package jp.pinolab.hitokoma.core.time

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
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
