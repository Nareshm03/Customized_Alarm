package com.example.teacherscheduler.features.department.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teacherscheduler.features.department.model.DepartmentTask
import com.example.teacherscheduler.features.department.model.TaskStatus
import com.example.teacherscheduler.ui.theme.AppElevation
import com.example.teacherscheduler.ui.theme.AppSpacing
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDepartmentTasksScreen(
    teacherId: String,
    viewModel: TeacherTasksViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.tasks.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is TasksUiState.Success) {
            viewModel.resetUiState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Department Tasks") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (tasks.isEmpty()) {
                EmptyTasksState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks, key = { it.taskId }) { task ->
                        TaskCard(
                            task = task,
                            teacherId = teacherId,
                            onMarkInProgress = { viewModel.markInProgress(task.taskId) },
                            onMarkCompleted = { viewModel.markCompleted(task.taskId) },
                            isLoading = uiState is TasksUiState.Loading
                        )
                    }
                }
            }

            if (uiState is TasksUiState.Error) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text((uiState as TasksUiState.Error).message)
                }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: DepartmentTask,
    teacherId: String,
    onMarkInProgress: () -> Unit,
    onMarkCompleted: () -> Unit,
    isLoading: Boolean
) {
    val status = task.getTeacherStatus(teacherId)
    val isOverdue = task.isOverdue() && status != TaskStatus.COMPLETED
    val cardColor = if (isOverdue) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.level1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (task.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                PriorityChip(task.priority.name)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusChip(status)
                DeadlineText(task.deadline, isOverdue)
            }

            if (status != TaskStatus.COMPLETED) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (status == TaskStatus.ASSIGNED) {
                        Button(
                            onClick = onMarkInProgress,
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start")
                        }
                    }
                    Button(
                        onClick = onMarkCompleted,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Complete")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: TaskStatus) {
    val (color, text) = when (status) {
        TaskStatus.ASSIGNED -> Color(0xFF2196F3) to "Assigned"
        TaskStatus.IN_PROGRESS -> Color(0xFFFF9800) to "In Progress"
        TaskStatus.COMPLETED -> Color(0xFF4CAF50) to "Completed"
        TaskStatus.OVERDUE -> Color(0xFFF44336) to "Overdue"
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PriorityChip(priority: String) {
    val color = when (priority) {
        "LOW" -> Color(0xFF4CAF50)
        "MEDIUM" -> Color(0xFFFF9800)
        "HIGH" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.2f)
    ) {
        Text(
            text = priority,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun DeadlineText(deadline: Long, isOverdue: Boolean) {
    val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
    val countdown = getCountdown(deadline)
    val color = if (isOverdue) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = dateFormat.format(Date(deadline)),
                style = MaterialTheme.typography.bodySmall,
                color = color
            )
        }
        Text(
            text = countdown,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyTasksState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Assignment,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tasks assigned",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "You're all caught up!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

private fun getCountdown(deadline: Long): String {
    val now = System.currentTimeMillis()
    val diff = deadline - now

    if (diff < 0) {
        val overdueDiff = -diff
        val days = TimeUnit.MILLISECONDS.toDays(overdueDiff)
        val hours = TimeUnit.MILLISECONDS.toHours(overdueDiff) % 24
        return when {
            days > 0 -> "Overdue by $days day${if (days > 1) "s" else ""}"
            hours > 0 -> "Overdue by $hours hour${if (hours > 1) "s" else ""}"
            else -> "Overdue"
        }
    }

    val days = TimeUnit.MILLISECONDS.toDays(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60

    return when {
        days > 0 -> "Due in $days day${if (days > 1) "s" else ""}"
        hours > 0 -> "Due in $hours hour${if (hours > 1) "s" else ""}"
        minutes > 0 -> "Due in $minutes min${if (minutes > 1) "s" else ""}"
        else -> "Due now"
    }
}
