package com.geraldarya.studenttasks.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.geraldarya.studenttasks.data.AppDatabase
import com.geraldarya.studenttasks.notifications.NotificationHelper

class DeadlineNotificationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val repository = AppDatabase.getInstance(applicationContext).taskDao()
        val now = System.currentTimeMillis()
        val threeDays = now + THREE_DAYS_MILLIS
        val tasks = repository.getUpcomingNotDone(now, threeDays)

        tasks.forEach { task ->
            val remaining = task.dueAtMillis - now
            val urgency = when {
                remaining <= DAY_MILLIS -> "Due within 24 hours"
                remaining <= THREE_DAYS_MILLIS -> "Due within 3 days"
                else -> null
            }

            if (urgency != null) {
                NotificationHelper.showDeadlineNotification(
                    context = applicationContext,
                    taskId = task.id,
                    title = task.title,
                    message = "$urgency: ${task.description.ifBlank { "Open app for details" }}"
                )
            }
        }

        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "deadline_check_worker"
        private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
        private const val THREE_DAYS_MILLIS = 3L * DAY_MILLIS
    }
}
