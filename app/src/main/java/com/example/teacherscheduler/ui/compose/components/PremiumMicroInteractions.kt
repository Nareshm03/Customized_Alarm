package com.example.teacherscheduler.ui.compose.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Premium Micro-Interactions System
 *
 * Subtle, elegant animations that enhance user experience without being overwhelming.
 *
 * Principles:
 * - Smooth and natural (spring physics)
 * - Quick response (150-300ms)
 * - Subtle effects (small scale changes)
 * - Purpose-driven (feedback, not decoration)
 */

// ============================================================================
// INTERACTION CONSTANTS
// ============================================================================

object PremiumInteractions {
    // Scale amounts
    const val BUTTON_PRESS_SCALE = 0.96f
    const val CARD_PRESS_SCALE = 0.98f
    const val FAB_PRESS_SCALE = 0.92f
    const val CHIP_PRESS_SCALE = 0.95f

    // Elevation changes
    val BUTTON_PRESS_ELEVATION_DECREASE = 2.dp
    val CARD_PRESS_ELEVATION_INCREASE = 2.dp

    // Animation durations
    const val QUICK_ANIMATION = 150
    const val NORMAL_ANIMATION = 250
    const val SLOW_ANIMATION = 350

    // Spring configurations
    val BOUNCY_SPRING = SpringSpec<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val RESPONSIVE_SPRING = SpringSpec<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val GENTLE_SPRING = SpringSpec<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
}

// ============================================================================
// BUTTON PRESS ANIMATION
// ============================================================================

/**
 * Premium button press animation
 * - Scales down slightly on press
 * - Uses spring physics for natural feel
 * - Quick and responsive
 */
@Composable
fun Modifier.premiumButtonPress(
    enabled: Boolean = true,
    scaleAmount: Float = PremiumInteractions.BUTTON_PRESS_SCALE
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) scaleAmount else 1f,
        animationSpec = PremiumInteractions.RESPONSIVE_SPRING,
        label = "buttonPressScale"
    )

    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = null
        ) { }
}

/**
 * Card press animation with elevation change
 * - Slight scale down
 * - Elevation increases for feedback
 */
@Composable
fun Modifier.premiumCardPress(
    enabled: Boolean = true
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (enabled && isPressed) PremiumInteractions.CARD_PRESS_SCALE else 1f,
        animationSpec = PremiumInteractions.GENTLE_SPRING,
        label = "cardPressScale"
    )

    val translationY by animateFloatAsState(
        targetValue = if (enabled && isPressed) -1f else 0f,
        animationSpec = PremiumInteractions.GENTLE_SPRING,
        label = "cardPressTranslation"
    )

    this
        .scale(scale)
        .graphicsLayer {
            this.translationY = translationY
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null
        ) { }
}

// ============================================================================
// ELEVATION ANIMATIONS
// ============================================================================

/**
 * Animated elevation for interactive elements
 */
@Composable
fun animatedElevation(
    isPressed: Boolean,
    defaultElevation: Dp,
    pressedElevation: Dp
): State<Dp> {
    return animateDpAsState(
        targetValue = if (isPressed) pressedElevation else defaultElevation,
        animationSpec = tween(
            durationMillis = PremiumInteractions.QUICK_ANIMATION,
            easing = FastOutSlowInEasing
        ),
        label = "elevation"
    )
}

// ============================================================================
// PROGRESS ANIMATIONS
// ============================================================================

/**
 * Smooth progress animation
 */
@Composable
fun animateProgressSmoothly(
    targetProgress: Float,
    durationMillis: Int = 800
): State<Float> {
    return animateFloatAsState(
        targetValue = targetProgress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "progress"
    )
}

/**
 * Soft progress indicator with smooth animation
 */
