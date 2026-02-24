package com.example.teacherscheduler.ui.compose

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Notes
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.teacherscheduler.model.Meeting
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*
import com.example.teacherscheduler.viewmodel.AddEditMeetingViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * AddEditMeetingScreen - Modern soft UI design for adding/editing meetings
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
fun AddEditMeetingScreen(
    meeting: Meeting? = null,
    onSave: (Meeting) -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: AddEditMeetingViewModel = hiltViewModel()

    var isSaving by remember { mutableStateOf(false) }
    // State variables
    var title by remember { mutableStateOf(meeting?.title ?: "") }
    var withWhom by remember { mutableStateOf(meeting?.withWhom ?: "") }
    var location by remember { mutableStateOf(meeting?.location ?: "") }
    var notes by remember { mutableStateOf(meeting?.notes ?: "") }
    var selectedDate by remember { mutableStateOf(meeting?.startDate ?: Date()) }
    var startTime by remember { mutableStateOf(meeting?.startTime ?: getDefaultStartTime()) }
    var endTime by remember { mutableStateOf(meeting?.endTime ?: getDefaultEndTime()) }
    var notificationsEnabled by remember { mutableStateOf(meeting?.notificationsEnabled ?: true) }

    // Date/Time pickers state
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val isEditing = meeting != null

    // Screen fade-in animation
    ScreenFadeIn {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                SoftTopAppBar(
                    title = if (isEditing) "Edit Meeting" else "Add Meeting",
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
                    cornerRadius = 16.dp,
                    elevation = 2.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    color = PrimaryContainer,
                                    shape = RoundedCornerShape(14.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Event,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (isEditing) "Edit Meeting Details" else "Schedule New Meeting",
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
                            color = SoftUIColors.AccentBlue
                        )

                        // Title Field
                        SoftTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = "Meeting Title",
                            placeholder = "e.g., Faculty Meeting",
                            leadingIcon = Icons.Outlined.Title
                        )

                        // With Whom Field
                        SoftTextField(
                            value = withWhom,
                            onValueChange = { withWhom = it },
                            label = "With Whom",
                            placeholder = "e.g., Department Head",
                            leadingIcon = Icons.Outlined.Person,
                            singleLine = true
                        )

                        // Location Field
                        SoftTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = "Location",
                            placeholder = "e.g., Conference Room A",
                            leadingIcon = Icons.Outlined.LocationOn
                        )

                        // Notes Field
                        SoftTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = "Notes (Optional)",
                            placeholder = "Additional notes about the meeting",
                            leadingIcon = Icons.AutoMirrored.Outlined.Notes,
                            singleLine = false,
                            minLines = 3
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
                            color = SoftUIColors.AccentBlue
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
                            color = SoftUIColors.AccentBlue
                        )

                        // Notifications Toggle
                        SoftToggleRow(
                            label = "Enable Notifications",
                            description = "Get reminded before meeting starts",
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            icon = Icons.Outlined.Notifications
                        )
                    }
                }

                // Action Buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Full-width pill-style Save button
                    RoundedPrimaryButton(
                        text = if (isEditing) "Save Changes" else "Create Meeting",
                        onClick = {
                            // Validate and save
                            if (title.isNotBlank() && withWhom.isNotBlank()) {
                                isSaving = true
                                scope.launch {
                                    try {
                                        val newMeeting = Meeting(
                                            id = meeting?.id ?: System.currentTimeMillis(),
                                            title = title,
                                            withWhom = withWhom,
                                            location = location,
                                            notes = notes,
                                            startDate = selectedDate,
                                            endDate = selectedDate,
                                            date = selectedDate,
                                            startTime = startTime,
                                            endTime = endTime,
                                            notificationsEnabled = notificationsEnabled
                                        )
                                        val success = viewModel.saveMeeting(newMeeting)
                                        isSaving = false
                                        if (success) {
                                            Toast.makeText(context, "Meeting saved successfully", Toast.LENGTH_SHORT).show()
                                            onSave(newMeeting)
                                        } else {
                                            Toast.makeText(context, "Failed to save meeting", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        isSaving = false
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Title and With Whom are required", Toast.LENGTH_SHORT).show()
                            }
                        },
                        icon = if (isEditing) Icons.Outlined.Save else Icons.Outlined.Add,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving,
                        cornerRadius = 14.dp
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
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }


    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDate = Date(it)
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Start Time Picker Dialog
    if (showStartTimePicker) {
        val cal = Calendar.getInstance().apply { time = startTime }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            title = { Text("Start Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val newCal = Calendar.getInstance()
                    newCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    newCal.set(Calendar.MINUTE, timePickerState.minute)
                    newCal.set(Calendar.SECOND, 0)
                    startTime = newCal.time
                    showStartTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // End Time Picker Dialog
    if (showEndTimePicker) {
        val cal = Calendar.getInstance().apply { time = endTime }
        val timePickerState = rememberTimePickerState(
            initialHour = cal.get(Calendar.HOUR_OF_DAY),
            initialMinute = cal.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            title = { Text("End Time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    val newCal = Calendar.getInstance()
                    newCal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    newCal.set(Calendar.MINUTE, timePickerState.minute)
                    newCal.set(Calendar.SECOND, 0)
                    endTime = newCal.time
                    showEndTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helper functions
private fun getDefaultStartTime(): Date {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 14)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.time
}

private fun getDefaultEndTime(): Date {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 15)
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
    singleLine: Boolean = true,
    minLines: Int = 1
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
                        tint = SoftUIColors.AccentBlue
                    )
                }
            } else null,
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SoftUIColors.ChipBackground,
                unfocusedContainerColor = SoftUIColors.ChipBackground,
                disabledContainerColor = SoftUIColors.ChipBackground,
                focusedBorderColor = SoftUIColors.AccentBlue.copy(alpha = 0.5f),
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = SoftUIColors.AccentBlue
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
                        tint = SoftUIColors.AccentBlue,
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
                    tint = SoftUIColors.AccentBlue,
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
                    checkedTrackColor = SoftUIColors.AccentBlue,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = OutlineLight,
                    uncheckedBorderColor = OutlineLight
                )
            )
        }
    }
}





