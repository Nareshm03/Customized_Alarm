package com.example.teacherscheduler.ui.compose

import android.Manifest
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*
import com.example.teacherscheduler.util.CsvExporter
import com.example.teacherscheduler.util.GoogleCalendarSync
import com.example.teacherscheduler.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

/**
 * SettingsScreen - Redesigned with soft UI layout system
 *
 * Design:
 * - White background
 * - 24dp horizontal padding
 * - 32dp top spacing
 * - Section-based layout
 * - Large rounded cards (24dp)
 * - Minimal visual clutter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    val meetings by viewModel.meetings.collectAsStateWithLifecycle()
    
    var showSyncDialog by remember { mutableStateOf(false) }
    var syncInProgress by remember { mutableStateOf(false) }
    
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showSyncDialog = true
        } else {
            Toast.makeText(context, "Calendar permission required", Toast.LENGTH_SHORT).show()
        }
    }
    
    Scaffold(
        containerColor = BackgroundPrimary
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = SoftLayoutDimens.screenPadding,
                end = SoftLayoutDimens.screenPadding,
                top = SoftLayoutDimens.topSpacing,
                bottom = SoftLayoutDimens.bottomSafeArea
            ),
            verticalArrangement = Arrangement.spacedBy(SoftLayoutDimens.sectionSpacing)
        ) {
            // Header with Profile
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SoftProfileAvatar(
                        size = 80.dp,
                        icon = Icons.Outlined.Person,
                        gradientColors = listOf(Color(0xFFF7F4EF), Color(0xFFFAF7F2)),
                        iconTint = Color(0xFFD8B4A0),
                        borderWidth = 0.dp
                    )
                    Text(
                        text = "Teacher",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextPrimary
                    )
                }
            }

            // Data Management Section
            item {
                SoftSection(title = "Data Management") {
                    SoftSettingsCard(
                        title = "Export to CSV",
                        subtitle = "Export all classes and meetings",
                        icon = Icons.Outlined.Download,
                        onClick = {
                            scope.launch {
                                try {
                                    val classFile = CsvExporter.exportClasses(context, classes)
                                    val meetingFile = CsvExporter.exportMeetings(context, meetings)

                                    val uri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        classFile
                                    )

                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/csv"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Export CSV"))

                                    Toast.makeText(context, "CSV exported successfully", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )

                    SoftSettingsCard(
                        title = "Sync to Google Calendar",
                        subtitle = "Sync all classes and meetings",
                        icon = Icons.Outlined.Event,
                        onClick = {
                            calendarPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR)
                        }
                    )
                }
            }

            // About Section
            item {
                SoftSection(title = "About") {
                    SoftContentCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Teacher Scheduler",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Version 1.0.0",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Sync Dialog
    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { if (!syncInProgress) showSyncDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = {
                Text(
                    "Sync to Google Calendar",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
            },
            text = {
                if (syncInProgress) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionSpacing)
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFD8B4A0),
                            strokeWidth = 3.dp
                        )
                        Text(
                            "Syncing...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                } else {
                    Text(
                        "Sync ${classes.size} classes and ${meetings.size} meetings to your Google Calendar?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                if (!syncInProgress) {
                    TextButton(
                        onClick = {
                            syncInProgress = true
                            scope.launch {
                                try {
                                    var synced = 0
                                    classes.forEach { classItem ->
                                        GoogleCalendarSync.syncClassToCalendar(context, classItem)
                                        synced++
                                    }
                                    meetings.forEach { meeting ->
                                        GoogleCalendarSync.syncMeetingToCalendar(context, meeting)
                                        synced++
                                    }
                                    Toast.makeText(context, "Synced $synced items", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Sync failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    syncInProgress = false
                                    showSyncDialog = false
                                }
                            }
                        }
                    ) {
                        Text(
                            "Sync",
                            color = Color(0xFFD8B4A0),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            dismissButton = {
                if (!syncInProgress) {
                    TextButton(onClick = { showSyncDialog = false }) {
                        Text(
                            "Cancel",
                            color = TextSecondary
                        )
                    }
                }
            }
        )
    }
}

// ============================================================================
// SETTINGS CARD
// ============================================================================

@Composable
private fun SoftSettingsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SoftContentCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SoftLayoutDimens.itemSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFD8B4A0),
                modifier = Modifier.size(24.dp)
            )

            // Content
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

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextTertiary
            )
        }
    }
}
