package com.example.teacherscheduler.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.teacherscheduler.model.AnnouncementPriority
import com.example.teacherscheduler.model.DepartmentAnnouncement
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*
import com.example.teacherscheduler.viewmodel.DepartmentNoticesViewModel
import com.example.teacherscheduler.viewmodel.NoticesUiState
import java.text.SimpleDateFormat
import java.util.*

/**
 * DepartmentNoticesScreen - Redesigned with soft UI layout system
 *
 * Design:
 * - Clean list with staggered animations
 * - Large rounded cards (24dp)
 * - Proper spacing (16dp between items)
 * - Minimal visual clutter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentNoticesScreen(
    modifier: Modifier = Modifier,
    viewModel: DepartmentNoticesViewModel = viewModel(),
    onNoticeClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notices by viewModel.notices.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadNotices()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            is NoticesUiState.Loading -> {
                LoadingState()
            }
            is NoticesUiState.Success -> {
                NoticesList(
                    notices = notices,
                    onNoticeClick = { noticeId ->
                        viewModel.markNoticeAsSeen(noticeId)
                        onNoticeClick(noticeId)
                    },
                    onRefresh = { viewModel.refresh() }
                )
            }
            is NoticesUiState.Empty -> {
                EmptyState()
            }
            is NoticesUiState.Error -> {
                ErrorState(
                    message = (uiState as NoticesUiState.Error).message,
                    onRetry = { viewModel.refresh() }
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = SoftUIColors.AccentLavender,
            strokeWidth = 3.dp
        )
    }
}

@Composable
private fun EmptyState() {
    SoftEmptyStateView(
        title = "No Notices Yet",
        subtitle = "Department notices and announcements\nwill appear here",
        icon = Icons.Outlined.Campaign
    )
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SoftLayoutDimens.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SoftUIColors.CoralGradientStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Campaign,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = SoftUIColors.AccentCoral
            )
        }

        Text(
            text = "Error Loading Notices",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = TextPrimary
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )

        RoundedPrimaryButton(
            text = "Retry",
            onClick = onRetry,
            icon = Icons.Outlined.Refresh
        )
    }
}

@Composable
private fun NoticesList(
    notices: List<DepartmentAnnouncement>,
    onNoticeClick: (Long) -> Unit,
    onRefresh: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(SoftLayoutDimens.cardSpacing)
    ) {
        itemsIndexed(
            items = notices,
            key = { _, item -> item.id }
        ) { index, notice ->
            StaggeredItem(index = index) {
                NoticeCard(
                    notice = notice,
                    onClick = { onNoticeClick(notice.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(SoftLayoutDimens.bottomSafeArea))
        }
    }
}

@Composable
private fun NoticeCard(
    notice: DepartmentAnnouncement,
    onClick: () -> Unit
) {
    val priorityColor = getPriorityColor(notice.priority)
    val priorityBgColor = getPriorityBgColor(notice.priority)
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    SoftCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppDimens.spacingMedium)
        ) {
            // Header row with priority and pinned badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority badge
                Surface(
                    color = priorityBgColor,
                    shape = RoundedCornerShape(AppDimens.cornerRadiusSmall)
                ) {
                    Text(
                        text = notice.priority.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = priorityColor,
                        modifier = Modifier.padding(
                            horizontal = AppDimens.spacingMedium,
                            vertical = AppDimens.spacingXSmall
                        )
                    )
                }

                if (notice.isPinned) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppDimens.spacingXSmall)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier.size(14.dp),
                            tint = Primary
                        )
                        Text(
                            text = "Pinned",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary
                        )
                    }
                }
            }

            // Title
            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Description
            Text(
                text = notice.message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Seen count with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.spacingXSmall)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TextTertiary
                    )
                    Text(
                        text = "${notice.totalReaders}/${notice.totalMembers}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }

                // Date and creator
                Text(
                    text = "${dateFormat.format(Date(notice.publishedAt))} • ${notice.createdByName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
    }
}

/**
 * Get soft color for priority
 */
@Composable
private fun getPriorityColor(priority: AnnouncementPriority): Color {
    return when (priority) {
        AnnouncementPriority.URGENT -> Error
        AnnouncementPriority.HIGH -> Warning
        AnnouncementPriority.NORMAL -> Primary
        AnnouncementPriority.LOW -> Secondary
    }
}

/**
 * Get soft background color for priority
 */
@Composable
private fun getPriorityBgColor(priority: AnnouncementPriority): Color {
    return when (priority) {
        AnnouncementPriority.URGENT -> ErrorLight
        AnnouncementPriority.HIGH -> WarningLight
        AnnouncementPriority.NORMAL -> PrimaryContainer
        AnnouncementPriority.LOW -> SecondaryContainer
    }
}
