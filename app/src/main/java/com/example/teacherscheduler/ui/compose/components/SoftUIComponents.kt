package com.example.teacherscheduler.ui.compose.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teacherscheduler.ui.theme.*

/**
 * Soft UI Components for Teacher Scheduler
 *
 * Design Philosophy:
 * - Apple-inspired clean UI
 * - Soft light backgrounds (off-white / light neutral)
 * - Large rounded cards (24dp radius)
 * - Subtle pastel gradients
 * - Soft shadows (low elevation)
 * - Smooth micro-animations
 * - No harsh ripples, minimal heavy outlines
 * - Clean and airy appearance
 */

// ============================================================================
// COLOR DEFINITIONS
// ============================================================================
object SoftUIColors {
    // ── Single accent: iOS Blue ──────────────────────────────────────────
    val AccentPrimary = Color(0xFF007AFF)
    val AccentSecondary = Color(0xFF34C759)   // success only
    val AccentBlue = AccentPrimary
    val AccentMint = AccentSecondary
    val AccentPeach = Color(0xFFFF9500)
    val AccentCoral = Color(0xFFFF3B30)
    val AccentRose = Color(0xFFFF2D55)
    val AccentSky = Color(0xFF5AC8FA)

    // Backward-compat alias — NO purple, maps to primary blue
    val AccentLavender = AccentPrimary

    // ── Tint backgrounds (no heavy gradients) ───────────────────────────
    val BlueTint = Color(0xFFE8F0FE)          // light blue tint for chips/icons
    val GreenTint = Color(0xFFE8F8EE)
    val OrangeTint = Color(0xFFFFF3E0)

    // Legacy gradient names → flat tints (kept for callers that still pass lists)
    val WarmGradientStart = BlueTint
    val WarmGradientEnd = BlueTint
    val BlueGradientStart = BlueTint
    val BlueGradientEnd = BlueTint
    val MintGradientStart = GreenTint
    val MintGradientEnd = GreenTint
    val PeachGradientStart = OrangeTint
    val PeachGradientEnd = OrangeTint
    val PinkGradientStart = BlueTint
    val PinkGradientEnd = BlueTint
    val CoralGradientStart = BlueTint
    val CoralGradientEnd = BlueTint
    val RoseGradientStart = BlueTint
    val RoseGradientEnd = BlueTint
    val SkyGradientStart = BlueTint
    val SkyGradientEnd = BlueTint
    val LavenderGradientStart = BlueTint
    val LavenderGradientEnd = BlueTint

    // ── Button colors ───────────────────────────────────────────────────
    val ButtonBackground = Color(0xFF007AFF)
    val ButtonBackgroundPressed = Color(0xFF0062CC)
    val ButtonText = Color(0xFFFFFFFF)

    // ── Surface colors ──────────────────────────────────────────────────
    val CardBackground = Color(0xFFFFFFFF)
    val ChipBackground = Color(0xFFF2F2F7)
    val ChipBackgroundSelected = Color(0xFFE8F0FE)
    val SoftGrey = Color(0xFFF2F2F7)
    val LightNeutral = Color(0xFFE5E5EA)

    // ── Shadow colors ───────────────────────────────────────────────────
    val SoftShadow = Color(0x0A000000)
    val MediumShadow = Color(0x14000000)
    val CardShadow = Color(0x0F000000)
}

// ============================================================================
// SOFT CARD
// ============================================================================

/**
 * SoftCard - A clean card with 24dp radius, soft shadow, and white background
 *
 * Features:
 * - 24dp corner radius (configurable)
 * - Soft shadow with low elevation
 * - White background by default
 * - Optional gradient support
 * - Smooth press animation (scale down on press)
 * - No harsh ripple effect
 *
 * @param modifier Modifier for the card
 * @param onClick Optional click handler
 * @param cornerRadius Corner radius (default 24dp)
 * @param elevation Shadow elevation (default 4dp for soft shadow)
 * @param containerColor Background color (default white)
 * @param gradientColors Optional gradient colors (overrides containerColor if set)
 * @param contentPadding Padding inside the card (default 16dp)
 * @param content Card content
 */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 24.dp,
    elevation: Dp = Elevation.level1,
    containerColor: Color = SoftUIColors.CardBackground,
    gradientColors: List<Color>? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    PremiumCard(
        modifier = modifier,
        onClick = onClick,
        gradientColors = gradientColors,
        content = content
    )
}

