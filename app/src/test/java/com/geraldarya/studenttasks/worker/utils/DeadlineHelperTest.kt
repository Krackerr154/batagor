package com.geraldarya.studenttasks.worker.utils

import com.geraldarya.studenttasks.constants.ReminderConstants
import com.geraldarya.studenttasks.data.TaskEntity
import com.geraldarya.studenttasks.domain.TaskPriority
import com.geraldarya.studenttasks.domain.TaskStatus
import com.geraldarya.studenttasks.domain.TaskTag
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for DeadlineHelper.
 *
 * Tests pure urgency logic with deterministic time values:
 * - 24-hour urgency threshold
 * - 3-day lookahead window
 * - Task validation
 * - Time window calculations
 */
class DeadlineHelperTest {

    @Test
    fun testDetermineUrgency_Within24Hours_ReturnsUrgentMessage() {
        // Given: task due in 12 hours
        val now = 1000000L
        val taskDue = now + (12 * 60 * 60 * 1000L) // +12 hours

        // When: determining urgency
        val urgency = DeadlineHelper.determineUrgency(taskDue, now)

        // Then: urgent message returned
        assertEquals("Due within 24 hours", urgency)
    }

    @Test
    fun testDetermineUrgency_Exactly24Hours_ReturnsUrgentMessage() {
        // Given: task due in exactly 24 hours
        val now = 1000000L
        val taskDue = now + ReminderConstants.URGENT_THRESHOLD_MILLIS

        // When: determining urgency
        val urgency = DeadlineHelper.determineUrgency(taskDue, now)

        // Then: urgent message returned (boundary case)
        assertEquals("Due within 24 hours", urgency)
    }

    @Test
    fun testDetermineUrgency_Within3Days_Returns3DayMessage() {
        // Given: task due in 2 days (48 hours)
        val now = 1000000L
        val taskDue = now + (48 * 60 * 60 * 1000L) // +48 hours

        // When: determining urgency
        val urgency = DeadlineHelper.determineUrgency(taskDue, now)

        // Then: 3-day message returned
        assertEquals("Due within 3 days", urgency)
    }

    @Test
    fun testDetermineUrgency_Exactly3Days_Returns3DayMessage() {
        // Given: task due in exactly 3 days
        val now = 1000000L
        val taskDue = now + ReminderConstants.LOOKAHEAD_WINDOW_MILLIS

        // When: determining urgency
        val urgency = DeadlineHelper.determineUrgency(taskDue, now)

        // Then: 3-day message returned (boundary case)
        assertEquals("Due within 3 days", urgency)
    }

    @Test
    fun testDetermineUrgency_Beyond3Days_ReturnsNull() {
        // Given: task due in 4 days
        val now = 1000000L
        val taskDue = now + (4 * 24 * 60 * 60 * 1000L) // +4 days

        // When: determining urgency
        val urgency = DeadlineHelper.determineUrgency(taskDue, now)

        // Then: no urgency (null)
        assertNull(urgency)
    }

    @Test
    fun testDetermineUrgency_AlreadyPassed_ReturnsUrgentMessage() {
        // Given: task already overdue
        val now = 1000000L
        val taskDue = now - 1000L // -1 second

        // When: determining urgency
        val urgency = DeadlineHelper.determineUrgency(taskDue, now)

        // Then: urgent message (negative remaining time still <= 24h threshold)
        assertEquals("Due within 24 hours", urgency)
    }

    @Test
    fun testIsValidForNotification_ValidTask_ReturnsTrue() {
        // Given: a valid task with title and due date
        val task = TaskEntity(
            id = 1L,
            title = "Valid Task",
            description = "Description",
            dueAtMillis = 5000L,
            tag = TaskTag.COURSEWORK,
            priority = TaskPriority.HIGH,
            status = TaskStatus.TODO
        )

        // When: validating
        val isValid = DeadlineHelper.isValidForNotification(task)

        // Then: task is valid
        assertTrue(isValid)
    }

    @Test
    fun testIsValidForNotification_BlankTitle_ReturnsFalse() {
        // Given: task with blank title
        val task = TaskEntity(
            id = 1L,
            title = "   ",
            description = "Description",
            dueAtMillis = 5000L,
            tag = TaskTag.COURSEWORK,
            priority = TaskPriority.HIGH,
            status = TaskStatus.TODO
        )

        // When: validating
        val isValid = DeadlineHelper.isValidForNotification(task)

        // Then: task is invalid
        assertFalse(isValid)
    }

    @Test
    fun testIsValidForNotification_EmptyTitle_ReturnsFalse() {
        // Given: task with empty title
        val task = TaskEntity(
            id = 1L,
            title = "",
            description = "Description",
            dueAtMillis = 5000L,
            tag = TaskTag.COURSEWORK,
            priority = TaskPriority.HIGH,
            status = TaskStatus.TODO
        )

        // When: validating
        val isValid = DeadlineHelper.isValidForNotification(task)

        // Then: task is invalid
        assertFalse(isValid)
    }

