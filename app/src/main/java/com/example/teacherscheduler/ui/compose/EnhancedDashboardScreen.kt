package com.example.teacherscheduler.ui.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.teacherscheduler.viewmodel.UserViewModel
import com.example.teacherscheduler.viewmodel.HodDashboardViewModel
import com.example.teacherscheduler.viewmodel.HodDashboardStats

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    hodDashboardViewModel: HodDashboardViewModel = hiltViewModel(),
    onNavigateToAddClass: () -> Unit,
    onNavigateToAddMeeting: () -> Unit,
    onClassClick: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val uiState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val isHOD by userViewModel.isHOD.collectAsStateWithLifecycle()
    val userState by userViewModel.globalUserState.collectAsStateWithLifecycle()
    val hodStats by hodDashboardViewModel.stats.collectAsStateWithLifecycle()
    
    LaunchedEffect(isHOD, userState.department) {
        if (isHOD && userState.department.isNotEmpty()) {
            hodDashboardViewModel.loadDashboardStats(userState.department)
        }
    }
    
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
                        name = userState.name,
                        role = if (isHOD) "HOD" else "Teacher",
                        onNotificationClick = onNavigateToNotifications,
                        onSettingsClick = onNavigateToSettings,
                        onNavigateToProfile = onNavigateToProfile
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

            // HOD-specific section
            if (isHOD) {
                item {
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(500, delayMillis = 250))
                    ) {
                        HODStatsSection(stats = hodStats)
                    }
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
                            color = Primary,
                            modifier = Modifier.weight(1f)
                        )
                        SoftStatChip(
                            icon = Icons.Outlined.Event,
                            label = "${uiState.upcomingMeetingsCount} Meetings",
                            color = Primary,
                            modifier = Modifier.weight(1f)
                        )
                        SoftStatChip(
                            icon = Icons.Outlined.CheckCircle,
                            label = "${uiState.activeToDosCount} Tasks",
                            color = Primary,
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
                                    onClick = { onClassClick(classItem.id) }
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
                            icon = Icons.Outlined.EventBusy,
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


        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Primary,
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
    name: String,
    role: String,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNavigateToProfile: () -> Unit
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
                    .background(PrimaryContainer)
                    .border(2.dp, Color.White, CircleShape)
                    .clickable { onNavigateToProfile() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "$name • $role",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SoftIconButton(
                icon = Icons.Outlined.Notifications,
                onClick = onNotificationClick,
                contentDescription = "Notifications"
            )
            SoftIconButton(
                icon = Icons.Outlined.Settings,
                onClick = onSettingsClick,
                contentDescription = "Settings"
            )
        }
    }
}

// ============================================================================
// HOD STATS SECTION
// ============================================================================
@Composable
private fun HODStatsSection(stats: HodDashboardStats) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.AdminPanelSettings,
                contentDescription = null,
                tint = SoftUIColors.AccentPeach,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Department Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HODStatCard(
                icon = Icons.Outlined.People,
                title = "Teachers",
                value = stats.totalTeachers.toString(),
                color = SoftUIColors.AccentLavender,
                modifier = Modifier.weight(1f)
            )
            HODStatCard(
                icon = Icons.Outlined.Assignment,
                title = "Pending",
                value = stats.pendingTasks.toString(),
                color = SoftUIColors.AccentPeach,
                modifier = Modifier.weight(1f)
            )
            HODStatCard(
                icon = Icons.Outlined.Notifications,
                title = "Notices",
                value = stats.noticesPublished.toString(),
                color = SoftUIColors.AccentMint,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HODStatCard(
    icon: ImageVector,
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    PremiumCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = TextPrimary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

// ============================================================================
// HOD ACTIONS CARD
// ============================================================================
@Composable
private fun HODActionsCard() {
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        elevation = 2.dp
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AdminPanelSettings,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "HOD Actions",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    onClick = { },
                    shape = RoundedCornerShape(12.dp),
                    color = Primary.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Assignment,
                            contentDescription = null,
                            tint = Primary
                        )
                        Text(
                            text = "Assign Task",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    onClick = { },
                    shape = RoundedCornerShape(12.dp),
                    color = Primary.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.People,
                            contentDescription = null,
                            tint = Primary
                        )
                        Text(
                            text = "Teachers",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    onClick = { },
                    shape = RoundedCornerShape(12.dp),
                    color = Primary.copy(alpha = 0.1f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Dashboard,
                            contentDescription = null,
                            tint = Primary
                        )
                        Text(
                            text = "Overview",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
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
    SoftCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = { nextClass?.let { onClassClick(it.id) } },
        cornerRadius = 16.dp,
        elevation = 2.dp
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
                    color = Primary
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

                Spacer(modifier = Modifier.height(4.dp))

                RoundedPrimaryButton(
                    text = "View Schedule",
                    onClick = onViewScheduleClick,
                    cornerRadius = 14.dp
                )
            }

            // Today count badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SoftUIColors.BlueTint)
            ) {
                Text(
                    text = totalClassesToday.toString(),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = Primary
                )
                Text(
                    text = "classes",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }
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
    accentColor: Color = Primary,
    gradientColors: List<Color> = listOf(PrimaryContainer, PrimaryContainer)
) {
    SoftCard(
        modifier = modifier.aspectRatio(0.9f),
        onClick = onClick,
        cornerRadius = 16.dp,
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Icon on flat tint background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = null,
                    tint = Primary,
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

            // Bottom: Tags and Subject label
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SoftColorChip(
                        text = classItem.getFormattedTime(),
                        color = Primary
                    )
                    SoftColorChip(
                        text = classItem.room,
                        color = Primary
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = classItem.subject,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = Primary,
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
        cornerRadius = 16.dp,
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
                    .background(PrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Event,
                    contentDescription = null,
                    tint = Primary,
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
