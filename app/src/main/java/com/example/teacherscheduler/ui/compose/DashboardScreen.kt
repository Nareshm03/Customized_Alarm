package com.example.teacherscheduler.ui.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToAddClass: () -> Unit,
    onNavigateToAddMeeting: () -> Unit
) {
    val uiState by viewModel.dashboardState.collectAsStateWithLifecycle()
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
                start = AppSpacing.screenHorizontal,
                end = AppSpacing.screenHorizontal,
                top = AppSpacing.largeSpacing,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.screenHorizontal)
        ) {
            // Top Row: Profile + Greeting + Notification
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { -20 }
                ) {
                    MinimalTopArea(
                        greeting = uiState.greeting,
                        subtitle = getCurrentDate()
                    )
                }
            }

            // Hero Card: Next Class
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400, 100)) + slideInVertically(tween(400, 100)) { 30 }
                ) {
                    NextClassHeroCard(
                        nextClass = uiState.todayClasses.firstOrNull(),
                        totalClassesToday = uiState.todayClassesCount
                    )
                }
            }

            // Section: Ongoing
            item {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn(tween(400, 200)) + slideInVertically(tween(400, 200)) { 30 }
                ) {
                    Text(
                        text = "Ongoing",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextPrimary
                    )
                }
            }

            // Grid: 2-Column Class Cards
            if (uiState.todayClasses.isNotEmpty()) {
                itemsIndexed(
                    items = uiState.todayClasses.chunked(2),
                    key = { index, _ -> "row_$index" }
                ) { rowIndex, rowItems ->
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(tween(400, 300 + rowIndex * 50)) + 
                                slideInVertically(tween(400, 300 + rowIndex * 50)) { 30 }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sectionSpacing)
                        ) {
                            rowItems.forEachIndexed { index, classItem ->
                                ClassGridCard(
                                    classItem = classItem,
                                    modifier = Modifier.weight(1f),
                                    accentColor = if (index % 2 == 0) 
                                        SoftUIColors.AccentLavender else SoftUIColors.AccentBlue,
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

            if (uiState.todayClasses.isEmpty() && !uiState.isLoading) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.School,
                        title = "No Classes Today",
                        subtitle = "Enjoy your free day!"
                    )
                }
            }
        }

        // FAB
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(tween(300, 400)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(AppSpacing.screenHorizontal)
        ) {
            IconCircleButton(
                icon = Icons.Default.Add,
                onClick = onNavigateToAddClass,
                contentDescription = "Add Class",
                size = 56.dp
            )
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFD8B4A0))
            }
        }
    }
}// ============================================================================
// MINIMAL TOP AREA
// ============================================================================
@Composable
private fun MinimalTopArea(
    greeting: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sectionSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SoftProfileAvatar(
                size = 52.dp,
                icon = Icons.Outlined.Person,
                gradientColors = listOf(Color(0xFFF7F4EF), Color(0xFFFAF7F2)),
                iconTint = Color(0xFF2B2B2B),
                borderWidth = 0.dp
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium.copy(
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
        }
        IconCircleButton(
            icon = Icons.Outlined.Notifications,
            onClick = { },
            contentDescription = "Notifications"
        )
    }
}

@Composable
private fun getCurrentDate(): String {
    val calendar = java.util.Calendar.getInstance()
    val dateFormat = java.text.SimpleDateFormat("EEEE, MMM d", java.util.Locale.getDefault())
    return dateFormat.format(calendar.time)
}

// ============================================================================
// HERO CARD - Next Class
// ============================================================================
@Composable
private fun NextClassHeroCard(
    nextClass: Class?,
    totalClassesToday: Int
) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "NEXT CLASS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondary
                )
                Text(
                    text = nextClass?.title ?: "No classes",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (nextClass != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = nextClass.getFormattedTime(),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text("•", color = TextTertiary)
                        Text(
                            text = nextClass.room,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                if (nextClass != null) {
                    PrimaryButton(
                        text = "Join",
                        onClick = { },
                        modifier = Modifier.fillMaxWidth(0.4f)
                    )
                }
            }
            
            if (totalClassesToday > 0) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = totalClassesToday.toString(),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "today",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

// ============================================================================
// CLASS GRID CARD - 2 Column Layout
// ============================================================================
@Composable
private fun ClassGridCard(
    classItem: Class,
    modifier: Modifier = Modifier,
    accentColor: Color = SoftUIColors.AccentLavender,
    gradientColors: List<Color> = listOf(SoftUIColors.LavenderGradientStart, SoftUIColors.LavenderGradientEnd)
) {
    PremiumCard(
        modifier = modifier.aspectRatio(0.85f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF7F4EF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = null,
                    tint = Color(0xFF2B2B2B),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = classItem.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
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
            
            Text(
                text = classItem.getFormattedTime(),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
