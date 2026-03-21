package com.geraldarya.studenttasks.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geraldarya.studenttasks.data.TaskEntity
import com.geraldarya.studenttasks.domain.TaskStatus
import com.geraldarya.studenttasks.domain.TaskTag
import com.geraldarya.studenttasks.ui.TaskUiState
import com.geraldarya.studenttasks.ui.components.TaskCard

@Composable
fun ListScreen(
    uiState: TaskUiState,
    onFilterChanged: (TaskTag?) -> Unit,
    onStatusChanged: (TaskEntity, TaskStatus) -> Unit,
    onDelete: (TaskEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Deadline-first list", style = MaterialTheme.typography.headlineSmall)
        TagFilterRow(selected = uiState.filterTag, onFilterChanged = onFilterChanged)

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.tasks, key = { it.id }) { task ->
                TaskCard(task = task, onStatusChanged = onStatusChanged, onDelete = onDelete)
            }
        }
    }
}

@Composable
private fun TagFilterRow(selected: TaskTag?, onFilterChanged: (TaskTag?) -> Unit) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        item {
            AssistChip(
                onClick = { onFilterChanged(null) },
                label = { Text("All") },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
        items(TaskTag.entries) { tag ->
            AssistChip(
                onClick = { onFilterChanged(tag) },
                label = { Text(tag.name.replace('_', ' ')) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = if (selected == tag) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow
                )
            )
        }
    }
}