// ============================================================================
// GRADIENT HIGHLIGHT CARD
// ============================================================================

/**
 * GradientHighlightCard - A prominent card with soft pastel gradient background
 *
 * Features:
 * - Soft pastel gradient (not aggressive)
 * - Rounded corners (24dp default)
 * - Minimal soft shadow
 * - Support for overlay text (label, title, subtitle)
 * - Trailing content area (badges, progress indicators)
 * - Bottom content area (action buttons)
 * - Smooth press animation
 *
 * @param modifier Modifier for the card
 * @param onClick Optional click handler
 * @param gradientColors Gradient colors for background (pastel recommended)
 * @param cornerRadius Corner radius (default 24dp)
 * @param elevation Shadow elevation (default 8dp)
 * @param overlayLabel Optional small label text at top (e.g., "NEXT CLASS")
 * @param title Main title text
 * @param subtitle Optional subtitle text
 * @param accentColor Color for accent elements (label, badge)
 * @param trailingContent Optional content on the right side
 * @param bottomContent Optional content at the bottom
 */
@Composable
fun GradientHighlightCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    gradientColors: List<Color> = listOf(
        SoftUIColors.LavenderGradientStart,
        SoftUIColors.LavenderGradientEnd
    ),
    cornerRadius: Dp = 24.dp,
    elevation: Dp = Elevation.level2,
    overlayLabel: String? = null,
    title: String,
    subtitle: String? = null,
    accentColor: Color = SoftUIColors.AccentLavender,
    trailingContent: @Composable (() -> Unit)? = null,
    bottomContent: @Composable (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth scale animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) SoftAnimations.pressScale else 1f,
        animationSpec = tween(SoftAnimations.pressDuration),
        label = "highlightScale"
    )

    // Subtle elevation change
    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) elevation * 0.5f else elevation,
        animationSpec = tween(SoftAnimations.pressDuration),
        label = "highlightElevation"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = animatedElevation,
                shape = shape,
                ambientColor = SoftUIColors.SoftShadow,
                spotColor = SoftUIColors.SoftShadow
            )
            .clip(shape)
            .background(gradientColors.firstOrNull() ?: SoftUIColors.CardBackground)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(24.dp)
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
                // Overlay Label
                if (overlayLabel != null) {
                    Text(
                        text = overlayLabel.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = accentColor
                    )
                }

                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp
                    ),
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Subtitle
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom Content
                if (bottomContent != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    bottomContent()
                }
            }

            // Trailing Content
            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(16.dp))
                trailingContent()
            }
        }
    }
}

/**
 * GradientHighlightCard with custom content
 */
@Composable
fun GradientHighlightCardCustom(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    gradientColors: List<Color> = listOf(
        SoftUIColors.LavenderGradientStart,
        SoftUIColors.LavenderGradientEnd
    ),
    cornerRadius: Dp = 24.dp,
    elevation: Dp = Elevation.level2,
    accentColor: Color = SoftUIColors.AccentLavender,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    PremiumCard(
        modifier = modifier,
        onClick = onClick,
        gradientColors = gradientColors
    ) {
        Box(
            modifier = Modifier.padding(8.dp),
            content = content
        )
    }
}

// ============================================================================
// ROUNDED PRIMARY BUTTON
// ============================================================================

/**
 * RoundedPrimaryButton - A pill-shaped button with soft gradient background
 *
 * Features:
 * - Pill shaped (full rounded corners)
 * - Soft gradient background (lavender by default)
 * - Subtle elevation (soft shadow)
 * - No harsh ripple effect
 * - Smooth scale animation on press
 * - Optional leading icon
 *
 * @param text Button text
 * @param onClick Click handler
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 * @param icon Optional leading icon
 * @param gradientColors Gradient colors for background (soft lavender by default)
 * @param cornerRadius Corner radius (default 50dp for pill shape)
 * @param contentPadding Padding inside the button
 */
