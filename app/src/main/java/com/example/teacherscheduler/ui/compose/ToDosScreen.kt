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
import com.example.teacherscheduler.viewmodel.UserViewModel
import com.example.teacherscheduler.viewmodel.ToDosData
import com.example.teacherscheduler.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToDosScreen(
    viewModel: ToDoViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    onToDoClick: (Long) -> Unit,
    onAddToDo: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isHOD by userViewModel.isHOD.collectAsStateWithLifecycle()
    var showDepartmentTasks by remember { mutableStateOf(false) }

    val (todos, pendingCount, overdueCount) = when (val state = uiState) {
        is UiState.Success -> {
            val data = state.data
            val filteredTodos = if (isHOD && showDepartmentTasks) {
                data.todos.filter { it.isDepartmentTask }
            } else {
                data.todos.filter { !it.isDepartmentTask || it.assignedTo.isNotEmpty() }
            }
            Triple(filteredTodos, data.pendingCount, data.overdueCount)
        }
        else -> Triple(emptyList(), 0, 0)
    }

    Scaffold(
        containerColor = BackgroundPrimary,
        floatingActionButton = {
            if (isHOD) {
                FloatingActionButton(
                    onClick = onAddToDo,
                    containerColor = SoftUIColors.AccentPeach,
                    contentColor = Color.White
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Assign Task")
                    }
                }
            } else {
                IconCircleButton(
                    icon = Icons.Default.Add,
                    onClick = onAddToDo,
                    contentDescription = "Add Task",
                    size = 56.dp
                )
            }
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tasks",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextPrimary
                    )
                    if (isHOD) {
                        SoftChip(
                            text = "HOD",
                            selected = true,
                            selectedBackgroundColor = SoftUIColors.AccentPeach,
                            selectedTextColor = Color.White
                        )
                    }
                }
            }

            if (isHOD) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = !showDepartmentTasks,
                            onClick = { showDepartmentTasks = false },
                            label = { Text("My Tasks") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = showDepartmentTasks,
                            onClick = { showDepartmentTasks = true },
                            label = { Text("Department Tasks") },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SoftUIColors.AccentPeach,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
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
                        isHOD = isHOD,
                        onToggle = { viewModel.toggleCompletion(todo) },
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
    isHOD: Boolean,
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
                    checkedColor = Color(0xFF34C759),
                    uncheckedColor = Color(0xFFC7C7CC)
                )
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else null
                        ),
                        color = if (todo.isCompleted) TextSecondary else TextPrimary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (todo.isDepartmentTask) {
                        SoftChip(
                            text = "Dept",
                            selected = true,
                            selectedBackgroundColor = SoftUIColors.AccentPeach.copy(alpha = 0.2f),
                            selectedTextColor = SoftUIColors.AccentPeach
                        )
                    }
                }
                
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
                    
                    if (isHOD && todo.assignedToName.isNotEmpty()) {
                        Text(
                            text = "→ ${todo.assignedToName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val format = SimpleDateFormat("MMM d", Locale.getDefault())
    return format.format(date)
}
