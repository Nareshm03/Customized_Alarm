package com.example.teacherscheduler.ui.compose.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Subtle Micro-Animations for Teacher Scheduler
 *
 * Design Principles:
 * - Elegant and subtle animations
 * - Not overused - only where meaningful
 * - Smooth transitions that enhance UX
 * - Performance-conscious implementations
 */

// ============================================================================
// ANIMATION CONSTANTS
// ============================================================================

object AnimationDurations {
    const val INSTANT = 100
    const val FAST = 200
    const val NORMAL = 300
    const val SLOW = 450
    const val SCREEN_TRANSITION = 400
}

object AnimationDelays {
    const val STAGGER_SHORT = 50
    const val STAGGER_NORMAL = 80
    const val STAGGER_LONG = 120
}

// ============================================================================
// CUSTOM EASING CURVES
// ============================================================================

val EaseOutQuart = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
val EaseOutExpo = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
val EaseOutBack = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

// ============================================================================
// SCREEN FADE-IN ANIMATION
// ============================================================================

/**
 * A container that applies a fade-in animation when the screen appears.
 * Use this as the root of screen content for elegant entrance.
 */
@Composable
fun ScreenFadeIn(
    modifier: Modifier = Modifier,
    durationMillis: Int = AnimationDurations.SCREEN_TRANSITION,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = EaseOutCubic
        ),
        label = "screenFadeIn"
    )

    Box(
        modifier = modifier.alpha(alpha)
    ) {
        content()
    }
}

/**
 * Screen transition with both fade and slight slide up
 */
@Composable
fun ScreenTransition(
    modifier: Modifier = Modifier,
    durationMillis: Int = AnimationDurations.SCREEN_TRANSITION,
    slideOffset: Float = 20f,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val transition = updateTransition(targetState = isVisible, label = "screenTransition")

    val alpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = durationMillis, easing = EaseOutCubic)
        },
        label = "alpha"
    ) { visible -> if (visible) 1f else 0f }

    val translationY by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = durationMillis, easing = EaseOutCubic)
        },
        label = "translationY"
    ) { visible -> if (visible) 0f else slideOffset }

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            this.translationY = translationY
        }
    ) {
        content()
    }
}

// ============================================================================
// CARD SLIDE-UP ANIMATION
// ============================================================================

/**
 * AnimatedVisibility wrapper for card slide-up effect
 */
@Composable
fun CardSlideUp(
    visible: Boolean,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    durationMillis: Int = AnimationDurations.NORMAL,
    slideDistance: Int = 40,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = EaseOutCubic
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = EaseOutCubic
            ),
            initialOffsetY = { slideDistance }
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = AnimationDurations.FAST,
                easing = EaseOutCubic
            )
        ) + slideOutVertically(
            animationSpec = tween(
                durationMillis = AnimationDurations.FAST,
                easing = EaseOutCubic
            ),
            targetOffsetY = { -slideDistance / 2 }
        ),
        content = content
    )
}

/**
 * Staggered card animations for lists
 */
@Composable
fun StaggeredCardSlideUp(
    visible: Boolean,
    index: Int,
    modifier: Modifier = Modifier,
    baseDelayMillis: Int = 0,
    staggerDelayMillis: Int = AnimationDelays.STAGGER_NORMAL,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    CardSlideUp(
        visible = visible,
        modifier = modifier,
        delayMillis = baseDelayMillis + (index * staggerDelayMillis),
        content = content
    )
}

// ============================================================================
// BUTTON PRESS SCALE ANIMATION
// ============================================================================

/**
 * A modifier that adds subtle scale animation on press (0.95f -> 1f)
 */
@Composable
fun Modifier.pressScale(
    enabled: Boolean = true,
    pressedScale: Float = 0.95f,
    onClick: () -> Unit
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pressScale"
    )

    return this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * Button with built-in press animation and optional soft ripple
 */
@Composable
fun AnimatedPressButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    pressedScale: Float = 0.95f,
    useSoftRipple: Boolean = true,
    rippleColor: Color = Color(0xFF007AFF).copy(alpha = 0.12f), // iOS blue
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "buttonPressScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = if (useSoftRipple) {
                    ripple(
                        bounded = true,
                        color = rippleColor
                    )
                } else null,
                enabled = enabled,
                onClick = onClick
            ),
        content = content
    )
}

// ============================================================================
// SMOOTH PROGRESS BAR ANIMATION
// ============================================================================

/**
 * Animated linear progress bar with smooth transitions
 */
@Composable
fun SmoothProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF007AFF), // iOS blue
    trackColor: Color = Color(0xFFE5E5EA), // iOS separator
    cornerRadius: Dp = 8.dp,
    height: Dp = 8.dp,
    animationDuration: Int = AnimationDurations.SLOW
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = EaseOutCubic
        ),
        label = "progressAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(cornerRadius))
                .background(color)
        )
    }
}

/**
 * Animated circular/indeterminate progress with pulsing effect
 */
