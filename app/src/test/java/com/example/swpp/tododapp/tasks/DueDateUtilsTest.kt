package com.example.swpp.tododapp.tasks

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/** One test per [Unit] Tip in the reviewed DueDateUtils.spec.md. */
class DueDateUtilsTest {
    private fun date(value: String): LocalDate = LocalDate.parse(value)

    @Test
    fun dueDateLabel_completedTaskAnyDueDate_returnsClear() {
        // Given
        val today = date("2026-10-15")
        val dueDates = listOf(null, date("2026-10-14"), date("2026-10-15"), date("2026-10-16"))
        // When
        val actual = dueDates.map { dueDateLabel(it, today, true) }
        // Then
        assertEquals(listOf("CLEAR", "CLEAR", "CLEAR", "CLEAR"), actual)
    }

    @Test
    fun dueDateLabel_activeTaskWithoutDueDate_returnsEmpty() {
        // Given
        val today = date("2026-10-15")
        // When
        val actual = dueDateLabel(null, today, false)
        // Then
        assertEquals("", actual)
    }

    @Test
    fun dueDateLabel_dueToday_returnsDDay() {
        // Given
        val today = date("2026-10-15")
        // When
        val actual = dueDateLabel(date("2026-10-15"), today, false)
        // Then
        assertEquals("D-Day", actual)
    }

    @Test
    fun dueDateLabel_aroundFullMonth_returnsTotalDays() {
        // Given
        val today = date("2026-10-15")
        val dates = listOf("2026-11-14", "2026-11-15", "2026-11-16")
        // When
        val actual = dates.map { dueDateLabel(date(it), today, false) }
        // Then
        assertEquals(listOf("D-30", "D-31", "D-32"), actual)
    }

    @Test
    fun dueDateLabel_overdueFullMonth_returnsTotalDays() {
        // Given
        val today = date("2026-10-15")
        // When
        val actual = dueDateLabel(date("2026-09-15"), today, false)
        // Then
        assertEquals("D+30", actual)
    }

    @Test
    fun dueDateLabel_yesterdayTodayTomorrow_returnsSignedLabels() {
        // Given
        val today = date("2026-10-15")
        val dates = listOf("2026-10-14", "2026-10-15", "2026-10-16")
        // When
        val actual = dates.map { dueDateLabel(date(it), today, false) }
        // Then
        assertEquals(listOf("D+1", "D-Day", "D-1"), actual)
    }

    @Test
    fun dueDateLabel_monthBoundary_returnsContinuousDays() {
        // Given
        val today = date("2026-01-30")
        val dates = listOf("2026-01-31", "2026-02-01", "2026-02-02")
        // When
        val actual = dates.map { dueDateLabel(date(it), today, false) }
        // Then
        assertEquals(listOf("D-1", "D-2", "D-3"), actual)
    }

    @Test
    fun dueDateLabel_yearBoundary_returnsContinuousDays() {
        // Given
        val today = date("2025-12-30")
        val dates = listOf("2025-12-31", "2026-01-01", "2026-01-02")
        // When
        val actual = dates.map { dueDateLabel(date(it), today, false) }
        // Then
        assertEquals(listOf("D-1", "D-2", "D-3"), actual)
    }

    @Test
    fun dueDateLabel_leapDay_returnsContinuousDays() {
        // Given
        val today = date("2024-02-27")
        val dates = listOf("2024-02-28", "2024-02-29", "2024-03-01")
        // When
        val actual = dates.map { dueDateLabel(date(it), today, false) }
        // Then
        assertEquals(listOf("D-1", "D-2", "D-3"), actual)
    }

    @Test
    fun dueDateLabel_nonLeapFebruary_returnsContinuousDays() {
        // Given
        val today = date("2025-02-27")
        val dates = listOf("2025-02-28", "2025-03-01", "2025-03-02")
        // When
        val actual = dates.map { dueDateLabel(date(it), today, false) }
        // Then
        assertEquals(listOf("D-1", "D-2", "D-3"), actual)
    }

    @Test
    fun dueDateLabel_pastCalendarBoundaries_returnsTotalOverdueDays() {
        // Given
        val dates = listOf(
            "2026-01-30" to "2026-03-01",
            "2025-12-31" to "2026-01-02",
            "2024-02-28" to "2024-03-01"
        )
        // When
        val actual = dates.map { (due, today) -> dueDateLabel(date(due), date(today), false) }
        // Then
        assertEquals(listOf("D+30", "D+2", "D+2"), actual)
    }

    @Test
    fun dueDateLabel_completionWithAndWithoutDueDate_overridesActiveLabel() {
        // Given
        val today = date("2026-10-15")
        val inputs = listOf(null to false, null to true, date("2026-10-16") to false, date("2026-10-16") to true)
        // When
        val actual = inputs.map { (due, completed) -> dueDateLabel(due, today, completed) }
        // Then
        assertEquals(listOf("", "CLEAR", "D-1", "CLEAR"), actual)
    }
}

