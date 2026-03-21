package com.geraldarya.studenttasks.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.geraldarya.studenttasks.constants.ReminderConstants
import com.geraldarya.studenttasks.data.AppDatabase
import com.geraldarya.studenttasks.notifications.NotificationHelper
import com.geraldarya.studenttasks.worker.utils.DeadlineHelper

class DeadlineNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            // Defensive: wrap database access in try-catch
            val repository = AppDatabase.getInstance(applicationContext).taskDao()
            val now = System.currentTimeMillis()
            val lookaheadEnd = DeadlineHelper.computeLookaheadEnd(now)

            val tasks = try {
                repository.getUpcomingNotDone(now, lookaheadEnd)
            } catch (e: Exception) {
                Log.e(TAG, "Database query failed", e)
                // Retry on database errors as they may be transient
                return Result.retry()
            }

            // Defensive: validate task list is not null/empty before processing
            if (tasks.isEmpty()) {
                Log.d(TAG, "No upcoming tasks found")
                return Result.success()
            }

            // Process each task with defensive checks
            var notificationsSent = 0
            tasks.forEach { task ->
                try {
                    // Defensive: validate task data before processing
                    if (!DeadlineHelper.isValidForNotification(task)) {
                        Log.w(TAG, "Skipping invalid task: id=${task.id}")
                        return@forEach
                    }

                    val urgency = DeadlineHelper.determineUrgency(task.dueAtMillis, now)

                    if (urgency != null) {
                        NotificationHelper.showDeadlineNotification(
                            context = applicationContext,
                            taskId = task.id,
                            title = task.title,
                            message = "$urgency: ${task.description.ifBlank { "Open app for details" }}"
                        )
                        notificationsSent++
                    }
                } catch (e: Exception) {
                    // Log but continue processing other tasks
                    Log.e(TAG, "Failed to process task id=${task.id}", e)
                }
            }

            Log.d(TAG, "Worker completed successfully: sent $notificationsSent notifications")
            Result.success()
        } catch (e: SecurityException) {
            // Non-recoverable: notification permission denied
            Log.e(TAG, "Security exception: notification permission denied", e)
            Result.failure()
        } catch (e: Exception) {
            // Unexpected error: retry with backoff
            Log.e(TAG, "Unexpected worker failure", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "DeadlineNotificationWorker"
    }
}
