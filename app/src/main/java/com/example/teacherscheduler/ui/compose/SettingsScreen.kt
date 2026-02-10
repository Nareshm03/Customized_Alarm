package com.example.teacherscheduler.ui.compose

import android.Manifest
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.teacherscheduler.util.CsvExporter
import com.example.teacherscheduler.util.GoogleCalendarSync
import com.example.teacherscheduler.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

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
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Data Management",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                Card(
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
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("Export to CSV") },
                        supportingContent = { Text("Export all classes and meetings") },
                        leadingContent = { Icon(Icons.Default.Download, null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, null) }
                    )
                }
            }
            
            item {
                Card(
                    onClick = {
                        calendarPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ListItem(
                        headlineContent = { Text("Sync to Google Calendar") },
                        supportingContent = { Text("Sync all classes and meetings") },
                        leadingContent = { Icon(Icons.Default.Event, null) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, null) }
                    )
                }
            }
        }
    }
    
    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            title = { Text("Sync to Google Calendar") },
            text = { 
                if (syncInProgress) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Syncing...")
                    }
                } else {
                    Text("Sync ${classes.size} classes and ${meetings.size} meetings to your Google Calendar?")
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
                        Text("Sync")
                    }
                }
            },
            dismissButton = {
                if (!syncInProgress) {
                    TextButton(onClick = { showSyncDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}
