package com.example.teacherscheduler.ui.compose.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.teacherscheduler.ui.theme.*

/**
 * Base UI Components for Teacher Scheduler
 *
 * NOTE: For the new Apple-inspired soft UI design, prefer using components from
 * [SoftUIComponents.kt] which have enhanced animations and styling:
 * - SoftCard (enhanced with press animations)
 * - GradientHighlightCard
 * - RoundedPrimaryButton (pill-shaped with gradient)
 * - SoftChip (enhanced with color animations)
 * - SoftFloatingActionButton
 * - SoftProfileAvatar
 * - SoftGridCard, SoftListCard
 *
 * Components in this file are kept for backward compatibility.
 *
 * Design Principles:
 * - Large rounded cards (24dp radius)
 * - Subtle pastel gradients
 * - Soft shadows (low elevation)
 * - Smooth micro-animations
 * - Apple-inspired clean UI
 */

// ============================================================================
// CARD COMPONENTS
// ============================================================================

/**
 * Soft Card - Main card component with rounded corners and subtle shadow
 */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(AppDimens.cardPadding),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(AppDimens.cornerRadiusLarge)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier
                .shadow(
                    elevation = AppDimens.elevationSmall,
                    shape = shape,
                    ambientColor = ShadowMedium,
                    spotColor = ShadowMedium
                ),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp,
                pressedElevation = AppDimens.elevationXSmall
            )
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier
                .shadow(
                    elevation = AppDimens.elevationSmall,
                    shape = shape,
                    ambientColor = ShadowMedium,
                    spotColor = ShadowMedium
                ),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}

/**
 * Gradient Card - Card with subtle pastel gradient background
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    gradientColors: List<Color> = listOf(PrimaryContainer, PrimaryContainer),
    contentPadding: PaddingValues = PaddingValues(AppDimens.cardPadding),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(AppDimens.cornerRadiusLarge)

    Box(
        modifier = modifier
            .shadow(
                elevation = AppDimens.elevationSmall,
                shape = shape,
                ambientColor = ShadowMedium,
                spotColor = ShadowMedium
            )
            .clip(shape)
            .background(color = gradientColors.first())
            .then(
                if (onClick != null) {
                    Modifier
                } else {
                    Modifier
                }
            )
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Stat Card - For displaying statistics with icon
 */
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector? = null,
    gradientColors: List<Color> = listOf(PrimaryContainer, PrimaryContainer),
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(AppDimens.cornerRadiusLarge)

    Card(
        modifier = modifier
            .shadow(
                elevation = AppDimens.elevationSmall,
                shape = shape,
                ambientColor = ShadowMedium,
                spotColor = ShadowMedium
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = onClick ?: {}
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientColors.firstOrNull() ?: PrimaryContainer)
                .padding(AppDimens.cardPaddingLarge)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(AppDimens.iconSizeLarge),
                        tint = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(AppDimens.spacingSmall))
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(AppDimens.spacingXSmall))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

// ============================================================================
// BUTTON COMPONENTS
// ============================================================================

/**
 * Soft Button - Primary button with rounded corners
 */
@Composable
fun SoftButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> OutlineLight
            isPressed -> Color(0xFF0056B3)
            else -> Color(0xFF007AFF)
        },
        animationSpec = tween(SoftAnimations.pressDuration),
        label = "buttonBg"
    )

    val shape = RoundedCornerShape(AppRadius.button)

    Box(
        modifier = modifier
            .height(AppDimens.buttonHeight)
            .shadow(
                elevation = if (enabled) Elevation.level3 else Elevation.level0,
                shape = shape,
                ambientColor = SoftUIColors.SoftShadow,
                spotColor = SoftUIColors.SoftShadow
            )
            .clip(shape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = AppSpacing.screenHorizontal, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(AppDimens.iconSizeMedium),
                    tint = if (enabled) Color.White else TextTertiary
                )
                Spacer(modifier = Modifier.width(AppDimens.spacingSmall))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) Color.White else TextTertiary
            )
        }
    }
}

/**
 * Soft Outlined Button - Secondary button with outline
 */
@Composable
fun SoftOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(AppDimens.buttonHeight),
        enabled = enabled,
        shape = RoundedCornerShape(AppDimens.cornerRadiusButton),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Primary,
            disabledContentColor = TextTertiary
        ),
        border = ButtonDefaults.outlinedButtonBorder(enabled).copy(
            width = 1.dp,
            brush = androidx.compose.ui.graphics.SolidColor(OutlineLight)
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppDimens.iconSizeMedium)
            )
            Spacer(modifier = Modifier.width(AppDimens.spacingSmall))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Soft Text Button - Minimal button
 */
@Composable
fun SoftTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    icon: ImageVector? = null
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = Primary,
            disabledContentColor = TextTertiary
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppDimens.iconSizeSmall)
            )
            Spacer(modifier = Modifier.width(AppDimens.spacingXSmall))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Soft FAB - Floating Action Button with soft design
 */
@Composable
fun SoftFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String? = null,
    containerColor: Color = Primary,
    contentColor: Color = TextOnPrimary
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(AppDimens.cornerRadiusButton),
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = AppDimens.elevationSmall,
            pressedElevation = AppDimens.elevationMedium
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(AppDimens.iconSizeMedium)
        )
    }
}