@Composable
fun RoundedPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    cornerRadius: Dp = 14.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = tween(150),
        label = "buttonScale"
    )

    Box(
        modifier = modifier
            .height(52.dp)
            .scale(scale)
            .clip(RoundedCornerShape(cornerRadius))
            .background(if (enabled) Color(0xFF007AFF) else Color(0xFFD1D1D6))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (enabled) Color.White else Color(0xFF9E9E9E)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (enabled) Color.White else Color(0xFF9E9E9E)
            )
        }
    }
}

/**
 * RoundedSecondaryButton - Outlined version of the pill button
 */
@Composable
fun RoundedSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    borderColor: Color = SoftUIColors.AccentPrimary,
    cornerRadius: Dp = 50.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) SoftAnimations.pressScale else 1f,
        animationSpec = tween(SoftAnimations.pressDuration),
        label = "buttonScale"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(
                if (isPressed) borderColor.copy(alpha = 0.05f)
                else Color.Transparent
            )
            .then(
                Modifier.shadow(
                    elevation = 0.dp,
                    shape = shape
                )
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(1.dp)
            .background(
                borderColor,
                shape = shape
            )
            .padding(1.dp)
            .background(
                SoftUIColors.CardBackground,
                shape = shape
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (enabled) borderColor else TextTertiary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = if (enabled) borderColor else TextTertiary
            )
        }
    }
}

// ============================================================================
// SOFT CHIP
// ============================================================================

/**
 * SoftChip - A rounded chip with light grey background and small padding
 *
 * @param text Chip text
 * @param modifier Modifier for the chip
 * @param selected Whether the chip is selected
 * @param onClick Optional click handler
 * @param icon Optional leading icon
 * @param backgroundColor Background color (default light grey)
 * @param selectedBackgroundColor Background color when selected
 * @param textColor Text color
 * @param selectedTextColor Text color when selected
 * @param cornerRadius Corner radius (default 12dp)
 */
@Composable
fun SoftChip(
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    icon: ImageVector? = null,
    backgroundColor: Color = SoftUIColors.ChipBackground,
    selectedBackgroundColor: Color = SoftUIColors.AccentPrimary.copy(alpha = 0.15f),
    textColor: Color = TextSecondary,
    selectedTextColor: Color = SoftUIColors.AccentPrimary,
    cornerRadius: Dp = AppRadius.chip
) {
    val interactionSource = remember { MutableInteractionSource() }

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (selected) selectedBackgroundColor else backgroundColor,
        animationSpec = tween(durationMillis = 250, easing = EaseInOutCubic),
        label = "chipBg"
    )

    val animatedTextColor by animateColorAsState(
        targetValue = if (selected) selectedTextColor else textColor,
        animationSpec = tween(durationMillis = 250, easing = EaseInOutCubic),
        label = "chipText"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(animatedBackgroundColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = animatedTextColor
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = animatedTextColor,
                maxLines = 1
            )
        }
    }
}

/**
 * SoftChip with color accent
 */
@Composable
fun SoftColorChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 10.dp
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(color.copy(alpha = 0.12f))
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = color,
            maxLines = 1
        )
    }
}

// ============================================================================
// SOFT ICON BUTTON
// ============================================================================

/**
 * SoftIconButton - A rounded icon button with subtle background
 */
@Composable
fun SoftIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    backgroundColor: Color = SoftUIColors.CardBackground,
    iconColor: Color = TextSecondary,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    cornerRadius: Dp = 14.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) SoftAnimations.pressScale else 1f,
        animationSpec = tween(SoftAnimations.pressDuration),
        label = "iconButtonScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = iconColor
        )
    }
}

// ============================================================================
// SOFT PROGRESS INDICATOR
// ============================================================================

/**
 * SoftProgressBadge - A rounded badge showing progress
 */
