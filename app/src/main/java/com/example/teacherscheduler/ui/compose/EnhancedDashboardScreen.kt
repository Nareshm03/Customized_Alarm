package com.example.teacherscheduler.ui.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*
import com.example.teacherscheduler.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToAddClass: () -> Unit,
    onNavigateToAddMeeting: () -> Unit,
    onClassClick: (Long) -> Unit
) {
    val uiState by viewModel.dashboardState.collectAsStateWithLifecycle()
    
    // Animation states
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 16.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Top Header Row
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(400)) +
                            slideInVertically(animationSpec = tween(400)) { -20 }
                ) {
                    EnhancedDashboardHeader(
                        greeting = uiState.greeting,
                        onNotificationClick = { },
                        onProfileClick = { }
                    )
                }
            }

            // Main Highlight Card - Next Upcoming Class (using GradientHighlightCardCustom)
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(500, delayMillis = 100)) +
                            slideInVertically(animationSpec = tween(500, delayMillis = 100)) { 40 }
                ) {
                    EnhancedNextClassCard(
                        nextClass = uiState.todayClasses.firstOrNull(),
                        totalClassesToday = uiState.todayClassesCount,
                        onViewScheduleClick = { },
                        onClassClick = { classId -> onClassClick(classId) }
                    )
                }
            }

            // Quick Stats Row (using SoftStatChip)
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(500, delayMillis = 200)) +
                            slideInVertically(animationSpec = tween(500, delayMillis = 200)) { 40 }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SoftStatChip(
                            icon = Icons.Outlined.School,
                            label = "${uiState.todayClassesCount} Classes",
                            color = SoftUIColors.AccentLavender,
                            modifier = Modifier.weight(1f)
                        )
                        SoftStatChip(
                            icon = Icons.Outlined.Event,
                            label = "${uiState.upcomingMeetingsCount} Meetings",
                            color = SoftUIColors.AccentBlue,
                            modifier = Modifier.weight(1f)
                        )
                        SoftStatChip(
                            icon = Icons.Outlined.CheckCircle,
                            label = "${uiState.activeToDosCount} Tasks",
                            color = SoftUIColors.AccentMint,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Today's Schedule Section Header (using SoftSectionHeader)
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(500, delayMillis = 300)) +
                            slideInVertically(animationSpec = tween(500, delayMillis = 300)) { 40 }
                ) {
                    SoftSectionHeader(
                        title = "Today's Schedule",
                        onActionClick = { }
                    )
                }
            }

            // Today's Classes Grid - Two Column (using SoftCard)
            if (uiState.todayClasses.isNotEmpty()) {
                itemsIndexed(
                    items = uiState.todayClasses.chunked(2),
                    key = { index, _ -> "classes_row_$index" }
                ) { rowIndex, rowItems ->
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(400, delayMillis = 350 + (rowIndex * 50))) +
                                slideInVertically(animationSpec = tween(400, delayMillis = 350 + (rowIndex * 50))) { 30 }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowItems.forEachIndexed { index, classItem ->
                                EnhancedClassCard(
                                    classItem = classItem,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onClassClick(classItem.id) },
                                    accentColor = if (index % 2 == 0) SoftUIColors.AccentLavender else SoftUIColors.AccentBlue,
                                    gradientColors = if (index % 2 == 0)
                                        listOf(SoftUIColors.LavenderGradientStart, SoftUIColors.LavenderGradientEnd)
                                    else
                                        listOf(SoftUIColors.BlueGradientStart, SoftUIColors.BlueGradientEnd)
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Empty state for classes (using SoftEmptyStateCard)
            if (uiState.todayClasses.isEmpty() && !uiState.isLoading) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(500, delayMillis = 400))
                    ) {
                        SoftEmptyStateCard(
                            icon = Icons.Outlined.School,
                            title = "No Classes Today",
                            subtitle = "Enjoy your free day!"
                        )
                    }
                }
            }

            // Meetings Section Header (using SoftSectionHeader)
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(animationSpec = tween(500, delayMillis = 450)) +
                            slideInVertically(animationSpec = tween(500, delayMillis = 450)) { 40 }
                ) {
                    SoftSectionHeader(
                        title = "Upcoming Meetings",
                        onActionClick = { }
                    )
                }
            }

            // Meetings List (using SoftCard)
            if (uiState.upcomingMeetings.isNotEmpty()) {
                itemsIndexed(
                    items = uiState.upcomingMeetings,
                    key = { _, meeting -> "meeting_${meeting.id}" }
                ) { index, meeting ->
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(400, delayMillis = 500 + (index * 50))) +
                                slideInVertically(animationSpec = tween(400, delayMillis = 500 + (index * 50))) { 30 }
                    ) {
                        EnhancedMeetingCard(meeting = meeting)
                    }
                }
            }

            // Empty state for meetings (using SoftEmptyStateCard)
            if (uiState.upcomingMeetings.isEmpty() && !uiState.isLoading) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(500, delayMillis = 550))
                    ) {
                        SoftEmptyStateCard(
                            icon = Icons.Outlined.EventBusy,
                            title = "No Meetings",
                            subtitle = "Your calendar is clear"
                        )
                    }
                }
            }

            // Insights Section (if available) - using SoftCard
            if (uiState.insights.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(500, delayMillis = 600)) +
                                slideInVertically(animationSpec = tween(500, delayMillis = 600)) { 40 }
                    ) {
                        EnhancedInsightsCard(insights = uiState.insights)
                    }
                }
            }
        }

        // Floating Action Buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Secondary FAB - Add Meeting
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(400, delayMillis = 600)) +
                        scaleIn(animationSpec = tween(400, delayMillis = 600))
            ) {
                SmallFloatingActionButton(
                    onClick = onNavigateToAddMeeting,
                    shape = RoundedCornerShape(16.dp),
                    containerColor = SecondaryContainer,
                    contentColor = OnSecondaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = AppElevation.level1,
                        pressedElevation = AppElevation.level2
                    )
                ) {
                    Icon(
                        Icons.Outlined.Event,
                        contentDescription = "Add Meeting",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Primary FAB - Add Class
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(400, delayMillis = 650)) +
                        scaleIn(animationSpec = tween(400, delayMillis = 650))
            ) {
                FloatingActionButton(
                    onClick = onNavigateToAddClass,
                    shape = RoundedCornerShape(20.dp),
                    containerColor = SoftUIColors.AccentLavender,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = AppElevation.level2,
                        pressedElevation = 8.dp
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Class",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = SoftUIColors.AccentLavender,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

// ============================================================================
// ENHANCED DASHBOARD HEADER
// ============================================================================
@Composable
private fun EnhancedDashboardHeader(
    greeting: String,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Image
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(SoftUIColors.LavenderGradientStart, SoftUIColors.LavenderGradientEnd)
                        )
                    )
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = SoftUIColors.AccentLavender,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "Ready for today?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Using SoftIconButton
            SoftIconButton(
                icon = Icons.Outlined.Notifications,
                onClick = onNotificationClick,
                contentDescription = "Notifications"
            )
            SoftIconButton(
                icon = Icons.Outlined.Settings,
                onClick = onProfileClick,
                contentDescription = "Settings"
            )
        }
    }
}

