package com.example.teacherscheduler.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.teacherscheduler.model.ToDo
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*
import com.example.teacherscheduler.viewmodel.ToDoViewModel
import com.example.teacherscheduler.viewmodel.ToDosData
import com.example.teacherscheduler.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDosScreen(
    viewModel: ToDoViewModel = hiltViewModel(),
    onToDoClick: (Long) -> Unit,
    onAddToDo: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val (todos, pendingCount, overdueCount) = when (uiState) {
        is UiState.Success -> {
            val data = (uiState as UiState.Success<ToDosData>).data
            Triple(data.todos, data.pendingCount, data.overdueCount)
        }
        else -> Triple(emptyList(), 0, 0)
    }

    Scaffold(
        containerColor = BackgroundPrimary,
        floatingActionButton = {
            IconCircleButton(
                icon = Icons.Default.Add,
                onClick = onAddToDo,
                contentDescription = "Add Task",
                size = 56.dp
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 32.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Pending",
                        count = pendingCount,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Overdue",
                        count = overdueCount,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (todos.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.CheckCircle,
                        title = "No tasks yet",
                        subtitle = "Add your first task to get started",
                        actionText = "Add Task",
                        onActionClick = onAddToDo
                    )
                }
            } else {
                item {
                    Text(
                        text = "All Tasks (${todos.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextPrimary
                    )
                }

                items(
                    items = todos,
                    key = { it.id }
                ) { todo ->
                    ToDoCard(
                        todo = todo,
                        onToggle = { viewModel.toggleCompletion(todo.id, !todo.isCompleted) },
                        onClick = { onToDoClick(todo.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    PremiumCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = TextPrimary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ToDoCard(
    todo: ToDo,
    onToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFFD8B4A0),
                    uncheckedColor = Color(0xFFECE6DF)
                )
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null
                    ),
                    color = if (todo.isCompleted) TextSecondary else TextPrimary
                )
                
                if (todo.description.isNotEmpty()) {
                    Text(
                        text = todo.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (todo.dueDate != null) {
                        val isOverdue = !todo.isCompleted && todo.dueDate.time < System.currentTimeMillis()
                        Text(
                            text = formatDate(todo.dueDate),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isOverdue) Color(0xFFE57373) else TextSecondary
                        )
                    }
                    
                    Text(
                        text = todo.priority.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (todo.priority) {
                            ToDo.Priority.URGENT -> Color(0xFFE57373)
                            ToDo.Priority.HIGH -> Color(0xFFF5C06D)
                            else -> TextSecondary
                        }
                    )
                }
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val format = SimpleDateFormat("MMM d", Locale.getDefault())
    return format.format(date)
}