@Composable
fun SoftProgressBadge(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    valueColor: Color = SoftUIColors.AccentLavender,
    backgroundColor: Color = Color.White.copy(alpha = 0.8f),
    size: Dp = 72.dp,
    cornerRadius: Dp = 20.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = valueColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

// ============================================================================
// SOFT SECTION HEADER
// ============================================================================

/**
 * SoftSectionHeader - A section header with title and optional "See More" action
 */
@Composable
fun SoftSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = "See More",
    onActionClick: (() -> Unit)? = null,
    accentColor: Color = SoftUIColors.AccentLavender
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            ),
            color = TextPrimary
        )
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = accentColor
                )
            }
        }
    }
}

// ============================================================================
// SOFT EMPTY STATE
// ============================================================================

/**
 * SoftEmptyStateCard - A card showing empty state with icon, title, and subtitle
 */
@Composable
fun SoftEmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    height: Dp = 140.dp,
    cornerRadius: Dp = 24.dp
) {
    PremiumCard(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = TextTertiary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ============================================================================
// SOFT STAT CHIP ROW
// ============================================================================

/**
 * SoftStatChip - A compact stat display chip
 */
@Composable
fun SoftStatChip(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    PremiumCard(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}

// ============================================================================
// ANIMATION HELPERS
// ============================================================================

object SoftAnimations {
    // Fade in with slide up - for cards appearing
    val fadeInSlideUp = fadeIn(animationSpec = tween(400, easing = EaseOutCubic)) +
            slideInVertically(animationSpec = tween(400, easing = EaseOutCubic)) { 40 }

    // Delayed variations for staggered animations
    fun fadeInSlideUpDelayed(delayMillis: Int) = fadeIn(
        animationSpec = tween(400, delayMillis = delayMillis, easing = EaseOutCubic)
    ) + slideInVertically(
        animationSpec = tween(400, delayMillis = delayMillis, easing = EaseOutCubic)
    ) { 40 }

    // Scale in for FABs
    val scaleIn = scaleIn(animationSpec = tween(300, easing = EaseOutBack))

    // Standard spring for press animations
    val pressSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    // Premium interaction polish
    val pressScale = 0.97f
    val pressDuration = 150
    val fadeTransition = tween<Float>(300, easing = LinearOutSlowInEasing)
    val stateTransition = tween<Float>(250, easing = EaseInOutCubic)
}


// ============================================================================
// SOFT FLOATING ACTION BUTTON
// ============================================================================

/**
 * SoftFAB - A floating action button with soft styling
 */
@Composable
fun SoftFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    containerColor: Color = SoftUIColors.AccentPrimary,
    contentColor: Color = Color.White,
    size: Dp = 56.dp,
    iconSize: Dp = 24.dp,
    elevation: Dp = 6.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) SoftAnimations.pressScale else 1f,
        animationSpec = tween(SoftAnimations.pressDuration),
        label = "fabScale"
    )

    val animatedElevation by animateDpAsState(
        targetValue = if (isPressed) elevation * 0.5f else elevation,
        animationSpec = tween(SoftAnimations.pressDuration),
        label = "fabElevation"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(
                elevation = animatedElevation,
                shape = RoundedCornerShape(16.dp),
                ambientColor = SoftUIColors.SoftShadow,
                spotColor = SoftUIColors.SoftShadow
            )
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = contentColor
        )
    }
}

/**
 * SoftSmallFAB - A smaller floating action button
 */
@Composable
fun SoftSmallFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    containerColor: Color = SoftUIColors.ChipBackgroundSelected,
    contentColor: Color = SoftUIColors.AccentPrimary,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) SoftAnimations.pressScale else 1f,
        animationSpec = tween(SoftAnimations.pressDuration),
        label = "smallFabScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(
                elevation = Elevation.level1,
                shape = RoundedCornerShape(16.dp),
                ambientColor = SoftUIColors.SoftShadow,
                spotColor = SoftUIColors.SoftShadow
            )
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = contentColor
        )
    }
}

// ============================================================================
// SOFT PROFILE AVATAR
// ============================================================================

/**
 * SoftProfileAvatar - A circular profile image with gradient background
 */