@Composable
fun PulsingProgress(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF007AFF) // iOS blue
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsingProgress")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsingAlpha"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulsingScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .alpha(alpha),
        contentAlignment = Alignment.Center
    ) {
        LinearProgressIndicator(
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

// ============================================================================
// SOFT RIPPLE CLICKABLE
// ============================================================================

/**
 * A modifier that adds a soft, subtle ripple effect
 */
@Composable
fun Modifier.softRippleClickable(
    enabled: Boolean = true,
    rippleColor: Color = Color(0xFF007AFF).copy(alpha = 0.08f), // iOS blue
    bounded: Boolean = true,
    onClick: () -> Unit
): Modifier {
    return this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(
            bounded = bounded,
            color = rippleColor
        ),
        enabled = enabled,
        onClick = onClick
    )
}

// ============================================================================
// TRANSITION UTILITIES
// ============================================================================

/**
 * Animated content with crossfade for state changes
 */
@Composable
fun <T> SoftCrossfade(
    targetState: T,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = AnimationDurations.NORMAL,
        easing = EaseOutCubic
    ),
    content: @Composable (T) -> Unit
) {
    Crossfade(
        targetState = targetState,
        modifier = modifier,
        animationSpec = animationSpec,
        content = content,
        label = "softCrossfade"
    )
}

/**
 * Value animation wrapper for any animatable value
 */
@Composable
fun animatedValue(
    targetValue: Float,
    durationMillis: Int = AnimationDurations.NORMAL,
    delayMillis: Int = 0,
    easing: Easing = EaseOutCubic
): Float {
    val animatedValue by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(
            durationMillis = durationMillis,
            delayMillis = delayMillis,
            easing = easing
        ),
        label = "animatedValue"
    )
    return animatedValue
}

// ============================================================================
// ANIMATED VISIBILITY PRESETS
// ============================================================================

object AnimatedVisibilityPresets {

    /**
     * Fade in/out - Simple and elegant
     */
    val fade = EnterExitPair(
        enter = fadeIn(tween(AnimationDurations.NORMAL, easing = EaseOutCubic)),
        exit = fadeOut(tween(AnimationDurations.FAST, easing = EaseOutCubic))
    )

    /**
     * Slide up with fade - For cards and content
     */
    val slideUp = EnterExitPair(
        enter = fadeIn(tween(AnimationDurations.NORMAL, easing = EaseOutCubic)) +
                slideInVertically(tween(AnimationDurations.NORMAL, easing = EaseOutCubic)) { 40 },
        exit = fadeOut(tween(AnimationDurations.FAST, easing = EaseOutCubic)) +
                slideOutVertically(tween(AnimationDurations.FAST, easing = EaseOutCubic)) { -20 }
    )

    /**
     * Scale in - For FABs and buttons
     */
    val scaleIn = EnterExitPair(
        enter = fadeIn(tween(AnimationDurations.FAST, easing = EaseOutCubic)) +
                scaleIn(tween(AnimationDurations.NORMAL, easing = EaseOutBack), initialScale = 0.8f),
        exit = fadeOut(tween(AnimationDurations.FAST, easing = EaseOutCubic)) +
                scaleOut(tween(AnimationDurations.FAST, easing = EaseOutCubic), targetScale = 0.8f)
    )

    /**
     * Expand vertically - For collapsible content
     */
    val expandVertically = EnterExitPair(
        enter = fadeIn(tween(AnimationDurations.NORMAL, easing = EaseOutCubic)) +
                expandVertically(tween(AnimationDurations.NORMAL, easing = EaseOutCubic)),
        exit = fadeOut(tween(AnimationDurations.FAST, easing = EaseOutCubic)) +
                shrinkVertically(tween(AnimationDurations.FAST, easing = EaseOutCubic))
    )
}

data class EnterExitPair(
    val enter: EnterTransition,
    val exit: ExitTransition
)

// ============================================================================
// LOADING STATE ANIMATION
// ============================================================================

/**
 * Shimmer loading effect for skeleton screens
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFFF2F2F7), // System grey background
    highlightColor: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")

    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerProgress"
    )

    Box(
        modifier = modifier
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        baseColor,
                        highlightColor.copy(alpha = 0.4f),
                        baseColor
                    ),
                    start = androidx.compose.ui.geometry.Offset(
                        x = shimmerProgress * 1000f - 500f,
                        y = 0f
                    ),
                    end = androidx.compose.ui.geometry.Offset(
                        x = shimmerProgress * 1000f,
                        y = 0f
                    )
                )
            )
    )
}

// ============================================================================
// ANIMATED COUNTER
// ============================================================================

/**
 * Returns an animated integer value with smooth transitions
 */
@Composable
fun animatedCounterValue(
    targetValue: Int,
    durationMillis: Int = AnimationDurations.SLOW
): Int {
    val animatedValue by animateIntAsState(
        targetValue = targetValue,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = EaseOutCubic
        ),
        label = "counterAnimation"
    )

    return animatedValue
}

// ============================================================================
// HOVER/FOCUS SCALE EFFECT
// ============================================================================

/**
 * Subtle scale effect on hover/focus for interactive elements
 */
@Composable
fun Modifier.hoverScale(
    isHovered: Boolean,
    scale: Float = 1.02f
): Modifier {
    val animatedScale by animateFloatAsState(
        targetValue = if (isHovered) scale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "hoverScale"
    )

    return this.scale(animatedScale)
}








