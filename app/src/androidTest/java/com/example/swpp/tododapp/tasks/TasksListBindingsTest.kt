package com.example.swpp.tododapp.tasks

import android.view.View
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.example.swpp.tododapp.data.Task
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.time.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** One test per [Integration] Tip in the reviewed TasksListBindings.spec.md.
 * Static date mocking requires Android 9 (API 28) or newer.
 */
@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 28)
class TasksListBindingsTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val defaultToday = LocalDate.of(2026, 10, 15)

    @Before
    fun freezeToday() {
        mockkStatic(LocalDate::class)
        every { LocalDate.now() } returns defaultToday
    }

    @After
    fun restoreDate() {
        unmockkStatic(LocalDate::class)
    }

    private fun date(value: String): LocalDate = LocalDate.parse(value)

    private fun task(dueDate: LocalDate?, completed: Boolean = false) = Task(
        title = "Task",
        description = "Description",
        id = "task-1",
        dueDate = dueDate,
        isCompleted = completed
    )

    private fun view(text: String, visibility: Int) =
        TextView(instrumentation.targetContext).apply {
            this.text = text
            this.visibility = visibility
        }

    private fun assertLabel(view: TextView, text: String, visibility: Int, case: String) {
        assertEquals("label text for $case", text, view.text.toString())
        assertEquals("label visibility for $case", visibility, view.visibility)
    }

    @Test
    fun setDueDateLabel_completedAnyDueDate_showsClear() = instrumentation.runOnMainSync {
        // Given
        val dates = listOf(null, date("2026-10-14"), date("2026-10-15"), date("2026-10-16"))
        dates.forEach { due ->
            val textView = view("old", View.GONE)
            // When
            setDueDateLabel(textView, task(due, completed = true))
            // Then
            assertLabel(textView, "CLEAR", View.VISIBLE, "completed, dueDate=$due")
        }
    }

    @Test
    fun setDueDateLabel_activeDueDates_showsExactLabels() = instrumentation.runOnMainSync {
        // Given
        val cases = listOf("2026-10-12" to "D+3", "2026-10-15" to "D-Day", "2026-11-15" to "D-31")
        cases.forEach { (due, expected) ->
            val textView = view("old", View.INVISIBLE)
            // When
            setDueDateLabel(textView, task(date(due)))
            // Then
            assertLabel(textView, expected, View.VISIBLE, "active, dueDate=$due")
        }
    }

    @Test
    fun setDueDateLabel_nullTask_clearsAndHides() = instrumentation.runOnMainSync {
        // Given
        val textView = view("D-5", View.VISIBLE)
        // When
        setDueDateLabel(textView, null)
        // Then
        assertLabel(textView, "", View.GONE, "null task")
    }

    @Test
    fun setDueDateLabel_activeWithoutDueDate_clearsAndHides() = instrumentation.runOnMainSync {
        // Given
        val textView = view("CLEAR", View.VISIBLE)
        val currentTask = task(null)
        // When
        setDueDateLabel(textView, currentTask)
        // Then
        assertLabel(textView, "", View.GONE, "active, dueDate=null")
    }

    @Test
    fun setDueDateLabel_yesterdayTodayTomorrow_showsSignedLabels() = instrumentation.runOnMainSync {
        // Given
        val textView = view("", View.GONE)
        val cases = listOf("2026-10-14" to "D+1", "2026-10-15" to "D-Day", "2026-10-16" to "D-1")
        cases.forEach { (due, expected) ->
            // When
            setDueDateLabel(textView, task(date(due)))
            // Then
            assertLabel(textView, expected, View.VISIBLE, "active, dueDate=$due")
        }
    }

    @Test
    fun setDueDateLabel_rebindThroughNull_clearsThenRestores() = instrumentation.runOnMainSync {
        // Given
        val textView = view("", View.GONE)
        val tasks = listOf(task(date("2026-10-16")), null, task(date("2026-10-15")))
        val expected = listOf("D-1" to View.VISIBLE, "" to View.GONE, "D-Day" to View.VISIBLE)
        tasks.zip(expected).forEachIndexed { index, (currentTask, label) ->
            // When
            setDueDateLabel(textView, currentTask)
            // Then
            assertLabel(textView, label.first, label.second, "binding #${index + 1}: $currentTask")
        }
    }

    @Test
    fun setDueDateLabel_rebindCompletionWithoutDate_refreshesTextAndVisibility() = instrumentation.runOnMainSync {
        // Given
        val textView = view("", View.GONE)
        val tasks = listOf(task(null, false), task(null, true), task(null, false))
        val expected = listOf("" to View.GONE, "CLEAR" to View.VISIBLE, "" to View.GONE)
        tasks.zip(expected).forEachIndexed { index, (currentTask, label) ->
            // When
            setDueDateLabel(textView, currentTask)
            // Then
            assertLabel(textView, label.first, label.second, "binding #${index + 1}: $currentTask")
        }
    }

    @Test
    fun setDueDateLabel_checkThenUncheckWithDueDate_switchesBetweenDDayAndClear() = instrumentation.runOnMainSync {
        // Given
        val textView = view("", View.GONE)
        val dueDate = date("2026-10-16")
        val tasks = listOf(task(dueDate, false), task(dueDate, true), task(dueDate, false))
        val expected = listOf("D-1" to View.VISIBLE, "CLEAR" to View.VISIBLE, "D-1" to View.VISIBLE)
        tasks.zip(expected).forEachIndexed { index, (currentTask, label) ->
            // When
            setDueDateLabel(textView, currentTask)
            // Then
            assertLabel(textView, label.first, label.second, "binding #${index + 1}: $currentTask")
        }
    }

    @Test
    fun setDueDateLabel_localDateChanges_recomputesLabelEachCall() = instrumentation.runOnMainSync {
        // Given
        val textView = view("old", View.GONE)
        val currentTask = task(date("2026-10-15"))
        val cases = listOf("2026-10-14" to "D-1", "2026-10-15" to "D-Day", "2026-10-16" to "D+1")
        cases.forEach { (today, expected) ->
            val fixedDate = date(today)
            every { LocalDate.now() } returns fixedDate
            // When
            setDueDateLabel(textView, currentTask)
            // Then
            assertLabel(textView, expected, View.VISIBLE, "today=$today, dueDate=2026-10-15")
        }
    }

    @Test
    fun setDueDateLabel_leapDayBoundary_showsContinuousDays() = instrumentation.runOnMainSync {
        // Given
        val fixedDate = date("2024-02-27")
        every { LocalDate.now() } returns fixedDate
        val textView = view("", View.GONE)
        val cases = listOf("2024-02-28" to "D-1", "2024-02-29" to "D-2", "2024-03-01" to "D-3")
        cases.forEach { (due, expected) ->
            // When
            setDueDateLabel(textView, task(date(due)))
            // Then
            assertLabel(textView, expected, View.VISIBLE, "today=2024-02-27, dueDate=$due")
        }
    }
}
