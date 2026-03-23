package com.geraldarya.studenttasks.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.geraldarya.studenttasks.data.TaskEntity
import com.geraldarya.studenttasks.data.TaskRepository
import com.geraldarya.studenttasks.domain.SortOrder
import com.geraldarya.studenttasks.domain.TaskPriority
import com.geraldarya.studenttasks.domain.TaskStatus
import com.geraldarya.studenttasks.domain.TaskTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for TaskViewModel.
 *
 * Tests view model behavior for:
 * - Filtering tasks by tag
 * - Sorting tasks by due date, priority, and created date (M4)
 * - Saving new and existing tasks
 * - Editing existing tasks and preserving metadata (M4)
 * - Updating task status
 * - Deleting tasks
 * - State flow emission and collection
 * - Combining sort and filter operations
 */
@ExperimentalCoroutinesApi
class TaskViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var testDispatcher: TestDispatcher
    private lateinit var mockRepository: TaskRepository
    private lateinit var viewModel: TaskViewModel
    private lateinit var taskFlow: MutableStateFlow<List<TaskEntity>>
    private lateinit var tasksByPriorityFlow: MutableStateFlow<List<TaskEntity>>
    private lateinit var tasksByCreatedDateFlow: MutableStateFlow<List<TaskEntity>>
    private lateinit var collectorJob: Job

    @Before
    fun setup() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        mockRepository = mock()
        taskFlow = MutableStateFlow(emptyList())
        tasksByPriorityFlow = MutableStateFlow(emptyList())
        tasksByCreatedDateFlow = MutableStateFlow(emptyList())
        whenever(mockRepository.observeTasks()).thenReturn(taskFlow)
        whenever(mockRepository.observeTasksByPriority()).thenReturn(tasksByPriorityFlow)
        whenever(mockRepository.observeTasksByCreatedDate()).thenReturn(tasksByCreatedDateFlow)
        kotlinx.coroutines.runBlocking { whenever(mockRepository.save(any())).thenReturn(1L) }
        viewModel = TaskViewModel(mockRepository)
        // Start collecting uiState to trigger upstream flow collection
        collectorJob = CoroutineScope(testDispatcher).launch {
            viewModel.uiState.collect {}
        }
    }

    @After
    fun tearDown() {
        collectorJob.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState_IsEmpty() = runTest {
        // Given: freshly created view model

        // When: observing initial state
        val state = viewModel.uiState.value

        // Then: state is empty with no filter
        assertTrue(state.tasks.isEmpty())
        assertNull(state.filterTag)
    }

    @Test
    fun testFilterByTag_OnlyShowsMatchingTasks() = runTest {
        // Given: tasks with different tags
        val tasks = listOf(
            TaskEntity(
                id = 1L,
                title = "Coursework Task",
                description = "Assignment",
                dueAtMillis = 1000L,
                tag = TaskTag.COURSEWORK,
                priority = TaskPriority.HIGH,
                status = TaskStatus.TODO
            ),
            TaskEntity(
                id = 2L,
                title = "Lab Task",
                description = "Research",
                dueAtMillis = 2000L,
                tag = TaskTag.LAB_RESEARCH,
                priority = TaskPriority.MEDIUM,
                status = TaskStatus.TODO
            ),
            TaskEntity(
                id = 3L,
                title = "Another Coursework",
                description = "Essay",
                dueAtMillis = 3000L,
                tag = TaskTag.COURSEWORK,
                priority = TaskPriority.LOW,
                status = TaskStatus.IN_PROGRESS
            )
        )
        taskFlow.value = tasks

        // When: filtering by COURSEWORK tag
        viewModel.setFilter(TaskTag.COURSEWORK)

        // Then: only COURSEWORK tasks are shown
        val state = viewModel.uiState.value
        assertEquals(2, state.tasks.size)
        assertTrue(state.tasks.all { it.tag == TaskTag.COURSEWORK })
        assertEquals(TaskTag.COURSEWORK, state.filterTag)
    }

    @Test
    fun testFilterNull_ShowsAllTasks() = runTest {
        // Given: tasks with different tags and a filter applied
        val tasks = listOf(
            TaskEntity(
                id = 1L,
                title = "Task 1",
                description = "Desc 1",
                dueAtMillis = 1000L,
                tag = TaskTag.EXAMS,
                priority = TaskPriority.HIGH,
                status = TaskStatus.TODO
            ),
            TaskEntity(
                id = 2L,
                title = "Task 2",
                description = "Desc 2",
                dueAtMillis = 2000L,
                tag = TaskTag.THESIS,
                priority = TaskPriority.MEDIUM,
                status = TaskStatus.TODO
            )
        )
        taskFlow.value = tasks
        viewModel.setFilter(TaskTag.EXAMS)

        // When: clearing the filter (set to null)
        viewModel.setFilter(null)

        // Then: all tasks are shown
        val state = viewModel.uiState.value
        assertEquals(2, state.tasks.size)
        assertNull(state.filterTag)
    }

    @Test
    fun testTasksSortedByDueDate_Ascending() = runTest {
        // Given: tasks sorted by due date ascending (as repository would return them)
        val tasks = listOf(
            TaskEntity(
                id = 2L,
                title = "Earliest",
                description = "Due first",
                dueAtMillis = 1000L,
                tag = TaskTag.PERSONAL,
                priority = TaskPriority.HIGH,
                status = TaskStatus.TODO
            ),
            TaskEntity(
                id = 3L,
                title = "Middle",
                description = "Due second",
                dueAtMillis = 3000L,
                tag = TaskTag.PERSONAL,
                priority = TaskPriority.MEDIUM,
                status = TaskStatus.TODO
            ),
            TaskEntity(
                id = 1L,
                title = "Latest",
                description = "Due last",
                dueAtMillis = 5000L,
                tag = TaskTag.PERSONAL,
                priority = TaskPriority.LOW,
                status = TaskStatus.TODO
            )
        )
        taskFlow.value = tasks

        // When: observing state
        val state = viewModel.uiState.value

        // Then: tasks are sorted by due date ascending
        assertEquals(3, state.tasks.size)
        assertEquals(1000L, state.tasks[0].dueAtMillis)
        assertEquals(3000L, state.tasks[1].dueAtMillis)
        assertEquals(5000L, state.tasks[2].dueAtMillis)
    }

    @Test
    fun testSaveTask_CallsRepositorySave() = runTest {
        // Given: task data for a new task
        val title = "New Task"
        val description = "Task description"
        val dueAtMillis = 12345L
        val tag = TaskTag.COURSEWORK
        val priority = TaskPriority.HIGH
        val status = TaskStatus.TODO

        // When: saving the task
        viewModel.saveTask(
            id = 0L,
            title = title,
            description = description,
            dueAtMillis = dueAtMillis,
            tag = tag,
            priority = priority,
            status = status
        )
        testScheduler.runCurrent()

        // Then: repository save is called with correct entity
        val captor = argumentCaptor<TaskEntity>()
        verify(mockRepository).save(captor.capture())

        val savedTask = captor.firstValue
        assertEquals(0L, savedTask.id)
        assertEquals(title, savedTask.title)
        assertEquals(description, savedTask.description)
        assertEquals(dueAtMillis, savedTask.dueAtMillis)
        assertEquals(tag, savedTask.tag)
        assertEquals(priority, savedTask.priority)
        assertEquals(status, savedTask.status)
    }

    @Test
    fun testSaveTask_UpdatesExistingTask() = runTest {
        // Given: task data for an existing task
        val existingId = 42L
        val title = "Updated Task"
        val description = "Updated description"
        val dueAtMillis = 99999L
        val tag = TaskTag.THESIS
        val priority = TaskPriority.MEDIUM
        val status = TaskStatus.IN_PROGRESS

        // When: saving the task with existing ID
        viewModel.saveTask(
            id = existingId,
            title = title,
            description = description,
            dueAtMillis = dueAtMillis,
            tag = tag,
            priority = priority,
            status = status
        )
        testScheduler.runCurrent()

        // Then: repository save is called with correct entity
        val captor = argumentCaptor<TaskEntity>()
        verify(mockRepository).save(captor.capture())

        val savedTask = captor.firstValue
        assertEquals(existingId, savedTask.id)
        assertEquals(title, savedTask.title)
        assertEquals(description, savedTask.description)
        assertEquals(dueAtMillis, savedTask.dueAtMillis)
        assertEquals(tag, savedTask.tag)
        assertEquals(priority, savedTask.priority)
        assertEquals(status, savedTask.status)
    }

    @Test
    fun testUpdateStatus_CallsRepositorySave() = runTest {
        // Given: an existing task and new status
        val task = TaskEntity(
            id = 10L,
            title = "Task to Update",
            description = "Description",
            dueAtMillis = 5000L,
            tag = TaskTag.LAB_RESEARCH,
            priority = TaskPriority.HIGH,
            status = TaskStatus.TODO
        )
        val newStatus = TaskStatus.DONE

        // When: updating the status
        viewModel.updateStatus(task, newStatus)
        testScheduler.runCurrent()

        // Then: repository save is called with updated status
        verify(mockRepository).save(task.copy(status = newStatus))
    }

    @Test
    fun testDeleteTask_CallsRepositoryDelete() = runTest {
        // Given: a task to delete
        val task = TaskEntity(
            id = 20L,
            title = "Task to Delete",
            description = "Will be removed",
            dueAtMillis = 8000L,
            tag = TaskTag.PERSONAL,
            priority = TaskPriority.LOW,
            status = TaskStatus.DONE
        )

        // When: deleting the task
        viewModel.deleteTask(task)
        testScheduler.runCurrent()

        // Then: repository delete is called
        verify(mockRepository).delete(task)
    }

    @Test
    fun testFilterWithSorting_BothAppliedCorrectly() = runTest {
        // Given: tasks sorted by due date ascending (as repository would return them)
        val tasks = listOf(
            TaskEntity(
                id = 3L,
                title = "Coursework Early",
                description = "Due first",
                dueAtMillis = 1000L,
                tag = TaskTag.COURSEWORK,
                priority = TaskPriority.MEDIUM,
                status = TaskStatus.TODO
            ),
            TaskEntity(
                id = 2L,
                title = "Lab Task",
                description = "Different tag",
                dueAtMillis = 2000L,
                tag = TaskTag.LAB_RESEARCH,
                priority = TaskPriority.HIGH,
                status = TaskStatus.TODO
            ),
            TaskEntity(
                id = 1L,
                title = "Coursework Late",
                description = "Due last",
                dueAtMillis = 9000L,
                tag = TaskTag.COURSEWORK,
                priority = TaskPriority.LOW,
                status = TaskStatus.TODO
            )
        )
        taskFlow.value = tasks

        // When: filtering by COURSEWORK
        viewModel.setFilter(TaskTag.COURSEWORK)

        // Then: only COURSEWORK tasks shown, sorted by due date
        val state = viewModel.uiState.value
        assertEquals(2, state.tasks.size)
        assertEquals("Coursework Early", state.tasks[0].title)
        assertEquals("Coursework Late", state.tasks[1].title)
        assertEquals(1000L, state.tasks[0].dueAtMillis)
        assertEquals(9000L, state.tasks[1].dueAtMillis)
    }

    @Test
    fun testEmptyFilteredResult() = runTest {
        // Given: tasks that don't match the filter
        val tasks = listOf(
            TaskEntity(
                id = 1L,
                title = "Exam Task",
                description = "Study",
                dueAtMillis = 1000L,
                tag = TaskTag.EXAMS,
                priority = TaskPriority.HIGH,
                status = TaskStatus.TODO
            )
        )
        taskFlow.value = tasks

        // When: filtering by a tag that doesn't exist in tasks
        viewModel.setFilter(TaskTag.THESIS)

        // Then: empty filtered result
        val state = viewModel.uiState.value
        assertTrue(state.tasks.isEmpty())
        assertEquals(TaskTag.THESIS, state.filterTag)
    }

    // M4: Sorting functionality tests

    @Test
    fun testSetSortOrder_UpdatesState() = runTest {
        // Given: initial state with default sort order
        assertEquals(SortOrder.DUE_DATE, viewModel.uiState.value.sortOrder)

        // When: changing sort order to PRIORITY
        viewModel.setSortOrder(SortOrder.PRIORITY)

        // Then: sort order is updated in state
        assertEquals(SortOrder.PRIORITY, viewModel.uiState.value.sortOrder)
    }

    @Test
    fun testSortByPriority_UsesCorrectRepositoryFlow() = runTest {
        // Given: tasks in priority flow
        val tasks = listOf(
            TaskEntity(
                id = 1L,
                title = "High Priority",
                description = "Important",
                dueAtMillis = 5000L,
                tag = TaskTag.COURSEWORK,
                priority = TaskPriority.HIGH,
                status = TaskStatus.TODO
            ),
            TaskEntity(
                id = 2L,
                title = "Medium Priority",
                description = "Normal",
                dueAtMillis = 3000L,
                tag = TaskTag.COURSEWORK,
                priority = TaskPriority.MEDIUM,
                status = TaskStatus.TODO
            ),
            TaskEntity(
                id = 3L,
                title = "Low Priority",
                description = "Can wait",
                dueAtMillis = 1000L,
                tag = TaskTag.COURSEWORK,
                priority = TaskPriority.LOW,
                status = TaskStatus.TODO
            )
        )
        tasksByPriorityFlow.value = tasks

        // When: setting sort order to PRIORITY
        viewModel.setSortOrder(SortOrder.PRIORITY)

        // Then: tasks from priority flow are shown
        val state = viewModel.uiState.value
        assertEquals(3, state.tasks.size)
        assertEquals(SortOrder.PRIORITY, state.sortOrder)
        assertEquals("High Priority", state.tasks[0].title)
        assertEquals("Medium Priority", state.tasks[1].title)
        assertEquals("Low Priority", state.tasks[2].title)
    }

    @Test
    fun testSortByCreatedDate_UsesCorrectRepositoryFlow() = runTest {
        // Given: tasks in created date flow (newest first)
        val tasks = listOf(
            TaskEntity(
                id = 1L,
                title = "Newest Task",
                description = "Created last",
                dueAtMillis = 5000L,
                tag = TaskTag.LAB_RESEARCH,
                priority = TaskPriority.MEDIUM,
                status = TaskStatus.TODO,
                createdAtMillis = 3000L
            ),
            TaskEntity(
                id = 2L,
                title = "Middle Task",
                description = "Created second",
                dueAtMillis = 3000L,
                tag = TaskTag.LAB_RESEARCH,
                priority = TaskPriority.MEDIUM,
                status = TaskStatus.TODO,
                createdAtMillis = 2000L
            ),
            TaskEntity(
                id = 3L,
                title = "Oldest Task",
                description = "Created first",
                dueAtMillis = 1000L,
                tag = TaskTag.LAB_RESEARCH,
                priority = TaskPriority.MEDIUM,
                status = TaskStatus.TODO,
                createdAtMillis = 1000L
            )
        )
        tasksByCreatedDateFlow.value = tasks

        // When: setting sort order to CREATED_DATE
        viewModel.setSortOrder(SortOrder.CREATED_DATE)

        // Then: tasks from created date flow are shown (newest first)
        val state = viewModel.uiState.value
        assertEquals(3, state.tasks.size)
        assertEquals(SortOrder.CREATED_DATE, state.sortOrder)
        assertEquals("Newest Task", state.tasks[0].title)
        assertEquals("Middle Task", state.tasks[1].title)
        assertEquals("Oldest Task", state.tasks[2].title)
    }

    @Test
    fun testSortOrderWithFilter_BothApplied() = runTest {
        // Given: tasks in priority flow with different tags
        val tasks = listOf(
            TaskEntity(
                id = 1L,
                title = "High Coursework",
                description = "Important coursework",
                dueAtMillis = 5000L,
                tag = TaskTag.COURSEWORK,
                priority = TaskPriority.HIGH,
                status = TaskStatus.TODO
            ),
            TaskEntity(
                id = 2L,
                title = "High Lab",
                description = "Important lab",
                dueAtMillis = 3000L,
                tag = TaskTag.LAB_RESEARCH,
                priority = TaskPriority.HIGH,
                status = TaskStatus.TODO
            ),
            TaskEntity(
                id = 3L,
                title = "Medium Coursework",
                description = "Normal coursework",
                dueAtMillis = 1000L,
                tag = TaskTag.COURSEWORK,
                priority = TaskPriority.MEDIUM,
                status = TaskStatus.TODO
            )
        )
        tasksByPriorityFlow.value = tasks

        // When: setting sort to priority and filtering by COURSEWORK
        viewModel.setSortOrder(SortOrder.PRIORITY)
        viewModel.setFilter(TaskTag.COURSEWORK)

        // Then: only COURSEWORK tasks shown, sorted by priority
        val state = viewModel.uiState.value
        assertEquals(2, state.tasks.size)
        assertEquals(SortOrder.PRIORITY, state.sortOrder)
        assertEquals(TaskTag.COURSEWORK, state.filterTag)
        assertEquals("High Coursework", state.tasks[0].title)
        assertEquals("Medium Coursework", state.tasks[1].title)
    }

    @Test
    fun testSwitchingSortOrders_UpdatesFlowSource() = runTest {
        // Given: different tasks in each flow
        val dueDateTasks = listOf(
            TaskEntity(
                id = 1L,
                title = "Due Date Task",
                description = "Sorted by due",
                dueAtMillis = 1000L,
                tag = TaskTag.PERSONAL,
                priority = TaskPriority.LOW,
                status = TaskStatus.TODO
            )
        )
        val priorityTasks = listOf(
            TaskEntity(
                id = 2L,
                title = "Priority Task",
                description = "Sorted by priority",
                dueAtMillis = 5000L,
                tag = TaskTag.PERSONAL,
                priority = TaskPriority.HIGH,
                status = TaskStatus.TODO
            )
        )
        taskFlow.value = dueDateTasks
        tasksByPriorityFlow.value = priorityTasks

        // When: initially viewing due date sort
        var state = viewModel.uiState.value
        assertEquals("Due Date Task", state.tasks[0].title)
        assertEquals(SortOrder.DUE_DATE, state.sortOrder)

        // And: switching to priority sort
        viewModel.setSortOrder(SortOrder.PRIORITY)

        // Then: tasks from priority flow are now shown
        state = viewModel.uiState.value
        assertEquals("Priority Task", state.tasks[0].title)
        assertEquals(SortOrder.PRIORITY, state.sortOrder)
    }

    // M4: Task editing tests

    @Test
    fun testSaveExistingTask_PreservesMetadata() = runTest {
        // Given: an existing task with metadata
        val existingTask = TaskEntity(
            id = 99L,
            title = "Original Title",
            description = "Original Description",
            dueAtMillis = 5000L,
            tag = TaskTag.THESIS,
            priority = TaskPriority.HIGH,
            status = TaskStatus.TODO,
            createdAtMillis = 1000L,
            lastNotifiedAtMillis = 2000L
        )
        whenever(mockRepository.getTask(99L)).thenReturn(existingTask)

        // When: saving with updated values
        viewModel.saveTask(
            id = 99L,
            title = "Updated Title",
            description = "Updated Description",
            dueAtMillis = 10000L,
            tag = TaskTag.COURSEWORK,
            priority = TaskPriority.MEDIUM,
            status = TaskStatus.IN_PROGRESS
        )
        testScheduler.runCurrent()

        // Then: repository save is called with preserved metadata
        val captor = argumentCaptor<TaskEntity>()
        verify(mockRepository).save(captor.capture())

        val savedTask = captor.firstValue
        assertEquals(99L, savedTask.id)
        assertEquals("Updated Title", savedTask.title)
        assertEquals("Updated Description", savedTask.description)
        assertEquals(10000L, savedTask.dueAtMillis)
        assertEquals(TaskTag.COURSEWORK, savedTask.tag)
        assertEquals(TaskPriority.MEDIUM, savedTask.priority)
        assertEquals(TaskStatus.IN_PROGRESS, savedTask.status)
        // Verify metadata is preserved
        assertEquals(1000L, savedTask.createdAtMillis)
        assertEquals(2000L, savedTask.lastNotifiedAtMillis)
    }

    @Test
    fun testGetTask_ReturnsTaskFromRepository() = runTest {
        // Given: a task in the repository
        val task = TaskEntity(
            id = 123L,
            title = "Test Task",
            description = "Description",
            dueAtMillis = 5000L,
            tag = TaskTag.EXAMS,
            priority = TaskPriority.HIGH,
            status = TaskStatus.TODO
        )
        whenever(mockRepository.getTask(123L)).thenReturn(task)

        // When: getting the task
        val result = viewModel.getTask(123L)

        // Then: task is returned
        assertEquals(task, result)
        verify(mockRepository).getTask(123L)
    }

    @Test
    fun testGetTask_ReturnsNullWhenNotFound() = runTest {
        // Given: no task with given ID
        whenever(mockRepository.getTask(999L)).thenReturn(null)

        // When: getting the task
        val result = viewModel.getTask(999L)

        // Then: null is returned
        assertNull(result)
        verify(mockRepository).getTask(999L)
    }
}
