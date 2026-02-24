package com.example.teacherscheduler.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*

/**
 * Example screen demonstrating HeroHighlightCard usage
 */
@Composable
fun HeroCardExamplesScreen() {
    Scaffold(
        containerColor = BackgroundPrimary
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = AppSpacing.screenHorizontal,
                end = AppSpacing.screenHorizontal,
                top = AppSpacing.largeSpacing,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionSpacing)
        ) {
            // Header
            item {
                Text(
                    text = "Hero Cards Examples",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp
                    ),
                    color = TextPrimary
                )
            }

            // Example 1: Upcoming Class
            item {
                Text(
                    text = "Upcoming Class",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
            }

            item {
                HeroUpcomingClassCard(
                    className = "Advanced Mathematics",
                    timeAndLocation = "Today at 2:00 PM • Room 301",
                    onViewClick = { /* Navigate to class details */ },
                    badgeText = "Next"
                )
            }

            // Example 2: Important Notice
            item {
                Text(
                    text = "Important Notice",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
            }

            item {
                HeroImportantNoticeCard(
                    noticeTitle = "Department Meeting",
                    noticePreview = "All faculty members are invited to tomorrow's meeting at 3 PM",
                    onReadClick = { /* Open notice */ },
                    badgeIcon = Icons.Outlined.Campaign
                )
            }

            // Example 3: Upcoming Meeting
            item {
                Text(
                    text = "Upcoming Meeting",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
            }

            item {
                HeroUpcomingMeetingCard(
                    meetingTitle = "Weekly Team Sync",
                    meetingDetails = "Tomorrow at 9:00 AM • Conference Room A",
                    onJoinClick = { /* Join meeting */ },
                    badgeText = "Soon"
                )
            }

            // Example 4: Custom Gradient
            item {
                Text(
                    text = "Custom Styling",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
            }

            item {
                HeroHighlightCard(
                    data = HeroHighlightCardData(
                        title = "Custom Styled Card",
                        subtitle = "This card uses custom gradient colors",
                        actionText = "Explore",
                        badgeText = "New"
                    ),
                    onActionClick = { /* Custom action */ },
                    gradientColors = listOf(
                        SoftUIColors.PeachGradientStart,
                        SoftUIColors.PeachGradientEnd
                    ),
                    actionButtonColor = SoftUIColors.AccentPeach
                )
            }

            // Example 5: With Icon Badge
            item {
                Text(
                    text = "With Icon Badge",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
            }

            item {
                HeroHighlightCard(
                    data = HeroHighlightCardData(
                        title = "Featured Event",
                        subtitle = "Don't miss this important event happening soon",
                        actionText = "Learn More",
                        badgeIcon = Icons.Outlined.Event
                    ),
                    onActionClick = { /* Event action */ },
                    gradientColors = listOf(
                        SoftUIColors.CoralGradientStart,
                        SoftUIColors.CoralGradientEnd
                    ),
                    actionButtonColor = SoftUIColors.AccentCoral
                )
            }
        }
    }
}

/**
 * Real-world example: Dashboard screen with hero card
 */
@Composable
fun DashboardWithHeroExample() {
    Scaffold(
        containerColor = BackgroundPrimary
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = AppSpacing.screenHorizontal,
                end = AppSpacing.screenHorizontal,
                top = AppSpacing.largeSpacing,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionSpacing)
        ) {
            // Greeting
            item {
                SoftGreetingHeader(
                    greeting = "Good Morning, Professor!",
                    subtitle = "You have 3 classes today"
                )
            }

            // Hero Card - Next Class
            item {
                HeroUpcomingClassCard(
                    className = "Physics 101",
                    timeAndLocation = "Starts in 30 minutes • Lab Building 202",
                    onViewClick = { /* Navigate to class */ },
                    badgeText = "Next"
                )
            }

            // Quick Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SoftStatChip(
                        icon = Icons.Outlined.School,
                        label = "3 Classes",
                        color = SoftUIColors.AccentLavender,
                        modifier = Modifier.weight(1f)
                    )
                    SoftStatChip(
                        icon = Icons.Outlined.Event,
                        label = "2 Meetings",
                        color = SoftUIColors.AccentBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Today's Schedule Section
            item {
                Text(
                    text = "Today's Schedule",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
            }
        }
    }
}
