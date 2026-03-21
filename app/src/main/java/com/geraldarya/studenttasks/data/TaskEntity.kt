package com.geraldarya.studenttasks.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.geraldarya.studenttasks.domain.TaskPriority
import com.geraldarya.studenttasks.domain.TaskStatus
import com.geraldarya.studenttasks.domain.TaskTag

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val dueAtMillis: Long,
    val tag: TaskTag,
    val priority: TaskPriority,
    val status: TaskStatus,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val lastNotifiedAtMillis: Long = 0
)
