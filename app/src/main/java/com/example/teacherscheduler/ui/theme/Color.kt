package com.example.teacherscheduler.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color palette for the Teacher Scheduler app.
 * Apple-inspired clean UI with soft, pastel colors.
 */

// Background colors
val BackgroundPrimary = Color(0xFFF3E8E2)  // Warm sand
val BackgroundSecondary = Color(0xFFFFFFFF) // Pure white
val BackgroundTertiary = Color(0xFFF3E8E2)  // Warm sand

// Surface colors
val SurfaceLight = Color(0xFFFFFFFF)        // Pure white cards
val SurfaceVariant = Color(0xFFF3E8E2)      // Warm sand variant
val SurfaceElevated = Color(0xFFFFFFFF)     // Elevated surfaces

// Primary colors - Warm Sand
val PrimaryLight = Color(0xFFF0DDD1)
val Primary = Color(0xFFE8CFC1)
val PrimaryDark = Color(0xFFD4B8A8)
val PrimaryContainer = Color(0xFFF3E8E2)
val OnPrimaryContainer = Color(0xFF2B2B2B)

// Secondary colors - Pale Pink
val SecondaryLight = Color(0xFFF5D5D8)
val Secondary = Color(0xFFE8CFC1)
val SecondaryDark = Color(0xFFD8B4A0)
val SecondaryContainer = Color(0xFFF3E8E2)
val OnSecondaryContainer = Color(0xFF2B2B2B)

// Tertiary colors - Soft coral/peach tones
val TertiaryLight = Color(0xFFF5C4B8)       // Soft peach
val Tertiary = Color(0xFFF0A794)            // Medium coral
val TertiaryDark = Color(0xFFE88B70)        // Deeper coral
val TertiaryContainer = Color(0xFFFDF0EC)   // Very light peach container
val OnTertiaryContainer = Color(0xFF5C3A30) // Dark text on tertiary container

// Text colors
val TextPrimary = Color(0xFF2B2B2B)
val TextSecondary = Color(0xFF7A7A7A)
val TextTertiary = Color(0xFF7A7A7A)
val TextOnPrimary = Color(0xFFFFFFFF)
val TextOnSurface = Color(0xFF1C1C1E)

// Status colors - Softened versions
val SuccessLight = Color(0xFFD4EDDA)        // Light success background
val Success = Color(0xFF5CB85C)             // Soft green
val SuccessDark = Color(0xFF3D8B3D)         // Darker success

val WarningLight = Color(0xFFFFF3CD)        // Light warning background
val Warning = Color(0xFFF5C06D)             // Soft amber
val WarningDark = Color(0xFFD4A437)         // Darker warning

val ErrorLight = Color(0xFFFDE8E8)          // Light error background
val Error = Color(0xFFE57373)               // Soft red
val ErrorDark = Color(0xFFC62828)           // Darker error

val InfoLight = Color(0xFFE3F2FD)           // Light info background
val Info = Color(0xFF64B5F6)                // Soft blue
val InfoDark = Color(0xFF1976D2)            // Darker info

// Outline and divider colors
val OutlineLight = Color(0xFFECE6DF)        // Border soft
val Outline = Color(0xFFECE6DF)             // Border soft
val OutlineDark = Color(0xFFECE6DF)         // Border soft
val Divider = Color(0xFFECE6DF)             // Border soft

// Shadow colors
val ShadowLight = Color(0x14000000)
val ShadowMedium = Color(0x1F000000)
val ShadowDark = Color(0x29000000)

// Gradient colors - Warm Sand
val GradientLavenderStart = Color(0xFFF3E8E2)
val GradientLavenderEnd = Color(0xFFE8CFC1)



// Legacy color constants for backward compatibility
object AppColors {
    // Primary colors - Warm Sand
    const val PRIMARY = "#E8CFC1"
    const val PRIMARY_DARK = "#D4B8A8"
    const val PRIMARY_LIGHT = "#F0DDD1"
    const val ACCENT = "#E8CFC1"

    // Neutral colors
    const val BACKGROUND = "#F3E8E2"
    const val SURFACE = "#FFFFFF"
    const val TEXT_PRIMARY = "#1A1A2E"
    const val TEXT_SECONDARY = "#6B7280"
    const val DIVIDER = "#ECE6DF"

    // Status colors
    const val SUCCESS = "#5CB85C"
    const val WARNING = "#F5C06D"
    const val ERROR = "#E57373"
    const val INFO = "#64B5F6"
}