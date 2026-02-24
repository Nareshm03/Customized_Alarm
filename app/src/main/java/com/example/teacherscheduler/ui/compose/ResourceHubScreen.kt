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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.teacherscheduler.model.Resource
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*
import com.example.teacherscheduler.viewmodel.ResourceViewModel
import com.example.teacherscheduler.viewmodel.UiState
import com.example.teacherscheduler.viewmodel.ResourceData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourceHubScreen(
    viewModel: ResourceViewModel = hiltViewModel(),
    onAddResource: () -> Unit
) {
    val uiState: UiState<ResourceData> by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Success -> {
            ResourceHubContent(
                data = state.data,
                onAddResource = onAddResource
            )
        }
        is UiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = state.message, color = Color.Red)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ResourceHubContent(
    data: ResourceData,
    onAddResource: () -> Unit
) {
    val resources = data.resources
    val subjects = data.subjects
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf<String?>(null) }

    val filteredResources = resources.filter {
        val matchesSearch = searchQuery.isEmpty() || 
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.subject.contains(searchQuery, ignoreCase = true)
        val matchesSubject = selectedSubject == null || it.subject == selectedSubject
        matchesSearch && matchesSubject
    }

    Scaffold(
        containerColor = BackgroundPrimary,
        floatingActionButton = {
            IconCircleButton(
                icon = Icons.Default.Add,
                onClick = onAddResource,
                contentDescription = "Add Resource",
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
                    text = "Resource Hub",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
            }

            item {
                SearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search resources..."
                )
            }

            if (subjects.isNotEmpty()) {
                item {
                    SubjectFilter(
                        subjects = subjects,
                        selectedSubject = selectedSubject,
                        onSubjectSelected = { selectedSubject = it }
                    )
                }
            }

            if (filteredResources.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Folder,
                        title = "No resources yet",
                        subtitle = "Add your first resource to get started",
                        actionText = "Add Resource",
                        onActionClick = onAddResource
                    )
                }
            } else {
                item {
                    Text(
                        text = "All Resources (${filteredResources.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextPrimary
                    )
                }

                items(
                    items = filteredResources,
                    key = { it.id }
                ) { resource ->
                    ResourceCard(resource = resource)
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(text = placeholder, color = TextTertiary) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = TextSecondary
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = TextSecondary
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF2F2F7),
            unfocusedContainerColor = Color(0xFFF2F2F7),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = Color(0xFF007AFF)
        )
    )
}

@Composable
private fun SubjectFilter(
    subjects: List<String>,
    selectedSubject: String?,
    onSubjectSelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedSubject == null,
            onClick = { onSubjectSelected(null) },
            label = { Text("All") }
        )
        subjects.forEach { subject ->
            FilterChip(
                selected = selectedSubject == subject,
                onClick = { onSubjectSelected(subject) },
                label = { Text(subject) }
            )
        }
    }
}

@Composable
private fun ResourceCard(
    resource: Resource,
    modifier: Modifier = Modifier
) {
    PremiumCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2F2F7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = Color(0xFF2B2B2B),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = resource.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "${resource.subject} • ${resource.uploadedByName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (resource.description.isNotEmpty()) {
                    Text(
                        text = resource.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
