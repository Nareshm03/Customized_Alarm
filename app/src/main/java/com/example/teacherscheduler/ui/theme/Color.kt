package com.example.teacherscheduler.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color palette for the Teacher Scheduler app.
 * Apple-inspired clean UI with clear contrast and proper hierarchy.
 *
 * Design philosophy:
 * - Light neutral background (not warm/pink - truly neutral)
 * - Strong accent color for primary actions (blue)
 * - High contrast text on backgrounds
 * - Distinct elevation levels using white surfaces on light grey bg
 */

// Background colors - Neutral cool tones (not warm sand)
val BackgroundPrimary = Color(0xFFF2F2F7)   // iOS system grey 6 - neutral light grey
val BackgroundSecondary = Color(0xFFFFFFFF) // Pure white
val BackgroundTertiary = Color(0xFFE5E5EA)  // iOS system grey 5

// Surface colors - White cards on grey bg = clear elevation
val SurfaceLight = Color(0xFFFFFFFF)        // Pure white cards
val SurfaceVariant = Color(0xFFF2F2F7)      // Matches background
val SurfaceElevated = Color(0xFFFFFFFF)     // Elevated surfaces

// Primary colors - Strong blue accent (Apple-like)
val PrimaryLight = Color(0xFFD6E4FF)
val Primary = Color(0xFF007AFF)             // iOS blue
val PrimaryDark = Color(0xFF0056B3)
val PrimaryContainer = Color(0xFFE8F0FE)
val OnPrimaryContainer = Color(0xFF1A1A1A)

// Secondary colors - Soft teal/green
val SecondaryLight = Color(0xFFD4F5E9)
val Secondary = Color(0xFF34C759)           // iOS green
val SecondaryDark = Color(0xFF248A3D)
val SecondaryContainer = Color(0xFFE8F8EE)
val OnSecondaryContainer = Color(0xFF1A1A1A)

// Tertiary colors - Warm orange accent
val TertiaryLight = Color(0xFFFFE0CC)
val Tertiary = Color(0xFFFF9500)            // iOS orange
val TertiaryDark = Color(0xFFCC7700)
val TertiaryContainer = Color(0xFFFFF3E0)
val OnTertiaryContainer = Color(0xFF5C3A10)

// Text colors - High contrast
val TextPrimary = Color(0xFF1C1C1E)         // iOS label color - near black
val TextSecondary = Color(0xFF8E8E93)       // iOS secondary label (per design spec)
val TextTertiary = Color(0xFF8E8E93)        // iOS tertiary label
val TextOnPrimary = Color(0xFFFFFFFF)
val TextOnSurface = Color(0xFF1C1C1E)

// Status colors
val SuccessLight = Color(0xFFD4EDDA)
val Success = Color(0xFF34C759)             // iOS green
val SuccessDark = Color(0xFF248A3D)

val WarningLight = Color(0xFFFFF3CD)
val Warning = Color(0xFFFF9500)             // iOS orange
val WarningDark = Color(0xFFCC7700)

val ErrorLight = Color(0xFFFDE8E8)
val Error = Color(0xFFFF3B30)               // iOS red
val ErrorDark = Color(0xFFC62828)

val InfoLight = Color(0xFFE3F2FD)
val Info = Color(0xFF5AC8FA)                // iOS teal
val InfoDark = Color(0xFF007AFF)

// Outline and divider colors
val OutlineLight = Color(0xFFD1D1D6)        // iOS separator
val Outline = Color(0xFFC7C7CC)             // iOS opaque separator
val OutlineDark = Color(0xFFAEAEB2)
val Divider = Color(0xFFE5E5EA)             // Subtle divider

// Shadow colors
val ShadowLight = Color(0x14000000)
val ShadowMedium = Color(0x1F000000)
val ShadowDark = Color(0x29000000)




// Legacy color constants for backward compatibility
object AppColors {
    // Primary colors
    const val PRIMARY = "#007AFF"
    const val PRIMARY_DARK = "#0056B3"
    const val PRIMARY_LIGHT = "#D6E4FF"
    const val ACCENT = "#007AFF"

    // Neutral colors
    const val BACKGROUND = "#F2F2F7"
    const val SURFACE = "#FFFFFF"
    const val TEXT_PRIMARY = "#1C1C1E"
    const val TEXT_SECONDARY = "#636366"
    const val DIVIDER = "#E5E5EA"

    // Status colors
    const val SUCCESS = "#34C759"
    const val WARNING = "#FF9500"
    const val ERROR = "#FF3B30"
    const val INFO = "#5AC8FA"
}