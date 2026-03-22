package com.geraldarya.studenttasks.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.geraldarya.studenttasks.data.TaskEntity
import com.geraldarya.studenttasks.data.TaskRepository
import com.geraldarya.studenttasks.domain.TaskPriority
import com.geraldarya.studenttasks.domain.TaskStatus
import com.geraldarya.studenttasks.domain.TaskTag
import com.geraldarya.studenttasks.domain.SortOrder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskUiState(
    val tasks: List<TaskEntity> = emptyList(),
    val filterTag: TaskTag? = null,
    val sortOrder: SortOrder = SortOrder.DUE_DATE
)

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    private val filterTag = MutableStateFlow<TaskTag?>(null)
    private val sortOrder = MutableStateFlow(SortOrder.DUE_DATE)

    val uiState: StateFlow<TaskUiState> = sortOrder.flatMapLatest { sort ->
        val tasksFlow = when (sort) {
            SortOrder.DUE_DATE -> repository.observeTasks()
            SortOrder.PRIORITY -> repository.observeTasksByPriority()
            SortOrder.CREATED_DATE -> repository.observeTasksByCreatedDate()
        }
        combine(tasksFlow, filterTag) { tasks, selectedTag ->
            val filtered = if (selectedTag == null) tasks else tasks.filter { it.tag == selectedTag }
            TaskUiState(tasks = filtered, filterTag = selectedTag, sortOrder = sort)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskUiState())

    fun setFilter(tag: TaskTag?) {
        filterTag.value = tag
    }

    fun setSortOrder(order: SortOrder) {
        sortOrder.value = order
    }

    fun saveTask(
        id: Long,
        title: String,
        description: String,
        dueAtMillis: Long,
        tag: TaskTag,
        priority: TaskPriority,
        status: TaskStatus
    ) {
        viewModelScope.launch {
            repository.save(
                TaskEntity(
                    id = id,
                    title = title,
                    description = description,
                    dueAtMillis = dueAtMillis,
                    tag = tag,
                    priority = priority,
                    status = status
                )
            )
        }
    }

    fun updateStatus(task: TaskEntity, status: TaskStatus) {
        viewModelScope.launch {
            repository.save(task.copy(status = status))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }

    suspend fun getTask(id: Long): TaskEntity? {
        return repository.getTask(id)
    }
}

class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
