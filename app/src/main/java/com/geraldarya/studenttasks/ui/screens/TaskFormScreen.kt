package com.geraldarya.studenttasks.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.geraldarya.studenttasks.domain.TaskPriority
import com.geraldarya.studenttasks.domain.TaskStatus
import com.geraldarya.studenttasks.domain.TaskTag
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    onSave: (
        id: Long,
        title: String,
        description: String,
        dueAtMillis: Long,
        tag: TaskTag,
        priority: TaskPriority,
        status: TaskStatus
    ) -> Unit,
    onBack: () -> Unit,
    editingTaskId: Long = 0,
    editingTaskTitle: String = "",
    editingTaskDescription: String = "",
    editingTaskDueAtMillis: Long = 0,
    editingTaskTag: TaskTag = TaskTag.COURSEWORK,
    editingTaskPriority: TaskPriority = TaskPriority.MEDIUM,
    editingTaskStatus: TaskStatus = TaskStatus.TODO
) {
    val context = LocalContext.current
    val isEditMode = editingTaskId != 0L

    val title = remember { mutableStateOf(if (isEditMode) editingTaskTitle else "") }
    val description = remember { mutableStateOf(if (isEditMode) editingTaskDescription else "") }
    val dueDateTime = remember {
        mutableStateOf(
            if (isEditMode && editingTaskDueAtMillis > 0) {
                LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(editingTaskDueAtMillis),
                    ZoneId.systemDefault()
                )
            } else {
                LocalDateTime.now().plusDays(1)
            }
        )
    }
    val selectedTag = remember { mutableStateOf(if (isEditMode) editingTaskTag else TaskTag.COURSEWORK) }
    val selectedPriority = remember { mutableStateOf(if (isEditMode) editingTaskPriority else TaskPriority.MEDIUM) }
    val selectedStatus = remember { mutableStateOf(if (isEditMode) editingTaskStatus else TaskStatus.TODO) }

    // Validation state
    val titleError = remember { mutableStateOf<String?>(null) }
    val dateError = remember { mutableStateOf<String?>(null) }

    // Validation functions
    fun validateTitle(): Boolean {
        return when {
            title.value.isBlank() -> {
                titleError.value = "Title cannot be empty"
                false
            }
            title.value.trim().length < 3 -> {
                titleError.value = "Title must be at least 3 characters"
                false
            }
            else -> {
                titleError.value = null
                true
            }
        }
    }

    fun validateDate(): Boolean {
        val dueMillis = dueDateTime.value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()
        return when {
            !isEditMode && dueMillis < now -> {
                // Only validate past dates for new tasks, not when editing
                dateError.value = "Due date cannot be in the past"
                false
            }
            else -> {
                dateError.value = null
                true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (isEditMode) "Edit task" else "Create task",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = title.value,
            onValueChange = {
                title.value = it
                // Clear error on change
                if (titleError.value != null) {
                    titleError.value = null
                }
            },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            isError = titleError.value != null,
            supportingText = {
                titleError.value?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        OutlinedTextField(
            value = description.value,
            onValueChange = { description.value = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Button(onClick = {
            val current = dueDateTime.value
            DatePickerDialog(
                context,
                { _, y, m, d ->
                    dueDateTime.value = dueDateTime.value.withYear(y).withMonth(m + 1).withDayOfMonth(d)
                    // Clear date error when user changes date
                    if (dateError.value != null) {
                        dateError.value = null
                    }
                },
                current.year,
                current.monthValue - 1,
                current.dayOfMonth
            ).show()
        }) {
            Text("Pick date")
        }

        Button(onClick = {
            val current = dueDateTime.value
            TimePickerDialog(
                context,
                { _, h, min ->
                    dueDateTime.value = dueDateTime.value.withHour(h).withMinute(min)
                    // Clear date error when user changes time
                    if (dateError.value != null) {
                        dateError.value = null
                    }
                },
                current.hour,
                current.minute,
                true
            ).show()
        }) {
            Text("Pick time")
        }

        Text(
            "Due: ${dueDateTime.value.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm"))}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (dateError.value != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )

        dateError.value?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        EnumDropdown(
            title = "Tag",
            value = selectedTag.value.name.replace('_', ' '),
            options = TaskTag.entries.map { it.name.replace('_', ' ') },
            onSelected = { selectedTag.value = TaskTag.valueOf(it.replace(' ', '_')) }
        )

        EnumDropdown(
            title = "Priority",
            value = selectedPriority.value.name,
            options = TaskPriority.entries.map { it.name },
            onSelected = { selectedPriority.value = TaskPriority.valueOf(it) }
        )

        EnumDropdown(
            title = "Status",
            value = selectedStatus.value.name.replace('_', ' '),
            options = TaskStatus.entries.map { it.name.replace('_', ' ') },
            onSelected = { selectedStatus.value = TaskStatus.valueOf(it.replace(' ', '_')) }
        )

        Button(
            onClick = {
                // Validate all fields
                val isTitleValid = validateTitle()
                val isDateValid = validateDate()

                // Only save if all validation passes
                if (isTitleValid && isDateValid) {
                    val dueMillis = dueDateTime.value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    onSave(
                        editingTaskId, // Use the editing task ID (0 for new tasks)
                        title.value.trim(),
                        description.value.trim(),
                        dueMillis,
                        selectedTag.value,
                        selectedPriority.value,
                        selectedStatus.value
                    )
                    onBack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save task")
        }

        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Back")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnumDropdown(
    title: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    val expanded = remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded.value, onExpandedChange = { expanded.value = !expanded.value }) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(title) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded.value = false
                    }
                )
            }
        }
    }
}
