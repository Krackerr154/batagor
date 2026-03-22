package com.geraldarya.studenttasks.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.geraldarya.studenttasks.data.TaskEntity
import com.geraldarya.studenttasks.domain.TaskStatus
import com.geraldarya.studenttasks.ui.components.TaskCard
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    tasks: List<TaskEntity>,
    onStatusChanged: (TaskEntity, TaskStatus) -> Unit,
    onEdit: (TaskEntity) -> Unit = {}
) {
    val tasksByDate = tasks.groupBy {
        Instant.ofEpochMilli(it.dueAtMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    }
    val start = LocalDate.now().minusDays(7)
    val days = (0..180).map { start.plusDays(it.toLong()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Scrollable planner", style = MaterialTheme.typography.headlineSmall)
            Text("Browse the next 6 months", style = MaterialTheme.typography.bodyMedium)
        }

        items(days) { date ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")),
                    style = MaterialTheme.typography.titleSmall
                )
                val dayTasks = tasksByDate[date].orEmpty()
                if (dayTasks.isEmpty()) {
                    Text(
                        text = "No tasks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    dayTasks.forEach { task ->
                        TaskCard(
                            task = task,
                            onStatusChanged = onStatusChanged,
                            onDelete = {},
                            onEdit = onEdit
                        )
                    }
                }
            }
        }
    }
}
