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
import com.example.teacherscheduler.model.Meeting
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*
import com.example.teacherscheduler.viewmodel.MeetingViewModel
import com.example.teacherscheduler.viewmodel.MeetingsData
import com.example.teacherscheduler.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingsScreen(
    viewModel: MeetingViewModel = hiltViewModel(),
    onMeetingClick: (Long) -> Unit,
    onAddMeeting: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    val meetings = when (uiState) {
        is UiState.Success -> (uiState as UiState.Success<MeetingsData>).data.meetings
        else -> emptyList()
    }

    val filteredMeetings = if (searchQuery.isEmpty()) {
        meetings
    } else {
        meetings.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.location.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = BackgroundPrimary,
        floatingActionButton = {
            IconCircleButton(
                icon = Icons.Default.Add,
                onClick = onAddMeeting,
                contentDescription = "Add Meeting",
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
                    text = "Meetings",
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
                    placeholder = "Search meetings..."
                )
            }

            if (filteredMeetings.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Event,
                        title = if (searchQuery.isEmpty()) "No meetings yet" else "No matching meetings",
                        subtitle = if (searchQuery.isEmpty()) "Add your first meeting" else "Try a different search term",
                        actionText = if (searchQuery.isEmpty()) "Add Meeting" else null,
                        onActionClick = if (searchQuery.isEmpty()) onAddMeeting else null
                    )
                }
            } else {
                item {
                    Text(
                        text = "All Meetings (${filteredMeetings.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextPrimary
                    )
                }

                items(
                    items = filteredMeetings,
                    key = { it.id }
                ) { meeting ->
                    MeetingCard(
                        meeting = meeting,
                        onClick = { onMeetingClick(meeting.id) }
                    )
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
        placeholder = {
            Text(text = placeholder, color = TextTertiary)
        },
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
            focusedContainerColor = Color(0xFFF7F4EF),
            unfocusedContainerColor = Color(0xFFF7F4EF),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = Color(0xFFD8B4A0)
        )
    )
}

@Composable
private fun MeetingCard(
    meeting: Meeting,
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
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF7F4EF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Event,
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
                    text = meeting.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "${meeting.location} • ${meeting.getFormattedTime()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
