package com.example.teacherscheduler.ui.compose.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teacherscheduler.ui.theme.*

/**
 * Soft Layout System for Teacher Scheduler
 *
 * Design Language:
 * - Soft light UI with white/off-white backgrounds
 * - Large spacing (24dp outer, 16dp between sections, 12dp inside cards)
 * - Section-based layout with clear hierarchy
 * - Modern minimal style inspired by premium iOS productivity apps
 *
 * Typography:
 * - Greeting → Large headline
 * - Section titles → Medium weight
 * - Body → Regular
 * - Avoid overusing bold
 *
 * Spacing Constants:
 * - Outer padding: 24dp
 * - Top spacing: 32dp
 * - Section spacing: 16dp
 * - Card inner padding: 12-16dp
 * - Card corner radius: 24dp
 */

// ============================================================================
// LAYOUT CONSTANTS
// ============================================================================

object SoftLayoutDimens {
    val screenPadding = AppSpacing.screenHorizontal
    val topSpacing = AppSpacing.largeSpacing
    val sectionSpacing = AppSpacing.sectionSpacing
    val sectionTitleSpacing = 12.dp
    val cardPadding = AppSpacing.cardPadding
    val cardInnerPadding = 12.dp
    val cardCornerRadius = AppRadius.card
    val cardSpacing = 12.dp
    val itemSpacing = 12.dp
    val textSpacing = 4.dp
    val bottomSafeArea = 100.dp
}

// ============================================================================
// SCREEN CONTAINER
// ============================================================================

/**
 * SoftScreenContainer - Base container for all screens
 *
 * Features:
 * - White/soft background
 * - Proper top spacing (32dp)
 * - 24dp horizontal padding
 * - Fade-in animation on entry
 */
@Composable
fun SoftScreenContainer(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        containerColor = BackgroundPrimary,
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        modifier = modifier
    ) { padding ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            ) + slideInVertically(
                animationSpec = tween(400, easing = FastOutSlowInEasing),
                initialOffsetY = { 20 }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = SoftLayoutDimens.screenPadding)
                    .padding(top = SoftLayoutDimens.topSpacing),
                content = content
            )
        }
    }
}

/**
 * SoftScrollableScreen - Scrollable screen with LazyColumn
 */
@Composable
fun SoftScrollableScreen(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: LazyListScope.() -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Scaffold(
        containerColor = BackgroundPrimary,
        topBar = topBar,
        floatingActionButton = floatingActionButton,
        modifier = modifier
    ) { padding ->
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                animationSpec = tween(400, easing = FastOutSlowInEasing)
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(
                    start = SoftLayoutDimens.screenPadding,
                    end = SoftLayoutDimens.screenPadding,
                    top = SoftLayoutDimens.topSpacing,
                    bottom = SoftLayoutDimens.bottomSafeArea
                ),
                verticalArrangement = Arrangement.spacedBy(SoftLayoutDimens.sectionSpacing),
                content = content
            )
        }
    }
}

// ============================================================================
// MINIMAL TOP BAR
// ============================================================================

/**
 * SoftMinimalTopBar - Clean minimal top bar (no heavy colors)
 *
 * Features:
 * - Transparent/white background
 * - Simple title
 * - Optional back button
 * - No heavy colored bars
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoftMinimalTopBar(
    title: String = "",
    modifier: Modifier = Modifier,
    showBackButton: Boolean = false,
    onBackClick: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
            }
        },
        modifier = modifier,
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.White.copy(alpha = 0.95f),
            navigationIconContentColor = TextPrimary,
            titleContentColor = TextPrimary,
            actionIconContentColor = TextSecondary
        )
    )
}

// ============================================================================
// SECTION COMPONENTS
// ============================================================================

/**
 * SoftSection - A section with title and content
 *
 * Features:
 * - Subtle section title (medium weight)
 * - Proper spacing
 * - Optional "See More" action
 */
@Composable
fun SoftSection(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SoftLayoutDimens.sectionTitleSpacing)
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = TextPrimary
            )

            if (actionText != null && onActionClick != null) {
                TextButton(
                    onClick = onActionClick,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = SoftUIColors.AccentLavender
                    )
                }
            }
        }

        // Section Content
        Column(
            verticalArrangement = Arrangement.spacedBy(SoftLayoutDimens.cardSpacing),
            content = content
        )
    }
}

/**
 * SoftSectionTitle - Standalone section title
 */
@Composable
fun SoftSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            ),
            color = TextPrimary
        )

        if (actionText != null && onActionClick != null) {
            TextButton(
                onClick = onActionClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = SoftUIColors.AccentLavender
                )
            }
        }
    }
}

// ============================================================================
// GREETING HEADER
// ============================================================================

/**
 * SoftGreetingHeader - Large greeting headline
 *
 * Features:
 * - Large headline text
 * - Optional subtitle
 * - Profile avatar option
 */
@Composable
fun SoftGreetingHeader(
    greeting: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    showProfile: Boolean = false,
    onProfileClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp
                ),
                color = TextPrimary
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        }

        if (showProfile && onProfileClick != null) {
            SoftProfileAvatar(
                onClick = onProfileClick
            )
        }
    }
}

// ============================================================================
// CONTENT CARDS
// ============================================================================

/**
 * SoftContentCard - Large rounded card with proper spacing
 *
 * Features:
 * - 24dp corner radius
 * - Soft shadow
 * - White background
 * - 16dp internal padding
 */
@Composable
fun SoftContentCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    PremiumCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        content = content
    )
}

/**
 * SoftHighlightCard - Featured card with gradient background
 */
@Composable
fun SoftHighlightCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    gradientColors: List<Color> = listOf(
        SoftUIColors.LavenderGradientStart,
        SoftUIColors.LavenderGradientEnd
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    PremiumCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        gradientColors = gradientColors,
        content = content
    )
}

// ============================================================================
// LIST ITEMS
// ============================================================================

/**
 * SoftListItemCard - Clean list item in card style
 */
@Composable
fun SoftListItemCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconBackgroundColor: Color = SoftUIColors.ChipBackground,
    iconTint: Color = SoftUIColors.AccentLavender,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    SoftContentCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SoftLayoutDimens.itemSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(SoftLayoutDimens.textSpacing)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )

                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // Trailing
            if (trailing != null) {
                trailing()
            }
        }
    }
}

// ============================================================================
// EMPTY STATE
// ============================================================================

/**
 * SoftEmptyStateView - Clean empty state with minimal design
 */
@Composable
fun SoftEmptyStateView(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SoftLayoutDimens.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Icon
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(SoftUIColors.ChipBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = TextSecondary
        )

        // Subtitle
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary
            )
        }

        // Action
        if (action != null) {
            Spacer(modifier = Modifier.height(8.dp))
            action()
        }
    }
}

// ============================================================================
// STAGGERED ANIMATION HELPER
// ============================================================================

/**
 * StaggeredItem - Wrapper for staggered animation in lists
 */
@Composable
fun StaggeredItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = index * 50,
                easing = FastOutSlowInEasing
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = index * 50,
                easing = FastOutSlowInEasing
            ),
            initialOffsetY = { 30 }
        ),
        modifier = modifier
    ) {
        content()
    }
}
