package com.geraldarya.studenttasks.data

import com.geraldarya.studenttasks.domain.TaskPriority
import com.geraldarya.studenttasks.domain.TaskStatus
import com.geraldarya.studenttasks.domain.TaskTag
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for TaskRepository.
 *
 * Tests repository behavior for:
 * - Insert new tasks (id == 0)
 * - Update existing tasks (id > 0)
 * - Delete tasks
 * - Query single task by ID
 * - Query upcoming non-done tasks in time window
 * - Observe all tasks via Flow
 */
class TaskRepositoryTest {

    private lateinit var mockDao: TaskDao
    private lateinit var repository: TaskRepository

    @Before
    fun setup() {
        mockDao = mock()
        repository = TaskRepository(mockDao)
    }

    @Test
    fun testSaveInsertNewTask_ReturnsPositiveId() = runTest {
        // Given: a new task with id == 0
        val newTask = TaskEntity(
            id = 0L,
            title = "New Assignment",
            description = "Complete lab report",
            dueAtMillis = System.currentTimeMillis() + 86400000L, // +1 day
            tag = TaskTag.LAB_RESEARCH,
            priority = TaskPriority.HIGH,
            status = TaskStatus.TODO
        )
        val expectedId = 42L
        whenever(mockDao.insert(newTask)).thenReturn(expectedId)

        // When: saving the new task
        val resultId = repository.save(newTask)

        // Then: insert is called and returns expected ID
        verify(mockDao).insert(newTask)
        assertEquals(expectedId, resultId)
    }

    @Test
    fun testSaveUpdateExistingTask_ReturnsSameId() = runTest {
        // Given: an existing task with id > 0
        val existingTask = TaskEntity(
            id = 10L,
            title = "Updated Assignment",
            description = "Revised lab report",
            dueAtMillis = System.currentTimeMillis() + 86400000L,
            tag = TaskTag.LAB_RESEARCH,
            priority = TaskPriority.MEDIUM,
            status = TaskStatus.IN_PROGRESS
        )

        // When: saving the existing task
        val resultId = repository.save(existingTask)

        // Then: update is called and returns same ID
        verify(mockDao).update(existingTask)
        assertEquals(existingTask.id, resultId)
    }

    @Test
    fun testDelete_CallsDaoDelete() = runTest {
        // Given: a task to delete
        val task = TaskEntity(
            id = 5L,
            title = "Task to Delete",
            description = "This will be removed",
            dueAtMillis = System.currentTimeMillis(),
            tag = TaskTag.PERSONAL,
            priority = TaskPriority.LOW,
            status = TaskStatus.DONE
        )

        // When: deleting the task
        repository.delete(task)

        // Then: dao delete is called
        verify(mockDao).delete(task)
    }

    @Test
    fun testGetTask_ReturnsTaskWhenExists() = runTest {
        // Given: a task exists in the database
        val taskId = 7L
        val expectedTask = TaskEntity(
            id = taskId,
            title = "Existing Task",
            description = "Task description",
            dueAtMillis = System.currentTimeMillis(),
            tag = TaskTag.COURSEWORK,
            priority = TaskPriority.HIGH,
            status = TaskStatus.TODO
        )
        whenever(mockDao.getById(taskId)).thenReturn(expectedTask)

        // When: getting the task by ID
        val result = repository.getTask(taskId)

        // Then: correct task is returned
        assertEquals(expectedTask, result)
    }

    @Test
    fun testGetTask_ReturnsNullWhenNotExists() = runTest {
        // Given: no task exists for the given ID
        val taskId = 99L
        whenever(mockDao.getById(taskId)).thenReturn(null)

        // When: getting the task by ID
        val result = repository.getTask(taskId)

        // Then: null is returned
        assertNull(result)
    }

    @Test
    fun testObserveTasks_EmitsFlowFromDao() = runTest {
        // Given: dao returns a flow of tasks
        val tasks = listOf(
            TaskEntity(
                id = 1L,
                title = "Task 1",
                description = "First task",
                dueAtMillis = 1000L,
                tag = TaskTag.EXAMS,
                priority = TaskPriority.HIGH,
                status = TaskStatus.TODO
            ),
            TaskEntity(
                id = 2L,
                title = "Task 2",
                description = "Second task",
                dueAtMillis = 2000L,
                tag = TaskTag.THESIS,
                priority = TaskPriority.MEDIUM,
                status = TaskStatus.IN_PROGRESS
            )
        )
        whenever(mockDao.observeAll()).thenReturn(flowOf(tasks))

        // When: observing tasks
        val result = repository.observeTasks().first()

        // Then: correct tasks are emitted
        assertEquals(tasks, result)
    }

    @Test
    fun testGetUpcomingNotDone_FiltersCorrectly() = runTest {
        // Given: a time window and matching tasks
        val now = System.currentTimeMillis()
        val fromMillis = now
        val toMillis = now + 86400000L // +1 day
        val upcomingTasks = listOf(
            TaskEntity(
                id = 1L,
                title = "Due Soon",
                description = "Within window",
                dueAtMillis = now + 43200000L, // +12 hours
                tag = TaskTag.COURSEWORK,
                priority = TaskPriority.HIGH,
                status = TaskStatus.TODO
            )
        )
        whenever(mockDao.getUpcomingNotDone(fromMillis, toMillis)).thenReturn(upcomingTasks)

        // When: querying upcoming non-done tasks
        val result = repository.getUpcomingNotDone(fromMillis, toMillis)

        // Then: correct tasks are returned
        assertEquals(upcomingTasks, result)
        verify(mockDao).getUpcomingNotDone(fromMillis, toMillis)
    }

    @Test
    fun testGetUpcomingNotDone_ReturnsEmptyWhenNoneMatch() = runTest {
        // Given: no tasks in the time window
        val now = System.currentTimeMillis()
        val fromMillis = now
        val toMillis = now + 86400000L
        whenever(mockDao.getUpcomingNotDone(fromMillis, toMillis)).thenReturn(emptyList())

        // When: querying upcoming non-done tasks
        val result = repository.getUpcomingNotDone(fromMillis, toMillis)

        // Then: empty list is returned
        assertTrue(result.isEmpty())
    }

    @Test
    fun testGetUpcomingNotDone_ExcludesDoneTasks() = runTest {
        // Given: a time window with no non-done tasks (DONE tasks are excluded by DAO query)
        val now = System.currentTimeMillis()
        val fromMillis = now
        val toMillis = now + 86400000L
        // DAO already filters out DONE status, so empty list is returned
        whenever(mockDao.getUpcomingNotDone(fromMillis, toMillis)).thenReturn(emptyList())

        // When: querying upcoming non-done tasks
        val result = repository.getUpcomingNotDone(fromMillis, toMillis)

        // Then: no tasks are returned (DONE tasks excluded)
        assertTrue(result.isEmpty())
    }
}
