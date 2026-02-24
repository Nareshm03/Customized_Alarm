package com.example.teacherscheduler.ui.compose.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teacherscheduler.ui.theme.*

/**
 * Soft Bottom Navigation - Modern, clean navigation bar
 *
 * Design:
 * - White background
 * - Minimal shadow (2dp)
 * - Selected item has soft lavender pill background
 * - Icons simple and centered
 * - Small label under icon
 * - No purple header bars or dark gradients
 * - Clean and airy feel
 */

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun SoftBottomNavigation(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ambientColor = Color(0x18000000),
                spotColor = Color(0x14000000)
            ),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(84.dp)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                SoftBottomNavItem(
                    item = item,
                    selected = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SoftBottomNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth scale animation on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "navItemScale"
    )

    // Animate icon color
    val iconColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF2C2C2E) else Color(0xFF8E8E93),
        animationSpec = tween(durationMillis = 200),
        label = "iconColor"
    )

    // Animate label color
    val labelColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF2C2C2E) else Color(0xFF8E8E93),
        animationSpec = tween(durationMillis = 200),
        label = "labelColor"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .selectable(
                selected = selected,
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null // No ripple for clean feel
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Soft sand pill background for selected item
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(300)) + scaleIn(
                animationSpec = tween(300),
                initialScale = 0.85f
            ),
            exit = fadeOut(tween(200)) + scaleOut(
                animationSpec = tween(200),
                targetScale = 0.85f
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xFFF4E4E6))
            )
        }

        // Content (icon + label)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(vertical = 6.dp)
                .heightIn(min = 52.dp)
        ) {
            // Icon
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Label - always visible
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 12.sp,
                    letterSpacing = 0.1.sp
                ),
                color = labelColor,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Alternative: Compact Bottom Navigation (for smaller screens or more items)
 */
@Composable
fun CompactSoftBottomNavigation(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                ambientColor = Color(0x18000000),
                spotColor = Color(0x14000000)
            ),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, item ->
                CompactNavItem(
                    item = item,
                    selected = selectedIndex == index,
                    onClick = { onItemSelected(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CompactNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "compactNavScale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF007AFF) else Color(0xFF8E8E93),
        animationSpec = tween(durationMillis = 200),
        label = "compactIconColor"
    )

    val labelColor by animateColorAsState(
        targetValue = if (selected) Color(0xFF007AFF) else Color(0xFF8E8E93),
        animationSpec = tween(durationMillis = 200),
        label = "compactLabelColor"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .selectable(
                selected = selected,
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pill background for selected
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.85f),
            exit = fadeOut(tween(200)) + scaleOut(
                animationSpec = tween(200),
                targetScale = 0.85f
            )
        ) {
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .widthIn(min = 80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF4E4E6))
            )
        }

        // Icon only when not selected, icon + label when selected
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )

            // Show label only when selected
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(tween(300)) + expandHorizontally(tween(300)),
                exit = fadeOut(tween(200)) + shrinkHorizontally(tween(200))
            ) {
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        color = labelColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
