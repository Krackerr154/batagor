package com.geraldarya.studenttasks.worker.utils

import com.geraldarya.studenttasks.constants.ReminderConstants
import com.geraldarya.studenttasks.data.TaskEntity

/**
 * Pure helper functions for deadline reminder logic.
 * Extracted for testability and reusability.
 */
object DeadlineHelper {

    /**
     * Determines urgency level for a task based on remaining time.
     *
     * @param taskDueMillis When the task is due (milliseconds since epoch)
     * @param currentMillis Current time (milliseconds since epoch)
     * @return Urgency message or null if not urgent
     */
    fun determineUrgency(taskDueMillis: Long, currentMillis: Long): String? {
        val remaining = taskDueMillis - currentMillis
        return when {
            remaining < 0 -> "Overdue"
            remaining <= ReminderConstants.URGENT_THRESHOLD_MILLIS -> "Due within 24 hours"
            remaining <= ReminderConstants.LOOKAHEAD_WINDOW_MILLIS -> "Due within 3 days"
            else -> null
        }
    }

    /**
     * Validates that a task has the minimum required data for notifications.
     *
     * @param task Task to validate
     * @return true if task is valid for notification, false otherwise
     */
    fun isValidForNotification(task: TaskEntity): Boolean {
        return task.title.isNotBlank() && task.dueAtMillis > 0
    }

    /**
     * Computes the lookahead window end time.
     *
     * @param currentMillis Current time
     * @return End of lookahead window
     */
    fun computeLookaheadEnd(currentMillis: Long): Long {
        return currentMillis + ReminderConstants.LOOKAHEAD_WINDOW_MILLIS
    }

    /**
     * Checks if a task falls within the reminder window.
     *
     * @param taskDueMillis When the task is due
     * @param windowStart Start of the time window
     * @param windowEnd End of the time window
     * @return true if task is within window, false otherwise
     */
    fun isWithinWindow(taskDueMillis: Long, windowStart: Long, windowEnd: Long): Boolean {
        return taskDueMillis in windowStart..windowEnd
    }
}
