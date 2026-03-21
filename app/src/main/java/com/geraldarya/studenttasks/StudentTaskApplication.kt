package com.geraldarya.studenttasks

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.geraldarya.studenttasks.constants.ReminderConstants
import com.geraldarya.studenttasks.data.AppDatabase
import com.geraldarya.studenttasks.data.TaskRepository
import com.geraldarya.studenttasks.worker.DeadlineNotificationWorker
import java.util.concurrent.TimeUnit

class StudentTaskApplication : Application() {
    lateinit var repository: TaskRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = TaskRepository(db.taskDao())
        scheduleDeadlineChecks()
    }

    private fun scheduleDeadlineChecks() {
        val request = PeriodicWorkRequestBuilder<DeadlineNotificationWorker>(
            ReminderConstants.WORKER_INTERVAL_HOURS,
            TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ReminderConstants.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