@Composable
fun rememberSoftProgressAnimation(
    targetProgress: Float
): State<Float> {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(targetProgress) {
        animatedProgress.animateTo(
            targetValue = targetProgress.coerceIn(0f, 1f),
            animationSpec = tween(
                durationMillis = 600,
                easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
            )
        )
    }

    return animatedProgress.asState()
}

// ============================================================================
// SECTION ANIMATIONS
// ============================================================================

/**
 * Fade in section with delay
 */
@Composable
fun AnimatedSection(
    visible: Boolean = true,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = PremiumInteractions.NORMAL_ANIMATION,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            )
        ) + expandVertically(
            animationSpec = tween(
                durationMillis = PremiumInteractions.NORMAL_ANIMATION,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            ),
            expandFrom = Alignment.Top
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = PremiumInteractions.QUICK_ANIMATION,
                easing = FastOutSlowInEasing
            )
        ) + shrinkVertically(
            animationSpec = tween(
                durationMillis = PremiumInteractions.QUICK_ANIMATION,
                easing = FastOutSlowInEasing
            ),
            shrinkTowards = Alignment.Top
        )
    ) {
        content()
    }
}

/**
 * Slide and fade in section
 */
@Composable
fun SlideInSection(
    visible: Boolean = true,
    delayMillis: Int = 0,
    slideDistance: Int = 30,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = PremiumInteractions.NORMAL_ANIMATION,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = PremiumInteractions.NORMAL_ANIMATION,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            ),
            initialOffsetY = { slideDistance }
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = PremiumInteractions.QUICK_ANIMATION,
                easing = FastOutSlowInEasing
            )
        ) + slideOutVertically(
            animationSpec = tween(
                durationMillis = PremiumInteractions.QUICK_ANIMATION,
                easing = FastOutSlowInEasing
            ),
            targetOffsetY = { -slideDistance }
        )
    ) {
        content()
    }
}

// ============================================================================
// SCREEN TRANSITIONS
// ============================================================================

/**
 * Smooth screen fade transition
 */
@Composable
fun ScreenTransition(
    visible: Boolean = true,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = PremiumInteractions.SLOW_ANIMATION,
                easing = FastOutSlowInEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = PremiumInteractions.NORMAL_ANIMATION,
                easing = FastOutSlowInEasing
            )
        )
    ) {
        content()
    }
}

/**
 * Cross-fade between two states
 */
@Composable
fun <T> SmoothCrossfade(
    targetState: T,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = PremiumInteractions.NORMAL_ANIMATION,
        easing = FastOutSlowInEasing
    ),
    content: @Composable (T) -> Unit
) {
    Crossfade(
        targetState = targetState,
        modifier = modifier,
        animationSpec = animationSpec,
        label = "smoothCrossfade",
        content = content
    )
}

// ============================================================================
// LIST ITEM ANIMATIONS
// ============================================================================

/**
 * Staggered list item animation
 */
@Composable
fun AnimatedListItem(
    index: Int,
    staggerDelay: Int = 50,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = PremiumInteractions.NORMAL_ANIMATION,
                delayMillis = index * staggerDelay,
                easing = FastOutSlowInEasing
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = PremiumInteractions.NORMAL_ANIMATION,
                delayMillis = index * staggerDelay,
                easing = FastOutSlowInEasing
            ),
            initialOffsetY = { 20 }
        )
    ) {
        content()
    }
}

// ============================================================================
// SHIMMER EFFECT (Loading State)
// ============================================================================

/**
 * Subtle shimmer animation for loading states
 */
@Composable
fun rememberShimmerAnimation(): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    return infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )
}

// ============================================================================
// PULSING ANIMATION
// ============================================================================

/**
 * Subtle pulsing animation for badges or notifications
 */
@Composable
fun rememberPulseAnimation(): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    return infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
}

// ============================================================================
// ROTATION ANIMATION
// ============================================================================

/**
 * Smooth rotation animation
 */
@Composable
fun rememberRotationAnimation(
    durationMillis: Int = 1000
): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    return infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
}

