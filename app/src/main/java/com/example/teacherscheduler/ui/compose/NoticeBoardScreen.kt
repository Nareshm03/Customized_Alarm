package com.example.teacherscheduler.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.teacherscheduler.model.Notice
import com.example.teacherscheduler.model.NoticePriority
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*
import com.example.teacherscheduler.viewmodel.NoticeViewModel
import com.example.teacherscheduler.viewmodel.UiState
import com.example.teacherscheduler.viewmodel.NoticeData
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeBoardScreen(
    viewModel: NoticeViewModel = hiltViewModel(),
    onAddNotice: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is UiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is UiState.Success -> {
            NoticeBoardContent(
                data = state.data,
                onAddNotice = onAddNotice,
                onMarkSeen = { viewModel.markAsSeen(it) }
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
private fun NoticeBoardContent(
    data: NoticeData,
    onAddNotice: () -> Unit,
    onMarkSeen: (Long) -> Unit
) {
    val notices = data.notices
    val unseenCount = data.unseenCount
    val isHOD = data.isHOD

    Scaffold(
        containerColor = BackgroundPrimary,
        floatingActionButton = {
            if (isHOD) {
                IconCircleButton(
                    icon = Icons.Default.Add,
                    onClick = onAddNotice,
                    contentDescription = "Post Notice",
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
                        text = "Notice Board",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextPrimary
                    )
                    if (unseenCount > 0) {
                        Badge(
                            containerColor = Color(0xFFE57373)
                        ) {
                            Text(
                                text = unseenCount.toString(),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            if (notices.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.Notifications,
                        title = "No notices yet",
                        subtitle = if (isHOD) "Post your first notice" else "Check back later for updates"
                    )
                }
            } else {
                items(
                    items = notices,
                    key = { it.id }
                ) { notice ->
                    NoticeCard(
                        notice = notice,
                        onMarkSeen = { onMarkSeen(notice.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoticeCard(
    notice: Notice,
    onMarkSeen: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = notice.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "By ${notice.createdByName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                
                PriorityBadge(priority = notice.priority)
            }

            Text(
                text = notice.message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(notice.publishedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                
                SecondaryTextButton(
                    text = "Mark as Seen",
                    onClick = onMarkSeen
                )
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: NoticePriority) {
    val color = when (priority) {
        NoticePriority.URGENT -> Color(0xFFE57373)
        NoticePriority.HIGH -> Color(0xFFF5C06D)
        NoticePriority.NORMAL -> Color(0xFFD8B4A0)
        NoticePriority.LOW -> Color(0xFFECE6DF)
    }
    
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = priority.name,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val format = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return format.format(Date(timestamp))
}
