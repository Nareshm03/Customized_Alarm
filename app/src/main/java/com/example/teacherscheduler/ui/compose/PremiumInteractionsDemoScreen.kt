package com.example.teacherscheduler.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*

/**
 * Premium Micro-Interactions Demo Screen
 *
 * Demonstrates all premium micro-interactions:
 * - Button press scale animations
 * - Card elevation changes on press
 * - Smooth screen transitions
 * - AnimatedVisibility for sections
 * - Soft progress animations
 * - Staggered list animations
 */
@Composable
fun PremiumInteractionsDemo() {
    var showSection1 by remember { mutableStateOf(true) }
    var showSection2 by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0.3f) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SoftLayoutDimens.screenPadding)
                .padding(top = SoftLayoutDimens.topSpacing),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            Text(
                text = "Premium Interactions",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp
                ),
                color = TextPrimary
            )

            Text(
                text = "Elegant micro-interactions that enhance user experience",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Section 1: Button Press Animations
            AnimatedSection(visible = showSection1, delayMillis = 0) {
                DemoSection(
                    title = "Button Press Animations",
                    onToggle = { showSection1 = !showSection1 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Primary Button
                        RoundedPrimaryButton(
                            text = "Primary Button",
                            onClick = { /* Action */ },
                            icon = Icons.Outlined.CheckCircle
                        )

                        // Secondary Button
                        Surface(
                            onClick = { /* Action */ },
                            shape = RoundedCornerShape(50),
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                SoftUIColors.AccentLavender
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = null,
                                    tint = SoftUIColors.AccentLavender,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Secondary Button",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = SoftUIColors.AccentLavender
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Card Press Animations
            SlideInSection(visible = showSection2, delayMillis = 100) {
                DemoSection(
                    title = "Card Press Animations",
                    onToggle = { showSection2 = !showSection2 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Interactive cards
                        repeat(3) { index ->
                            AnimatedListItem(index = index) {
                                InteractiveCard(
                                    icon = when(index) {
                                        0 -> Icons.Outlined.School
                                        1 -> Icons.Outlined.Event
                                        else -> Icons.AutoMirrored.Outlined.Assignment
                                    },
                                    title = when(index) {
                                        0 -> "Classes"
                                        1 -> "Meetings"
                                        else -> "Tasks"
                                    },
                                    subtitle = "Tap to feel the interaction",
                                    color = when(index) {
                                        0 -> SoftUIColors.AccentLavender
                                        1 -> SoftUIColors.AccentBlue
                                        else -> SoftUIColors.AccentMint
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Section 3: Progress Animations
            AnimatedSection(visible = true, delayMillis = 200) {
                DemoSection(
                    title = "Smooth Progress Animations",
                    onToggle = null
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Progress controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Progress: ${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { progress = (progress - 0.1f).coerceAtLeast(0f) }) {
                                    Text("-10%", color = SoftUIColors.AccentLavender)
                                }
                                TextButton(onClick = { progress = (progress + 0.1f).coerceAtMost(1f) }) {
                                    Text("+10%", color = SoftUIColors.AccentLavender)
                                }
                            }
                        }

                        // Animated progress bar
                        AnimatedProgressBar(
                            progress = progress,
                            color = SoftUIColors.AccentLavender
                        )

                        // Multiple progress examples
                        ProgressExample("Mathematics", 0.75f, SoftUIColors.AccentLavender)
                        ProgressExample("Physics", 0.45f, SoftUIColors.AccentBlue)
                        ProgressExample("Chemistry", 0.90f, SoftUIColors.AccentMint)
                    }
                }
            }

            // Section 4: Loading States
            AnimatedSection(visible = true, delayMillis = 300) {
                DemoSection(
                    title = "Loading Animations",
                    onToggle = null
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Shimmer effect
                        ShimmerCard()

                        // Pulsing badge
                        PulsingBadgeDemo()
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ============================================================================
// DEMO COMPONENTS
// ============================================================================

@Composable
private fun DemoSection(
    title: String,
    onToggle: (() -> Unit)?,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary
            )

            if (onToggle != null) {
                TextButton(onClick = onToggle) {
                    Text("Toggle", color = SoftUIColors.AccentLavender)
                }
            }
        }

        content()
    }
}

@Composable
private fun InteractiveCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    color: Color
) {
    SoftCard(
        onClick = { /* Interact */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AnimatedProgressBar(
    progress: Float,
    color: Color
) {
    val animatedProgress by rememberSoftProgressAnimation(progress)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun ProgressExample(
    label: String,
    progress: Float,
    color: Color
) {
    val animatedProgress by rememberSoftProgressAnimation(progress)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = color
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun ShimmerCard() {
    val shimmerAlpha by rememberShimmerAnimation()

    SoftCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        SoftUIColors.ChipBackground.copy(
                            alpha = 0.5f + (shimmerAlpha * 0.5f)
                        )
                    )
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            SoftUIColors.ChipBackground.copy(
                                alpha = 0.5f + (shimmerAlpha * 0.5f)
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            SoftUIColors.ChipBackground.copy(
                                alpha = 0.3f + (shimmerAlpha * 0.3f)
                            )
                        )
                )
            }
        }
    }
}

@Composable
private fun PulsingBadgeDemo() {
    val pulseScale by rememberPulseAnimation()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Pulsing Badge:",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SoftUIColors.AccentCoral.copy(alpha = 0.15f),
            modifier = Modifier.graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(SoftUIColors.AccentCoral)
                )
                Text(
                    "3 New",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = SoftUIColors.AccentCoral
                )
            }
        }
    }
}

