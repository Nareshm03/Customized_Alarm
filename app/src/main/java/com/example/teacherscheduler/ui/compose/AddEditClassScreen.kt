package com.example.teacherscheduler.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * AddEditClassScreen - Modern soft UI design for adding/editing classes
 *
 * Design:
 * - White background (#FFFFFF)
 * - Sections grouped in SoftCard
 * - Rounded text fields with light grey backgrounds
 * - Large vertical spacing (24dp)
 * - Full-width pill-style save button
 * - Text-only cancel button
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditClassScreen(
    classItem: Class? = null,
    onSave: (Class) -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    // State variables
    var subject by remember { mutableStateOf(classItem?.subject ?: "") }
    var department by remember { mutableStateOf(classItem?.department ?: "") }
    var roomNumber by remember { mutableStateOf(classItem?.roomNumber ?: "") }
    var selectedDate by remember { mutableStateOf(classItem?.startDate ?: Date()) }
    var startTime by remember { mutableStateOf(classItem?.startTime ?: getDefaultStartTime()) }
    var endTime by remember { mutableStateOf(classItem?.endTime ?: getDefaultEndTime()) }
    var notificationsEnabled by remember { mutableStateOf(classItem?.notificationsEnabled ?: true) }
    var isRecurring by remember { mutableStateOf(classItem?.isRecurring ?: false) }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }

    // Date/Time pickers state
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val isEditing = classItem != null

    // Screen fade-in animation
    ScreenFadeIn {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                SoftTopAppBar(
                    title = if (isEditing) "Edit Class" else "Add Class",
                    navigationIcon = {
                        IconButton(onClick = onCancel) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }
                    },
                    actions = {
                        if (isEditing && onDelete != null) {
                            IconButton(onClick = onDelete) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = Error
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(SoftLayoutDimens.screenPadding),
                verticalArrangement = Arrangement.spacedBy(SoftLayoutDimens.sectionSpacing)
            ) {
                // Header Card
                SoftCard(
                    cornerRadius = 20.dp,
                    elevation = 2.dp,
                    gradientColors = listOf(
                        SoftUIColors.LavenderGradientStart,
                        SoftUIColors.LavenderGradientEnd
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = SoftUIColors.AccentLavender.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.School,
                                contentDescription = null,
                                tint = SoftUIColors.AccentLavender,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (isEditing) "Edit Class Details" else "Create New Class",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Fill in the details below",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Basic Information Section
                SoftCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Basic Information",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = SoftUIColors.AccentLavender
                        )

                        // Subject Field
                        SoftTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = "Subject Name",
                            placeholder = "e.g., Mathematics",
                            leadingIcon = Icons.Outlined.School
                        )

                        // Department Field
                        SoftTextField(
                            value = department,
                            onValueChange = { department = it },
                            label = "Department",
                            placeholder = "e.g., Science",
                            leadingIcon = Icons.Outlined.Business
                        )

                        // Room Number Field
                        SoftTextField(
                            value = roomNumber,
                            onValueChange = { roomNumber = it },
                            label = "Room Number",
                            placeholder = "e.g., 101A",
                            leadingIcon = Icons.Outlined.Room
                        )
                    }
                }

                // Schedule Section
                SoftCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Schedule",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = SoftUIColors.AccentLavender
                        )

                        // Date Selector
                        SoftSelectButton(
                            label = "Date",
                            value = dateFormat.format(selectedDate),
                            icon = Icons.Outlined.CalendarToday,
                            onClick = { showDatePicker = true }
                        )

                        // Time Selectors
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SoftSelectButton(
                                modifier = Modifier.weight(1f),
                                label = "Start Time",
                                value = timeFormat.format(startTime),
                                icon = Icons.Outlined.Schedule,
                                onClick = { showStartTimePicker = true }
                            )

                            SoftSelectButton(
                                modifier = Modifier.weight(1f),
                                label = "End Time",
                                value = timeFormat.format(endTime),
                                icon = Icons.Outlined.Schedule,
                                onClick = { showEndTimePicker = true }
                            )
                        }
                    }
                }

                // Settings Section
                SoftCard {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = SoftUIColors.AccentLavender
                        )

                        // Notifications Toggle
                        SoftToggleRow(
                            label = "Enable Notifications",
                            description = "Get reminded before class starts",
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            icon = Icons.Outlined.Notifications
                        )

                        // Recurring Toggle
                        SoftToggleRow(
                            label = "Recurring Class",
                            description = "Repeat this class weekly",
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it },
                            icon = Icons.Outlined.Repeat
                        )

                        // Days of Week (shown when recurring)
                        if (isRecurring) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Repeat on",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                    days.forEach { day ->
                                        SoftChip(
                                            text = day,
                                            selected = selectedDays.contains(day),
                                            onClick = {
                                                selectedDays = if (selectedDays.contains(day)) {
                                                    selectedDays - day
                                                } else {
                                                    selectedDays + day
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Full-width pill-style Save button
                    RoundedPrimaryButton(
                        text = if (isEditing) "Save Changes" else "Create Class",
                        onClick = {
                            // Validate and save
                            if (subject.isNotBlank() && department.isNotBlank()) {
                                val newClass = Class(
                                    id = classItem?.id ?: 0,
                                    subject = subject,
                                    department = department,
                                    roomNumber = roomNumber,
                                    startDate = selectedDate,
                                    endDate = selectedDate,
                                    startTime = startTime,
                                    endTime = endTime,
                                    notificationsEnabled = notificationsEnabled,
                                    isRecurring = isRecurring
                                )
                                onSave(newClass)
                            }
                        },
                        icon = if (isEditing) Icons.Outlined.Save else Icons.Outlined.Add,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Text-only Cancel button
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextSecondary
                        )
                    }
                }

                // Bottom spacing
                Spacer(modifier = Modifier.height(AppSpacing.screenHorizontal))
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        // TODO: Implement Material3 DatePicker
        // For now using a placeholder
        showDatePicker = false
    }

    // Time Picker Dialogs
    if (showStartTimePicker) {
        // TODO: Implement Material3 TimePicker
        showStartTimePicker = false
    }

    if (showEndTimePicker) {
        // TODO: Implement Material3 TimePicker
        showEndTimePicker = false
    }
}

// Helper functions
private fun getDefaultStartTime(): Date {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 9)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.time
}

private fun getDefaultEndTime(): Date {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 10)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.time
}

// ============================================================================
// SOFT TEXT FIELD COMPONENT
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SoftTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    singleLine: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = TextPrimary
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = placeholder,
                    color = TextTertiary,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = SoftUIColors.AccentLavender
                    )
                }
            } else null,
            singleLine = singleLine,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SoftUIColors.ChipBackground,
                unfocusedContainerColor = SoftUIColors.ChipBackground,
                disabledContainerColor = SoftUIColors.ChipBackground,
                focusedBorderColor = SoftUIColors.AccentLavender.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = SoftUIColors.AccentLavender
            ),
            textStyle = MaterialTheme.typography.bodyLarge
        )
    }
}

// ============================================================================
// SOFT SELECT BUTTON (for date/time pickers)
// ============================================================================

@Composable
private fun SoftSelectButton(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = TextPrimary
        )

        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = SoftUIColors.ChipBackground
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = SoftUIColors.AccentLavender,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ============================================================================
// SOFT TOGGLE ROW
// ============================================================================

@Composable
private fun SoftToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = SoftUIColors.ChipBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SoftUIColors.AccentLavender,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SoftUIColors.AccentLavender,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = OutlineLight,
                    uncheckedBorderColor = OutlineLight
                )
            )
        }
    }
}





