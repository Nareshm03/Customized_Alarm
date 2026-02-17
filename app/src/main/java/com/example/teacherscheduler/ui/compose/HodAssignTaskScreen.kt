package com.example.teacherscheduler.ui.compose

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.teacherscheduler.model.DepartmentMember
import com.example.teacherscheduler.model.ToDo
import com.example.teacherscheduler.ui.theme.AppSpacing
import com.example.teacherscheduler.viewmodel.AssignTaskUiState
import com.example.teacherscheduler.viewmodel.DepartmentViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * HOD Assign Task Screen - Jetpack Compose implementation
 *
 * Features:
 * - Title and description input
 * - Multi-select teacher dropdown
 * - Deadline picker (date & time)
 * - Reminder minutes selector
 * - Priority selector
 * - Submit button with loading/error states
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HodAssignTaskScreen(
    departmentId: Long,
    onTaskAssigned: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DepartmentViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val departmentMembers by viewModel.departmentMembers.collectAsState()

    // Form state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedTeacherIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var assignToAll by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedPriority by remember { mutableStateOf(ToDo.Priority.MEDIUM) }
    var reminderMinutes by remember { mutableStateOf(15) }
    var showTeacherDropdown by remember { mutableStateOf(false) }
    var showPriorityDropdown by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()) }

    // Load department members when screen opens
    LaunchedEffect(departmentId) {
        viewModel.loadDepartmentMembers(departmentId)
    }

    // Handle success state
    LaunchedEffect(uiState) {
        if (uiState is AssignTaskUiState.Success) {
            onTaskAssigned()
            viewModel.resetUiState()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Assign Task") },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppSpacing.cardPadding),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionSpacing)
            ) {
                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    placeholder = { Text("Enter task title") },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = uiState !is AssignTaskUiState.Loading
                )

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Enter task description") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    enabled = uiState !is AssignTaskUiState.Loading
                )

                // Assign to All Teachers Checkbox
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = assignToAll,
                            onCheckedChange = { assignToAll = it },
                            enabled = uiState !is AssignTaskUiState.Loading
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Assign to All Teachers",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "This task will be assigned to all teachers in the department",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Teacher Selection (only show if not assigning to all)
                if (!assignToAll) {
                    ExposedDropdownMenuBox(
                        expanded = showTeacherDropdown,
                        onExpandedChange = {
                            if (uiState !is AssignTaskUiState.Loading) {
                                showTeacherDropdown = !showTeacherDropdown
                            }
                        }
                    ) {
                        OutlinedTextField(
                            value = getSelectedTeachersText(selectedTeacherIds, departmentMembers),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Teachers *") },
                            placeholder = { Text("Tap to select teachers") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTeacherDropdown) },
                            leadingIcon = { Icon(Icons.Default.People, contentDescription = null) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            enabled = uiState !is AssignTaskUiState.Loading
                        )
                        ExposedDropdownMenu(
                            expanded = showTeacherDropdown,
                            onDismissRequest = { showTeacherDropdown = false }
                        ) {
                            departmentMembers.forEach { member ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = selectedTeacherIds.contains(member.userId),
                                                onCheckedChange = null
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(member.userName)
                                                Text(
                                                    member.userEmail,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        selectedTeacherIds = if (selectedTeacherIds.contains(member.userId)) {
                                            selectedTeacherIds - member.userId
                                        } else {
                                            selectedTeacherIds + member.userId
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Deadline Picker
                OutlinedTextField(
                    value = selectedDate?.let { dateFormat.format(it) } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Deadline") },
                    placeholder = { Text("Select deadline") },
                    leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                    trailingIcon = {
                        if (selectedDate != null) {
                            IconButton(onClick = { selectedDate = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (uiState !is AssignTaskUiState.Loading) {
                                Modifier.clickableWithoutRipple {
                                    showDateTimePicker(context, selectedDate) { date ->
                                        selectedDate = date
                                    }
                                }
                            } else Modifier
                        ),
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                // Priority Selector
                ExposedDropdownMenuBox(
                    expanded = showPriorityDropdown,
                    onExpandedChange = {
                        if (uiState !is AssignTaskUiState.Loading) {
                            showPriorityDropdown = !showPriorityDropdown
                        }
                    }
                ) {
                    OutlinedTextField(
                        value = selectedPriority.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showPriorityDropdown) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Flag,
                                contentDescription = null,
                                tint = getPriorityColor(selectedPriority)
                            )
                        },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        enabled = uiState !is AssignTaskUiState.Loading
                    )
                    ExposedDropdownMenu(
                        expanded = showPriorityDropdown,
                        onDismissRequest = { showPriorityDropdown = false }
                    ) {
                        ToDo.Priority.entries.forEach { priority ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Flag,
                                            contentDescription = null,
                                            tint = getPriorityColor(priority),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(priority.displayName)
                                    }
                                },
                                onClick = {
                                    selectedPriority = priority
                                    showPriorityDropdown = false
                                }
                            )
                        }
                    }
                }

                // Reminder Minutes Selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Reminder",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Text(
                                "$reminderMinutes minutes before",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Slider(
                            value = reminderMinutes.toFloat(),
                            onValueChange = { reminderMinutes = it.toInt() },
                            valueRange = 0f..120f,
                            steps = 11, // 0, 15, 30, 45, 60, 75, 90, 105, 120
                            enabled = uiState !is AssignTaskUiState.Loading
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0 min", style = MaterialTheme.typography.bodySmall)
                            Text("2 hours", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Error Message
                if (uiState is AssignTaskUiState.Error) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                (uiState as AssignTaskUiState.Error).message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                // Submit Button
                Button(
                    onClick = {
                        if (assignToAll) {
                            viewModel.assignTaskToAll(
                                departmentId = departmentId,
                                title = title,
                                description = description,
                                dueDate = selectedDate,
                                reminderMinutes = reminderMinutes,
                                priority = selectedPriority
                            )
                        } else {
                            viewModel.assignTask(
                                departmentId = departmentId,
                                title = title,
                                description = description,
                                selectedTeachers = selectedTeacherIds.toList(),
                                dueDate = selectedDate,
                                reminderMinutes = reminderMinutes,
                                priority = selectedPriority
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState !is AssignTaskUiState.Loading && title.isNotBlank() &&
                            (assignToAll || selectedTeacherIds.isNotEmpty())
                ) {
                    if (uiState is AssignTaskUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Assigning...")
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Assign Task")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Helper function to get selected teachers text
 */
private fun getSelectedTeachersText(
    selectedIds: Set<String>,
    members: List<DepartmentMember>
): String {
    if (selectedIds.isEmpty()) return ""
    if (selectedIds.size == 1) {
        return members.find { it.userId == selectedIds.first() }?.userName ?: ""
    }
    return "${selectedIds.size} teachers selected"
}

/**
 * Helper function to get priority color
 */
@Composable
private fun getPriorityColor(priority: ToDo.Priority): androidx.compose.ui.graphics.Color {
    return when (priority) {
        ToDo.Priority.LOW -> MaterialTheme.colorScheme.tertiary
        ToDo.Priority.MEDIUM -> MaterialTheme.colorScheme.primary
        ToDo.Priority.HIGH -> androidx.compose.ui.graphics.Color(0xFFFF9800)
        ToDo.Priority.URGENT -> MaterialTheme.colorScheme.error
    }
}

/**
 * Helper function to show date and time picker
 */
private fun showDateTimePicker(
    context: android.content.Context,
    initialDate: Date?,
    onDateTimeSelected: (Date) -> Unit
) {
    val calendar = Calendar.getInstance()
    initialDate?.let { calendar.time = it }

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)

            // After date is selected, show time picker
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    onDateTimeSelected(calendar.time)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis()
    }.show()
}

/**
 * Extension function to make clickable without ripple effect
 */
@Composable
private fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier {
    return this.then(
        Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick
        )
    )
}

