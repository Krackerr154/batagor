package com.geraldarya.studenttasks.constants

/**
 * Centralized constants for deadline reminder system.
 *
 * Used by:
 * - DeadlineNotificationWorker for checking upcoming deadlines
 * - StudentTaskApplication for worker scheduling
 * - UI components for deadline urgency display
 */
object ReminderConstants {
    /**
     * Worker scheduling interval: how often to check for upcoming deadlines.
     * Set to 6 hours to balance timeliness with battery efficiency.
     */
    const val WORKER_INTERVAL_HOURS = 6L

    /**
     * Lookahead window: how far ahead to check for upcoming tasks.
     * Set to 3 days to give users advance notice of deadlines.
     */
    const val LOOKAHEAD_WINDOW_DAYS = 3L

    /**
     * Urgency threshold: tasks due within this window are marked "urgent".
     * Set to 24 hours for immediate attention.
     */
    const val URGENT_THRESHOLD_HOURS = 24L

    // Millisecond conversion constants
    const val HOUR_MILLIS = 60L * 60L * 1000L
    const val DAY_MILLIS = 24L * HOUR_MILLIS

    // Computed thresholds
    const val URGENT_THRESHOLD_MILLIS = URGENT_THRESHOLD_HOURS * HOUR_MILLIS
    const val LOOKAHEAD_WINDOW_MILLIS = LOOKAHEAD_WINDOW_DAYS * DAY_MILLIS

    /**
     * Worker unique name for WorkManager.
     */
    const val UNIQUE_WORK_NAME = "deadline_check_worker"
}