// ============================================================================
// INPUT COMPONENTS
// ============================================================================

/**
 * Soft TextField - Text input with soft styling
 */
@Composable
fun SoftTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    isError: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it, color = TextTertiary) } },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = if (isError) Error else TextSecondary
                )
            }
        },
        trailingIcon = trailingIcon,
        enabled = enabled,
        singleLine = singleLine,
        maxLines = maxLines,
        isError = isError,
        shape = RoundedCornerShape(AppDimens.cornerRadiusMedium),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = OutlineLight,
            disabledBorderColor = OutlineLight,
            errorBorderColor = Error,
            focusedContainerColor = SurfaceLight,
            unfocusedContainerColor = SurfaceVariant,
            disabledContainerColor = BackgroundTertiary,
            cursorColor = Primary
        )
    )
}

// ============================================================================
// TOP BAR COMPONENTS
// ============================================================================

/**
 * Soft Top App Bar - Clean top bar with soft background
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoftTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BackgroundPrimary,
            scrolledContainerColor = BackgroundPrimary,
            navigationIconContentColor = TextPrimary,
            titleContentColor = TextPrimary,
            actionIconContentColor = TextSecondary
        )
    )
}

/**
 * Soft Large Top App Bar - For screens with prominent headers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoftLargeTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    LargeTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary
            )
        },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.largeTopAppBarColors(
            containerColor = BackgroundPrimary,
            scrolledContainerColor = BackgroundPrimary,
            navigationIconContentColor = TextPrimary,
            titleContentColor = TextPrimary,
            actionIconContentColor = TextSecondary
        )
    )
}

// ============================================================================
// LIST ITEM COMPONENTS
// ============================================================================

/**
 * Soft List Item - Clean list item with proper spacing
 */
@Composable
fun SoftListItem(
    headlineText: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val containerColor by animateColorAsState(
        targetValue = SurfaceLight,
        animationSpec = tween(durationMillis = AppAnimations.durationFast),
        label = "containerColor"
    )

    SoftCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        containerColor = containerColor,
        contentPadding = PaddingValues(AppDimens.cardPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(AppDimens.iconSizeMedium),
                    tint = Primary
                )
                Spacer(modifier = Modifier.width(AppDimens.spacingLarge))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = headlineText,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                if (supportingText != null) {
                    Spacer(modifier = Modifier.height(AppDimens.spacingXSmall))
                    Text(
                        text = supportingText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            if (trailingContent != null) {
                Spacer(modifier = Modifier.width(AppDimens.spacingMedium))
                trailingContent()
            }
        }
    }
}

// ============================================================================
// SECTION HEADER
// ============================================================================

/**
 * Section Header - For grouping content
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppDimens.spacingMedium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary
        )
        action?.invoke()
    }
}

// ============================================================================
// DIVIDER
// ============================================================================

/**
 * Soft Divider - Subtle divider
 */
@Composable
fun SoftDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier,
        thickness = 1.dp,
        color = Divider
    )
}

// ============================================================================
// LOADING AND EMPTY STATES
// ============================================================================

/**
 * Soft Loading - Loading indicator
 */
@Composable
fun SoftLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Primary,
            strokeWidth = 3.dp
        )
    }
}

/**
 * Soft Empty State - For empty lists/content
 */
@Composable
fun SoftEmptyState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppDimens.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppDimens.iconSizeXLarge),
                tint = TextTertiary
            )
            Spacer(modifier = Modifier.height(AppDimens.spacingLarge))
        }
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(AppDimens.spacingLarge))
            action()
        }
    }
}

// ============================================================================
// CHIP COMPONENTS
// ============================================================================

/**
 * Soft Chip - For filters and tags
 */
@Composable
fun SoftChip(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) PrimaryContainer else SurfaceVariant,
        animationSpec = tween(durationMillis = AppAnimations.durationFast),
        label = "chipBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) OnPrimaryContainer else TextSecondary,
        animationSpec = tween(durationMillis = AppAnimations.durationFast),
        label = "chipContent"
    )

    if (onClick != null) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            modifier = modifier,
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
            colors = FilterChipDefaults.filterChipColors(
                containerColor = SurfaceVariant,
                selectedContainerColor = PrimaryContainer,
                labelColor = TextSecondary,
                selectedLabelColor = OnPrimaryContainer,
                iconColor = TextSecondary,
                selectedLeadingIconColor = OnPrimaryContainer
            ),
            border = FilterChipDefaults.filterChipBorder(
                borderColor = OutlineLight,
                selectedBorderColor = Primary.copy(alpha = 0.3f),
                enabled = true,
                selected = selected
            )
        )
    } else {
        SuggestionChip(
            onClick = {},
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            modifier = modifier,
            icon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            shape = RoundedCornerShape(AppDimens.cornerRadiusSmall),
            colors = SuggestionChipDefaults.suggestionChipColors(
                containerColor = backgroundColor,
                labelColor = contentColor,
                iconContentColor = contentColor
            ),
            border = SuggestionChipDefaults.suggestionChipBorder(
                borderColor = OutlineLight,
                enabled = true
            )
        )
    }
}