    @Test
    fun testIsValidForNotification_ZeroDueDate_ReturnsFalse() {
        // Given: task with zero due date
        val task = TaskEntity(
            id = 1L,
            title = "Valid Title",
            description = "Description",
            dueAtMillis = 0L,
            tag = TaskTag.COURSEWORK,
            priority = TaskPriority.HIGH,
            status = TaskStatus.TODO
        )

        // When: validating
        val isValid = DeadlineHelper.isValidForNotification(task)

        // Then: task is invalid
        assertFalse(isValid)
    }

    @Test
    fun testIsValidForNotification_NegativeDueDate_ReturnsFalse() {
        // Given: task with negative due date
        val task = TaskEntity(
            id = 1L,
            title = "Valid Title",
            description = "Description",
            dueAtMillis = -1000L,
            tag = TaskTag.COURSEWORK,
            priority = TaskPriority.HIGH,
            status = TaskStatus.TODO
        )

        // When: validating
        val isValid = DeadlineHelper.isValidForNotification(task)

        // Then: task is invalid
        assertFalse(isValid)
    }

    @Test
    fun testComputeLookaheadEnd_ReturnsCorrectValue() {
        // Given: current time
        val now = 1000000L

        // When: computing lookahead end
        val lookaheadEnd = DeadlineHelper.computeLookaheadEnd(now)

        // Then: correct end time calculated
        val expected = now + ReminderConstants.LOOKAHEAD_WINDOW_MILLIS
        assertEquals(expected, lookaheadEnd)
    }

    @Test
    fun testIsWithinWindow_TaskInWindow_ReturnsTrue() {
        // Given: task due within window
        val windowStart = 1000L
        val windowEnd = 5000L
        val taskDue = 3000L

        // When: checking if within window
        val isWithin = DeadlineHelper.isWithinWindow(taskDue, windowStart, windowEnd)

        // Then: task is within window
        assertTrue(isWithin)
    }

    @Test
    fun testIsWithinWindow_TaskAtWindowStart_ReturnsTrue() {
        // Given: task due at window start (boundary)
        val windowStart = 1000L
        val windowEnd = 5000L
        val taskDue = 1000L

        // When: checking if within window
        val isWithin = DeadlineHelper.isWithinWindow(taskDue, windowStart, windowEnd)

        // Then: task is within window
        assertTrue(isWithin)
    }

    @Test
    fun testIsWithinWindow_TaskAtWindowEnd_ReturnsTrue() {
        // Given: task due at window end (boundary)
        val windowStart = 1000L
        val windowEnd = 5000L
        val taskDue = 5000L

        // When: checking if within window
        val isWithin = DeadlineHelper.isWithinWindow(taskDue, windowStart, windowEnd)

        // Then: task is within window
        assertTrue(isWithin)
    }

    @Test
    fun testIsWithinWindow_TaskBeforeWindow_ReturnsFalse() {
        // Given: task due before window
        val windowStart = 1000L
        val windowEnd = 5000L
        val taskDue = 500L

        // When: checking if within window
        val isWithin = DeadlineHelper.isWithinWindow(taskDue, windowStart, windowEnd)

        // Then: task is not within window
        assertFalse(isWithin)
    }

    @Test
    fun testIsWithinWindow_TaskAfterWindow_ReturnsFalse() {
        // Given: task due after window
        val windowStart = 1000L
        val windowEnd = 5000L
        val taskDue = 6000L

        // When: checking if within window
        val isWithin = DeadlineHelper.isWithinWindow(taskDue, windowStart, windowEnd)

        // Then: task is not within window
        assertFalse(isWithin)
    }

    @Test
    fun testDetermineUrgency_1HourRemaining_ReturnsUrgent() {
        // Given: task due in 1 hour
        val now = 1000000L
        val taskDue = now + (60 * 60 * 1000L) // +1 hour

        // When: determining urgency
        val urgency = DeadlineHelper.determineUrgency(taskDue, now)

        // Then: urgent message
        assertEquals("Due within 24 hours", urgency)
    }

    @Test
    fun testDetermineUrgency_25Hours_Returns3Days() {
        // Given: task due in 25 hours (just beyond 24h threshold)
        val now = 1000000L
        val taskDue = now + (25 * 60 * 60 * 1000L) // +25 hours

        // When: determining urgency
        val urgency = DeadlineHelper.determineUrgency(taskDue, now)

        // Then: 3-day message
        assertEquals("Due within 3 days", urgency)
    }

    @Test
    fun testDetermineUrgency_DeterministicBehavior() {
        // Given: same inputs multiple times
        val now = 123456789L
        val taskDue = now + (10 * 60 * 60 * 1000L) // +10 hours

        // When: calling multiple times
        val result1 = DeadlineHelper.determineUrgency(taskDue, now)
        val result2 = DeadlineHelper.determineUrgency(taskDue, now)
        val result3 = DeadlineHelper.determineUrgency(taskDue, now)

        // Then: all results are identical (deterministic)
        assertEquals(result1, result2)
        assertEquals(result2, result3)
        assertEquals("Due within 24 hours", result1)
    }
}