@Composable
fun SoftProfileAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    icon: ImageVector = Icons.Outlined.Person,
    gradientColors: List<Color> = listOf(
        SoftUIColors.LavenderGradientStart,
        SoftUIColors.LavenderGradientEnd
    ),
    iconTint: Color = SoftUIColors.AccentLavender,
    borderColor: Color = Color.White,
    borderWidth: Dp = 2.dp,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) SoftAnimations.pressScale else 1f,
        animationSpec = tween(SoftAnimations.pressDuration),
        label = "avatarScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(gradientColors.firstOrNull() ?: SoftUIColors.BlueTint)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, borderColor, CircleShape)
                } else Modifier
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Profile",
            tint = iconTint,
            modifier = Modifier.size(size * 0.54f)
        )
    }
}

// ============================================================================
// SOFT GRID CARD (Two-Column Layout)
// ============================================================================

/**
 * SoftGridCard - A card designed for two-column grid layouts
 * Used for displaying class cards with icon, title, subtitle, tags, and progress area
 */
@Composable
fun SoftGridCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    icon: ImageVector,
    iconGradientColors: List<Color> = listOf(
        SoftUIColors.LavenderGradientStart,
        SoftUIColors.LavenderGradientEnd
    ),
    iconTint: Color = SoftUIColors.AccentLavender,
    title: String,
    subtitle: String,
    tags: List<String> = emptyList(),
    tagColor: Color = SoftUIColors.AccentLavender,
    bottomLabel: String? = null,
    bottomGradientColors: List<Color> = listOf(
        SoftUIColors.LavenderGradientStart,
        SoftUIColors.LavenderGradientEnd
    )
) {
    SoftCard(
        modifier = modifier.aspectRatio(0.9f),
        cornerRadius = 24.dp,
        elevation = Elevation.level1,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Circular icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconGradientColors.firstOrNull() ?: SoftUIColors.BlueTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Middle: Title and Subtitle
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom: Tags and Progress area
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tags row
                if (tags.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tags.take(2).forEachIndexed { index, tag ->
                            SoftColorChip(
                                text = tag,
                                color = if (index == 0) tagColor else tagColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Bottom progress/label area
                if (bottomLabel != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bottomGradientColors.firstOrNull() ?: SoftUIColors.BlueTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = bottomLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = tagColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// SOFT LIST CARD
// ============================================================================

/**
 * SoftListCard - A horizontal card for list items (like meetings)
 */
@Composable
fun SoftListCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    icon: ImageVector,
    iconGradientColors: List<Color> = listOf(
        SoftUIColors.BlueGradientStart,
        SoftUIColors.BlueGradientEnd
    ),
    iconTint: Color = SoftUIColors.AccentBlue,
    title: String,
    subtitle: String,
    trailingText: String? = null,
    trailingChipSelected: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(150),
        label = "listCardScale"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 2.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = SoftUIColors.SoftShadow,
                spotColor = SoftUIColors.SoftShadow
            )
            .clip(RoundedCornerShape(20.dp))
            .background(SoftUIColors.CardBackground)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = SoftUIColors.AccentLavender.copy(alpha = 0.1f)),
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconGradientColors.firstOrNull() ?: SoftUIColors.BlueTint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Trailing chip
            if (trailingText != null) {
                SoftChip(
                    text = trailingText,
                    selected = trailingChipSelected,
                    selectedBackgroundColor = SecondaryContainer,
                    selectedTextColor = OnSecondaryContainer
                )
            }
        }
    }
}

// ============================================================================
// SOFT SECTION
// ============================================================================

/**
 * SoftSection - A section container with title and content
 */
@Composable
fun SoftSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = TextPrimary
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

// ============================================================================
// SOFT CONTENT CARD
// ============================================================================

/**
 * SoftContentCard - A simple white card with rounded corners
 */
@Composable
fun SoftContentCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 20.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) SoftAnimations.pressScale else 1f,
        animationSpec = tween(SoftAnimations.pressDuration),
        label = "contentCardScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = Elevation.level1,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = SoftUIColors.SoftShadow,
                spotColor = SoftUIColors.SoftShadow
            )
            .clip(RoundedCornerShape(cornerRadius))
            .background(SoftUIColors.CardBackground)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(contentPadding),
        content = content
    )
}
