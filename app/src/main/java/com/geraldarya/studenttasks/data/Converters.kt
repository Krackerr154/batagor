package com.geraldarya.studenttasks.data

import androidx.room.TypeConverter
import com.geraldarya.studenttasks.domain.TaskPriority
import com.geraldarya.studenttasks.domain.TaskStatus
import com.geraldarya.studenttasks.domain.TaskTag

class Converters {
    @TypeConverter
    fun toTag(value: String): TaskTag = TaskTag.valueOf(value)

    @TypeConverter
    fun fromTag(tag: TaskTag): String = tag.name

    @TypeConverter
    fun toPriority(value: String): TaskPriority = TaskPriority.valueOf(value)

    @TypeConverter
    fun fromPriority(priority: TaskPriority): String = priority.name

    @TypeConverter
    fun toStatus(value: String): TaskStatus = TaskStatus.valueOf(value)

    @TypeConverter
    fun fromStatus(status: TaskStatus): String = status.name
}
