package com.geraldarya.studenttasks.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.geraldarya.studenttasks.data.TaskEntity
import com.geraldarya.studenttasks.data.TaskRepository
import com.geraldarya.studenttasks.domain.TaskPriority
import com.geraldarya.studenttasks.domain.TaskStatus
import com.geraldarya.studenttasks.domain.TaskTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
 * - Sorting tasks by due date
 * - Saving new and existing tasks
 * - Updating task status
 * - Deleting tasks
 * - State flow emission and collection
 */
@ExperimentalCoroutinesApi
class TaskViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var testDispatcher: UnconfinedTestDispatcher
    private lateinit var mockRepository: TaskRepository
    private lateinit var viewModel: TaskViewModel
    private lateinit var taskFlow: MutableStateFlow<List<TaskEntity>>
    private lateinit var collectorJob: Job

    @Before
    fun setup() {
        testDispatcher = UnconfinedTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        mockRepository = mock()
        taskFlow = MutableStateFlow(emptyList())
        whenever(mockRepository.observeTasks()).thenReturn(taskFlow)
        viewModel = TaskViewModel(mockRepository)
        // Start collecting uiState to trigger upstream flow collection
        collectorJob = testDispatcher.launch {
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
        // Given: tasks with different due dates (unsorted)
        val tasks = listOf(
            TaskEntity(
                id = 1L,
                title = "Latest",
                description = "Due last",
                dueAtMillis = 5000L,
                tag = TaskTag.PERSONAL,
                priority = TaskPriority.LOW,
                status = TaskStatus.TODO
            ),
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
        // Given: tasks with different tags and due dates
        val tasks = listOf(
            TaskEntity(
                id = 1L,
                title = "Coursework Late",
                description = "Due last",
                dueAtMillis = 9000L,
                tag = TaskTag.COURSEWORK,
                priority = TaskPriority.LOW,
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
                id = 3L,
                title = "Coursework Early",
                description = "Due first",
                dueAtMillis = 1000L,
                tag = TaskTag.COURSEWORK,
                priority = TaskPriority.MEDIUM,
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
}
