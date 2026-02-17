package com.example.teacherscheduler.features.department.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.teacherscheduler.features.department.model.ResourceVisibility

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadResourceScreen(
    departmentId: String,
    userId: String,
    viewModel: UploadResourceViewModel,
    onUploadSuccess: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf(ResourceVisibility.DEPARTMENT) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var showSubjectDropdown by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val subjects = listOf(
        "Mathematics",
        "Physics",
        "Chemistry",
        "Biology",
        "Computer Science",
        "English",
        "History",
        "Geography",
        "Other"
    )

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = it.lastPathSegment ?: "Selected file"
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is UploadUiState.Success -> {
                snackbarHostState.showSnackbar("Resource uploaded successfully!")
                viewModel.resetState()
                onUploadSuccess()
            }
            is UploadUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as UploadUiState.Error).message)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Resource") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                placeholder = { Text("Enter resource title") },
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = uiState !is UploadUiState.Loading
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Enter description") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                enabled = uiState !is UploadUiState.Loading
            )

            ExposedDropdownMenuBox(
                expanded = showSubjectDropdown,
                onExpandedChange = { 
                    if (uiState !is UploadUiState.Loading) {
                        showSubjectDropdown = !showSubjectDropdown
                    }
                }
            ) {
                OutlinedTextField(
                    value = selectedSubject,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Subject *") },
                    placeholder = { Text("Select subject") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showSubjectDropdown) },
                    leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    enabled = uiState !is UploadUiState.Loading
                )
                ExposedDropdownMenu(
                    expanded = showSubjectDropdown,
                    onDismissRequest = { showSubjectDropdown = false }
                ) {
                    subjects.forEach { subject ->
                        DropdownMenuItem(
                            text = { Text(subject) },
                            onClick = {
                                selectedSubject = subject
                                showSubjectDropdown = false
                            }
                        )
                    }
                }
            }

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
                    Text(
                        text = "Visibility",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = visibility == ResourceVisibility.DEPARTMENT,
                            onClick = { visibility = ResourceVisibility.DEPARTMENT },
                            label = { Text("Department") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.People,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            enabled = uiState !is UploadUiState.Loading
                        )

                        FilterChip(
                            selected = visibility == ResourceVisibility.PRIVATE,
                            onClick = { visibility = ResourceVisibility.PRIVATE },
                            label = { Text("Private") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            enabled = uiState !is UploadUiState.Loading
                        )
                    }
                }
            }

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
                    Text(
                        text = "File",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (selectedFileUri != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedFileName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { 
                                selectedFileUri = null
                                selectedFileName = ""
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { filePicker.launch("*/*") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = uiState !is UploadUiState.Loading
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (selectedFileUri == null) "Choose File" else "Change File")
                    }
                }
            }

            Button(
                onClick = {
                    selectedFileUri?.let { uri ->
                        val fileExtension = selectedFileName.substringAfterLast(".", "")
                        viewModel.uploadResource(
                            fileUri = uri,
                            title = title,
                            description = description,
                            subjectName = selectedSubject,
                            visibility = visibility,
                            fileType = fileExtension
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState !is UploadUiState.Loading && 
                         title.isNotBlank() && 
                         selectedSubject.isNotBlank() && 
                         selectedFileUri != null
            ) {
                if (uiState is UploadUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Uploading...")
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Resource")
                }
            }
        }
    }
}
