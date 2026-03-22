package com.geraldarya.studenttasks.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.geraldarya.studenttasks.constants.ReminderConstants
import com.geraldarya.studenttasks.data.TaskEntity
import com.geraldarya.studenttasks.domain.TaskStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TaskCard(
    task: TaskEntity,
    onStatusChanged: (TaskEntity, TaskStatus) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    onEdit: (TaskEntity) -> Unit
) {
    val dueColor = deadlineColor(task.dueAtMillis, task.status)
    val expanded = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(dueColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = dueColor)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (task.description.isNotBlank()) {
                Text(task.description, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                "Due: ${formatMillis(task.dueAtMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Tag: ${task.tag.name.replace('_', ' ')} | Priority: ${task.priority.name}",
                style = MaterialTheme.typography.bodySmall
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { expanded.value = true }) {
                    Text("Status: ${task.status.name.replace('_', ' ')}")
                }
                TextButton(onClick = { onEdit(task) }) { Text("Edit") }
                TextButton(onClick = { onDelete(task) }) { Text("Delete") }
            }
            DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
                TaskStatus.entries.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status.name.replace('_', ' ')) },
                        onClick = {
                            onStatusChanged(task, status)
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun deadlineColor(dueAtMillis: Long, status: TaskStatus) = when {
    status == TaskStatus.DONE -> MaterialTheme.colorScheme.surfaceContainerLow
    dueAtMillis <= System.currentTimeMillis() + ReminderConstants.URGENT_THRESHOLD_MILLIS -> MaterialTheme.colorScheme.errorContainer
    dueAtMillis <= System.currentTimeMillis() + ReminderConstants.LOOKAHEAD_WINDOW_MILLIS -> MaterialTheme.colorScheme.tertiaryContainer
    else -> MaterialTheme.colorScheme.surfaceContainerLowest
}

private fun formatMillis(millis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm")
    return Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(formatter)
}
