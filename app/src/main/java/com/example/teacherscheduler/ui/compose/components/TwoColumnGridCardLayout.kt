package com.example.teacherscheduler.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teacherscheduler.ui.theme.*

/**
 * TwoColumnGridCardLayout - Premium grid layout with soft UI cards
 *
 * Features:
 * - LazyVerticalGrid with 2 columns
 * - Equal spacing between cards
 * - Large rounded white cards (24dp corners)
 * - Icon at top in circular soft background
 * - Title and subtitle
 * - Small rounded chips
 * - Bottom progress indicator with soft colored background
 * - Elevated but not heavy feel
 * - Soft shadow (2-3dp)
 * - Light and airy appearance
 */

/**
 * Data class for grid card items
 */
data class GridCardItem(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val chips: List<String> = emptyList(),
    val progress: Float? = null, // 0.0 to 1.0
    val progressLabel: String? = null,
    val iconBackgroundColor: Color = SoftUIColors.ChipBackground,
    val iconTint: Color = SoftUIColors.AccentLavender,
    val progressColor: Color = SoftUIColors.AccentLavender
)

/**
 * Main two-column grid card layout
 */
@Composable
fun TwoColumnGridCardLayout(
    items: List<GridCardItem>,
    onItemClick: (GridCardItem) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items, key = { it.id }) { item ->
            GridCard(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

/**
 * Individual grid card component
 */
@Composable
private fun GridCard(
    item: GridCardItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SoftCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.85f), // Slightly taller than wide
        cornerRadius = 24.dp,
        elevation = 2.dp,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section: Icon + Content
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon in circular soft background
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(item.iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Title
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Subtitle
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp
                    ),
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Chips (if any)
                if (item.chips.isNotEmpty()) {
                    ChipRow(chips = item.chips)
                }
            }

            // Bottom section: Progress indicator (if present)
            if (item.progress != null) {
                ProgressIndicator(
                    progress = item.progress,
                    label = item.progressLabel,
                    color = item.progressColor
                )
            }
        }
    }
}

/**
 * Chip row component
 */
@Composable
private fun ChipRow(
    chips: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        chips.take(2).forEach { chipText -> // Show max 2 chips
            SoftChip(
                text = chipText,
                selected = false,
                onClick = null,
                modifier = Modifier.height(24.dp)
            )
        }

        // Show "+X more" if there are more than 2 chips
        if (chips.size > 2) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SoftUIColors.ChipBackground
            ) {
                Text(
                    text = "+${chips.size - 2}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp
                    ),
                    color = TextTertiary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Progress indicator with soft colored background and smooth animation
 */
@Composable
private fun ProgressIndicator(
    progress: Float,
    label: String?,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Smooth progress animation
    val animatedProgress by rememberSoftProgressAnimation(progress)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Progress label and percentage
        if (label != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondary
                )

                // Animate percentage text
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = color
                )
            }
        }

        // Progress bar with soft colored background
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
                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

// ============================================================================
// HELPER FUNCTIONS
// ============================================================================

/**
 * Create sample grid items for testing/preview
 */
fun createSampleGridItems(): List<GridCardItem> {
    return listOf(
        GridCardItem(
            id = "1",
            icon = Icons.Outlined.School,
            title = "Mathematics",
            subtitle = "Room 301 • Mon, Wed, Fri",
            chips = listOf("Grade 10", "Advanced"),
            progress = 0.75f,
            progressLabel = "Semester Progress",
            iconBackgroundColor = SoftUIColors.LavenderGradientStart,
            iconTint = SoftUIColors.AccentLavender,
            progressColor = SoftUIColors.AccentLavender
        ),
        GridCardItem(
            id = "2",
            icon = Icons.Outlined.Science,
            title = "Physics Lab",
            subtitle = "Lab 202 • Tue, Thu",
            chips = listOf("Grade 11", "Lab"),
            progress = 0.45f,
            progressLabel = "Completion",
            iconBackgroundColor = SoftUIColors.BlueGradientStart,
            iconTint = SoftUIColors.AccentBlue,
            progressColor = SoftUIColors.AccentBlue
        ),
        GridCardItem(
            id = "3",
            icon = Icons.Outlined.Book,
            title = "English Literature",
            subtitle = "Room 105 • Daily",
            chips = listOf("Grade 9", "Core"),
            progress = 0.90f,
            progressLabel = "Progress",
            iconBackgroundColor = SoftUIColors.MintGradientStart,
            iconTint = SoftUIColors.AccentMint,
            progressColor = SoftUIColors.AccentMint
        ),
        GridCardItem(
            id = "4",
            icon = Icons.Outlined.Calculate,
            title = "Calculus",
            subtitle = "Room 402 • Mon, Wed, Fri",
            chips = listOf("Grade 12", "AP"),
            progress = 0.60f,
            progressLabel = "Course Progress",
            iconBackgroundColor = SoftUIColors.PeachGradientStart,
            iconTint = SoftUIColors.AccentPeach,
            progressColor = SoftUIColors.AccentPeach
        )
    )
}

// ============================================================================
// VARIANT: Without Progress
// ============================================================================

/**
 * Simple grid card without progress indicator
 */
@Composable
fun SimpleGridCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    chips: List<String> = emptyList(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconBackgroundColor: Color = SoftUIColors.ChipBackground,
    iconTint: Color = SoftUIColors.AccentLavender
) {
    GridCard(
        item = GridCardItem(
            id = title, // Use title as ID for simple cases
            icon = icon,
            title = title,
            subtitle = subtitle,
            chips = chips,
            progress = null,
            iconBackgroundColor = iconBackgroundColor,
            iconTint = iconTint
        ),
        onClick = onClick,
        modifier = modifier
    )
}

// ============================================================================
// VARIANT: Compact Grid (3 columns)
// ============================================================================

/**
 * Compact three-column grid for smaller items
 */
@Composable
fun ThreeColumnGridCardLayout(
    items: List<GridCardItem>,
    onItemClick: (GridCardItem) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { item ->
            GridCard(
                item = item,
                onClick = { onItemClick(item) }
            )
        }
    }
}

