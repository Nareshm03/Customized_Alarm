package com.example.teacherscheduler.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*

/**
 * DepartmentResourcesScreen - Redesigned with soft UI layout system
 *
 * Design:
 * - Clean empty state
 * - Minimal visual elements
 * - Large rounded cards (24dp)
 * - Proper spacing
 */
@Composable
fun DepartmentResourcesScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionSpacing),
            modifier = Modifier.padding(SoftLayoutDimens.screenPadding)
        ) {
            Spacer(modifier = Modifier.height(AppSpacing.screenHorizontal))

            // Icon with soft gradient background
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(AppRadius.card))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                SoftUIColors.PeachGradientStart,
                                SoftUIColors.PeachGradientEnd
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = SoftUIColors.AccentPeach
                )
            }

            Text(
                text = "Department Resources",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = TextPrimary
            )

            Text(
                text = "Access shared resources\nand documents",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            SoftContentCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "In Development",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = SoftUIColors.AccentPeach
                    )
                    Text(
                        text = "• Browse shared files\n• Download resources\n• Upload documents\n• Organize by category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
