package com.example.teacherscheduler.ui.compose.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * PremiumCard - A high-quality rounded card with soft shadow and press animations.
 * Part of the Soft UI design system.
 */
@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    gradientColors: List<Color>? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "cardScale"
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 3.dp,
        animationSpec = spring(dampingRatio = 0.8f),
        label = "cardElevation"
    )
    
    val shape = RoundedCornerShape(24.dp)
    
    // Using hardcoded shadow colors to resolve the 'SoftUIColors' reference issue
    // and avoid circular dependencies between files in the same package.
    val cardShadowColor = Color(0x18000000)
    val softShadowColor = Color(0x14000000)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = cardShadowColor,
                spotColor = softShadowColor
            )
            .clip(shape)
            .border(1.dp, Color(0xFFECE6DF), shape)
            .then(
                if (gradientColors != null && gradientColors.size >= 2) {
                    Modifier.background(Brush.linearGradient(gradientColors))
                } else {
                    Modifier.background(Color.White)
                }
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
