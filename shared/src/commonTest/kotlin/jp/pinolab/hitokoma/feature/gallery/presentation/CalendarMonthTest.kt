package jp.pinolab.hitokoma.feature.gallery.presentation

import jp.pinolab.hitokoma.core.time.nextMonth
import jp.pinolab.hitokoma.core.time.previousMonth
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CalendarMonthTest {

    @Test
    fun 火曜始まりの月は先頭に空白が2つ入る() {
        // 2026年9月1日は火曜、30日まで
        val cells = buildMonthCells(LocalDate(2026, 9, 1))

        assertNull(cells[0])
        assertNull(cells[1])
        assertEquals(LocalDate(2026, 9, 1), cells[2])
        assertEquals(0, cells.size % 7)
        assertEquals(LocalDate(2026, 9, 30), cells.last { it != null })
        assertEquals(30, cells.count { it != null })
    }

    @Test
    fun 日曜始まりの28日の月はちょうど4週になる() {
        // 2026年2月1日は日曜
        val cells = buildMonthCells(LocalDate(2026, 2, 1))

        assertEquals(28, cells.size)
        assertEquals(LocalDate(2026, 2, 1), assertNotNull(cells.first()))
        assertEquals(LocalDate(2026, 2, 28), assertNotNull(cells.last()))
    }

    @Test
    fun `12月の翌月は翌年1月になる`() {
        assertEquals(LocalDate(2027, 1, 1), LocalDate(2026, 12, 15).nextMonth())
    }

    @Test
    fun `1月の前月は前年12月になる`() {
        assertEquals(LocalDate(2025, 12, 1), LocalDate(2026, 1, 20).previousMonth())
    }
}
