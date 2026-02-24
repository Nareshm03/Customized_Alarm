package com.example.teacherscheduler.ui.compose

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*
import com.example.teacherscheduler.util.HapticFeedback
import com.example.teacherscheduler.util.rememberHapticFeedback
import com.example.teacherscheduler.viewmodel.NotificationSettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val haptic = rememberHapticFeedback()
    
    Scaffold(
        containerColor = BackgroundPrimary,
        topBar = {
            SoftTopAppBar(
                title = "Notifications",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = AppSpacing.screenHorizontal,
                end = AppSpacing.screenHorizontal,
                top = AppSpacing.largeSpacing,
                bottom = 100.dp // Extra padding for bottom navigation
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionSpacing)
        ) {

            // Global Settings
            item {
                SoftSection(title = "General") {
                    SoftSwitchCard(
                        title = "Enable Notifications",
                        subtitle = "Receive all notifications",
                        icon = Icons.Outlined.Notifications,
                        checked = settings.notificationsEnabled,
                        onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                    )
                    
                    SoftSwitchCard(
                        title = "Sound & Vibration",
                        subtitle = "Play sound and vibrate",
                        icon = Icons.Outlined.VolumeUp,
                        checked = settings.soundEnabled,
                        onCheckedChange = { viewModel.setSoundEnabled(it) }
                    )
                }
            }

            // Category Settings
            item {
                SoftSection(title = "Categories") {
                    SoftSwitchCard(
                        title = "Class Notifications",
                        subtitle = "Reminders for classes",
                        icon = Icons.Outlined.School,
                        checked = settings.classNotificationsEnabled,
                        onCheckedChange = { viewModel.setClassNotificationsEnabled(it) }
                    )
                    
                    SoftSwitchCard(
                        title = "Meeting Notifications",
                        subtitle = "Reminders for meetings",
                        icon = Icons.Outlined.Event,
                        checked = settings.meetingNotificationsEnabled,
                        onCheckedChange = { viewModel.setMeetingNotificationsEnabled(it) }
                    )
                }
            }

            // Reminder Intervals
            item {
                SoftSection(title = "Reminder Times") {
                    SoftContentCard(
                        backgroundColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Select when to receive reminders",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            
                            ReminderIntervalChips(
                                selectedIntervals = settings.reminderIntervals,
                                onIntervalsChanged = { viewModel.setReminderIntervals(it) }
                            )
                        }
                    }
                }
            }

            // Save Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        haptic(HapticFeedback.HapticType.HEAVY_CLICK)
                        scope.launch {
                            viewModel.saveSettings()
                            Toast.makeText(context, "Settings saved", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.button),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save Settings", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun SoftSwitchCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    SoftContentCard(
        backgroundColor = Color.White,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(AppSpacing.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Primary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFE8E8E8)
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderIntervalChips(
    selectedIntervals: List<Int>,
    onIntervalsChanged: (List<Int>) -> Unit
) {
    val intervals = listOf(
        0 to "Exact",
        1 to "1m",
        5 to "5m",
        15 to "15m",
        30 to "30m",
        60 to "1h"
    )
    
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        intervals.forEach { (minutes, label) ->
            val isSelected = selectedIntervals.contains(minutes)
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newIntervals = if (isSelected) {
                        selectedIntervals - minutes
                    } else {
                        selectedIntervals + minutes
                    }
                    onIntervalsChanged(newIntervals)
                },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor = Color.White
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.chip)
            )
        }
    }
}