// ============================================================================
// ENHANCED NEXT CLASS CARD - Using GradientHighlightCardCustom
// ============================================================================
@Composable
private fun EnhancedNextClassCard(
    nextClass: Class?,
    totalClassesToday: Int,
    onViewScheduleClick: () -> Unit,
    onClassClick: (Long) -> Unit
) {
    GradientHighlightCardCustom(
        onClick = { nextClass?.let { onClassClick(it.id) } },
        gradientColors = listOf(
            SoftUIColors.LavenderGradientStart,
            SoftUIColors.LavenderGradientEnd,
            Color(0xFFF0EAFF)
        ),
        accentColor = SoftUIColors.AccentLavender
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (nextClass != null) "NEXT CLASS" else "TODAY",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = SoftUIColors.AccentLavender
                )

                Text(
                    text = nextClass?.title ?: "No more classes",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (nextClass != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = TextSecondary
                            )
                            Text(
                                text = nextClass.getFormattedTime(),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Room,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = TextSecondary
                            )
                            Text(
                                text = nextClass.room,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Using RoundedPrimaryButton
                RoundedPrimaryButton(
                    text = "View Schedule",
                    onClick = onViewScheduleClick
                )
            }

            // Using SoftProgressBadge
            SoftProgressBadge(
                value = totalClassesToday.toString(),
                label = "classes",
                valueColor = SoftUIColors.AccentLavender
            )
        }
    }
}

// ============================================================================
// ENHANCED CLASS CARD (TWO-COLUMN GRID) - Using SoftCard
// ============================================================================
@Composable
private fun EnhancedClassCard(
    classItem: Class,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    accentColor: Color = SoftUIColors.AccentLavender,
    gradientColors: List<Color> = listOf(SoftUIColors.LavenderGradientStart, SoftUIColors.LavenderGradientEnd)
) {
    SoftCard(
        modifier = modifier.aspectRatio(0.9f),
        onClick = onClick,
        cornerRadius = 24.dp,
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Middle: Title and Subtitle
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = classItem.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = classItem.department,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom: Tags and Progress (using SoftColorChip)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SoftColorChip(
                        text = classItem.getFormattedTime(),
                        color = accentColor
                    )
                    SoftColorChip(
                        text = classItem.room,
                        color = accentColor.copy(alpha = 0.7f)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Brush.linearGradient(gradientColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = classItem.subject,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = accentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ============================================================================
// ENHANCED MEETING CARD - Using SoftCard
// ============================================================================
@Composable
private fun EnhancedMeetingCard(
    meeting: Meeting
) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(SoftUIColors.BlueGradientStart, SoftUIColors.BlueGradientEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Event,
                    contentDescription = null,
                    tint = SoftUIColors.AccentBlue,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = meeting.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = TextTertiary
                    )
                    Text(
                        text = meeting.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Using SoftChip for time badge
            SoftChip(
                text = meeting.getFormattedTime(),
                selected = true,
                selectedBackgroundColor = SecondaryContainer,
                selectedTextColor = OnSecondaryContainer
            )
        }
    }
}

// ============================================================================
// ENHANCED INSIGHTS CARD - Using SoftCard
// ============================================================================
@Composable
private fun EnhancedInsightsCard(
    insights: List<String>
) {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = SoftUIColors.AccentPeach,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Daily Insights",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
            }

            insights.take(3).forEach { insight ->
                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}
