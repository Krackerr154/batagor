package com.geraldarya.studenttasks.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {
    fun observeTasks(): Flow<List<TaskEntity>> = dao.observeAll()

    suspend fun getTask(id: Long): TaskEntity? = dao.getById(id)

    suspend fun save(task: TaskEntity): Long {
        return if (task.id == 0L) dao.insert(task) else {
            dao.update(task)
            task.id
        }
    }

    suspend fun delete(task: TaskEntity) = dao.delete(task)

    suspend fun getUpcomingNotDone(fromMillis: Long, toMillis: Long): List<TaskEntity> {
        return dao.getUpcomingNotDone(fromMillis, toMillis)
    }
}
