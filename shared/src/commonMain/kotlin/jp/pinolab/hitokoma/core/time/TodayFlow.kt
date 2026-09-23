package jp.pinolab.hitokoma.core.time

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

/**
 * 今日の日付を流し、日付が変わる（0時をまたぐ）たびに新しい日付を流す
 */
fun todayFlow(
    clock: Clock = Clock.System,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Flow<LocalDate> = flow {
    while (true) {
        val today = clock.todayIn(timeZone)
        emit(today)

        // 次の日の0時まで待つ
        val nextMidnight = today.plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone)
        delay(nextMidnight - clock.now())
    }
}.distinctUntilChanged()
