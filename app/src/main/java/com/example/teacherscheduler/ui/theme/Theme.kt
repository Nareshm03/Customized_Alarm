package com.example.teacherscheduler.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

/**
 * Teacher Scheduler Theme
 *
 * Design Philosophy:
 * - Apple-inspired clean UI
 * - Soft light backgrounds (off-white / light neutral)
 * - Large rounded cards (24dp radius)
 * - Subtle pastel colors
 * - Soft shadows (low elevation)
 * - Minimal heavy outlines
 */

private val PremiumLightColorScheme = lightColorScheme(
    background = Color(0xFFF2F2F7),         // iOS system grey 6
    surface = Color.White,
    primary = Color(0xFF007AFF),             // iOS blue
    onPrimary = Color.White,
    onBackground = Color(0xFF1C1C1E),
    onSurface = Color(0xFF1C1C1E),
    primaryContainer = Color(0xFFE8F0FE),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondaryContainer = Color(0xFFE8F8EE),
    onSecondaryContainer = Color(0xFF1A1A1A),
    outline = Color(0xFFC7C7CC),
    outlineVariant = Color(0xFFD1D1D6),
    surfaceVariant = Color(0xFFF2F2F7),
    error = Color(0xFFFF3B30)
)

// Custom shapes for the app - Large rounded corners
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),      // Cards
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun TeacherSchedulerTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color(0xFFF2F2F7).toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = PremiumLightColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}

/**
 * Dimension constants for consistent spacing
 */
object AppDimens {
    // Outer padding (screen edges)
    val screenPadding = 24.dp

    // Internal padding (inside cards/containers)
    val cardPadding = 16.dp
    val cardPaddingLarge = 20.dp

    // Spacing between elements
    val spacingXSmall = 4.dp
    val spacingSmall = 8.dp
    val spacingMedium = 12.dp
    val spacingLarge = 16.dp
    val spacingXLarge = 24.dp
    val spacingXXLarge = 32.dp

    // Corner radius
    val cornerRadiusSmall = 12.dp
    val cornerRadiusMedium = 16.dp
    val cornerRadiusLarge = 24.dp    // Cards
    val cornerRadiusButton = 20.dp   // Buttons
    val cornerRadiusXLarge = 28.dp

    // Elevation (soft shadows)
    val elevationNone = 0.dp
    val elevationXSmall = 1.dp
    val elevationSmall = 2.dp
    val elevationMedium = 4.dp
    val elevationLarge = 8.dp

    // Icon sizes
    val iconSizeSmall = 20.dp
    val iconSizeMedium = 24.dp
    val iconSizeLarge = 32.dp
    val iconSizeXLarge = 48.dp

    // Button heights
    val buttonHeight = 56.dp
    val buttonHeightSmall = 40.dp

    // Card minimum height
    val cardMinHeight = 80.dp
}

/**
 * Animation durations for smooth micro-animations
 */
object AppAnimations {
    const val durationFast = 150
    const val durationMedium = 300
    const val durationSlow = 500
    const val durationVerySlow = 800
}
