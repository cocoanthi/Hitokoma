package jp.pinolab.hitokoma.core.time

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

class TodayFlowTest {

    private val timeZone = TimeZone.of("Asia/Tokyo")

    // テストの仮想時間に合わせて進む時計
    private fun TestScope.virtualClock(start: Instant) = object : Clock {
        override fun now(): Instant = start + testScheduler.currentTime.milliseconds
    }

    @Test
    fun 日付が変わると翌日の日付が流れる() = runTest {
        val start = LocalDateTime(2026, 9, 24, 23, 59, 30).toInstant(timeZone)
        val clock = virtualClock(start)

        val emitted = todayFlow(clock, timeZone).take(2).toList()

        assertEquals(
            listOf(LocalDate(2026, 9, 24), LocalDate(2026, 9, 25)),
            emitted
        )
    }

    @Test
    fun 翌日の日付は0時ちょうどに流れる() = runTest {
        val start = LocalDateTime(2026, 9, 24, 23, 59, 30).toInstant(timeZone)
        val clock = virtualClock(start)

        val emittedAt = mutableListOf<Long>()
        todayFlow(clock, timeZone)
            .take(2)
            .collect { emittedAt += testScheduler.currentTime }

        // 起動直後に今日、30秒後（0時）に翌日
        assertEquals(listOf(0L, 30_000L), emittedAt)
    }
}
