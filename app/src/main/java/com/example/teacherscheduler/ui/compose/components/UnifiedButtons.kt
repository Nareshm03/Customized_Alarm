package com.example.teacherscheduler.ui.compose.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.teacherscheduler.ui.theme.*

/**
 * Unified Button System
 * - Same radius (22dp)
 * - Same elevation (3dp)
 * - Same press animation
 * - Consistent color style
 */

private val ButtonRadius = 22.dp
private val ButtonElevation = 3.dp
private val ButtonPressScale = 0.96f

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) ButtonPressScale else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "buttonScale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed && enabled) 1.dp else ButtonElevation,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "buttonElevation"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (enabled) elevation else 0.dp,
                shape = RoundedCornerShape(ButtonRadius),
                ambientColor = Color(0x18000000),
                spotColor = Color(0x14000000)
            )
            .clip(RoundedCornerShape(ButtonRadius))
            .background(
                if (enabled) {
                    Brush.linearGradient(
                        listOf(Color(0xFFE8CFC1), Color(0xFFD8B4A0))
                    )
                } else {
                    Brush.linearGradient(listOf(Color(0xFFECE6DF), Color(0xFFECE6DF)))
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (enabled) Color(0xFF2B2B2B) else Color(0xFF9E9E9E)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = if (enabled) Color(0xFF2B2B2B) else Color(0xFF9E9E9E)
            )
        }
    }
}

@Composable
fun SecondaryTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "textButtonScale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(ButtonRadius))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Medium
            ),
            color = if (enabled) Color(0xFFD8B4A0) else Color(0xFF9E9E9E)
        )
    }
}

@Composable
fun IconCircleButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 44.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "iconButtonScale"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(
                elevation = ButtonElevation,
                shape = CircleShape,
                ambientColor = Color(0x18000000),
                spotColor = Color(0x14000000)
            )
            .clip(CircleShape)
            .background(Color(0xFFF7F4EF))
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
            modifier = Modifier.size(22.dp),
            tint = Color(0xFF2B2B2B)
        )
    }
}
