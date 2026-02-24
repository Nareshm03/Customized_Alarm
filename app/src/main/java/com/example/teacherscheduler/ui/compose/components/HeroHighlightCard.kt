package com.example.teacherscheduler.ui.compose.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teacherscheduler.ui.theme.*

/**
 * HeroHighlightCard - Premium featured card with visual richness
 *
 * Features:
 * - Large rounded corners (24dp)
 * - Soft pastel gradient background
 * - Decorative background shapes (very light opacity)
 * - Title (large text)
 * - Subtitle (light text)
 * - Action button (pill style)
 * - Optional badge on right
 * - Soft shadow
 * - Subtle animations
 *
 * Use cases:
 * - Upcoming class highlight
 * - Important notice banner
 * - Next meeting preview
 */

data class HeroHighlightCardData(
    val title: String,
    val subtitle: String,
    val actionText: String,
    val badgeText: String? = null,
    val badgeIcon: ImageVector? = null
)

/**
 * HeroHighlightCard with gradient background
 */
@Composable
fun HeroHighlightCard(
    data: HeroHighlightCardData,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(
        SoftUIColors.LavenderGradientStart,
        SoftUIColors.LavenderGradientEnd
    ),
    actionButtonColor: Color = SoftUIColors.AccentLavender
) {
    // Subtle pulsing animation for decorative shapes
    val infiniteTransition = rememberInfiniteTransition(label = "heroCardPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // Entrance animation
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "heroCardScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400),
        label = "heroCardAlpha"
    )

    SoftCard(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha),
        cornerRadius = 24.dp,
        elevation = 4.dp,
        onClick = null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = gradientColors.firstOrNull() ?: Color.White
                )
                .padding(SoftLayoutDimens.cardPadding + 4.dp)
        ) {
            // Decorative background shapes (very subtle)
            DecorativeBackgroundShapes(
                pulseAlpha = pulseAlpha,
                rotationAngle = rotationAngle
            )

            // Main content
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with optional badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Title and subtitle
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = data.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 22.sp,
                                letterSpacing = 0.sp
                            ),
                            color = Color.White.copy(alpha = 0.95f)
                        )

                        Text(
                            text = data.subtitle,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp
                            ),
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }

                    // Optional badge
                    if (data.badgeText != null || data.badgeIcon != null) {
                        HeroBadge(
                            text = data.badgeText,
                            icon = data.badgeIcon
                        )
                    }
                }

                // Action button
                HeroActionButton(
                    text = data.actionText,
                    onClick = onActionClick,
                    buttonColor = actionButtonColor
                )
            }
        }
    }
}

/**
 * Decorative background shapes - very subtle
 */
@Composable
private fun BoxScope.DecorativeBackgroundShapes(
    pulseAlpha: Float,
    rotationAngle: Float
) {
    // Large circle - top right
    Box(
        modifier = Modifier
            .size(180.dp)
            .align(Alignment.TopEnd)
            .offset(x = 60.dp, y = (-40).dp)
            .rotate(rotationAngle)
            .alpha(pulseAlpha * 0.15f)
            .clip(RoundedCornerShape(50))
            .background(Color.White)
    )

    // Medium circle - bottom left
    Box(
        modifier = Modifier
            .size(120.dp)
            .align(Alignment.BottomStart)
            .offset(x = (-30).dp, y = 30.dp)
            .rotate(-rotationAngle)
            .alpha(pulseAlpha * 0.12f)
            .clip(RoundedCornerShape(50))
            .background(Color.White)
    )

    // Small accent shape - center right
    Box(
        modifier = Modifier
            .size(80.dp)
            .align(Alignment.CenterEnd)
            .offset(x = 20.dp)
            .rotate(rotationAngle * 1.5f)
            .alpha(pulseAlpha * 0.1f)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
    )
}

/**
 * Hero badge component
 */
@Composable
private fun HeroBadge(
    text: String?,
    icon: ImageVector?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.25f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(16.dp)
                )
            }

            if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

/**
 * Hero action button - pill style
 */
@Composable
private fun HeroActionButton(
    text: String,
    onClick: () -> Unit,
    buttonColor: Color,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = buttonColor,
        shadowElevation = 2.dp,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                ),
                color = Color.White
            )
        }
    }
}

// ============================================================================
// PRESET VARIANTS
// ============================================================================

/**
 * HeroHighlightCard variant for upcoming class
 */
@Composable
fun HeroUpcomingClassCard(
    className: String,
    timeAndLocation: String,
    onViewClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = "Next"
) {
    HeroHighlightCard(
        data = HeroHighlightCardData(
            title = className,
            subtitle = timeAndLocation,
            actionText = "View Details",
            badgeText = badgeText
        ),
        onActionClick = onViewClick,
        modifier = modifier,
        gradientColors = listOf(
            SoftUIColors.LavenderGradientStart,
            SoftUIColors.LavenderGradientEnd
        ),
        actionButtonColor = SoftUIColors.AccentLavender
    )
}

/**
 * HeroHighlightCard variant for important notice
 */
@Composable
fun HeroImportantNoticeCard(
    noticeTitle: String,
    noticePreview: String,
    onReadClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeIcon: ImageVector? = null
) {
    HeroHighlightCard(
        data = HeroHighlightCardData(
            title = noticeTitle,
            subtitle = noticePreview,
            actionText = "Read More",
            badgeIcon = badgeIcon
        ),
        onActionClick = onReadClick,
        modifier = modifier,
        gradientColors = listOf(
            SoftUIColors.BlueGradientStart,
            SoftUIColors.BlueGradientEnd
        ),
        actionButtonColor = SoftUIColors.AccentBlue
    )
}

/**
 * HeroHighlightCard variant for upcoming meeting
 */
@Composable
fun HeroUpcomingMeetingCard(
    meetingTitle: String,
    meetingDetails: String,
    onJoinClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeText: String? = "Soon"
) {
    HeroHighlightCard(
        data = HeroHighlightCardData(
            title = meetingTitle,
            subtitle = meetingDetails,
            actionText = "View Meeting",
            badgeText = badgeText
        ),
        onActionClick = onJoinClick,
        modifier = modifier,
        gradientColors = listOf(
            SoftUIColors.MintGradientStart,
            SoftUIColors.MintGradientEnd
        ),
        actionButtonColor = SoftUIColors.AccentMint
    )
}


