package com.example.teacherscheduler.ui.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*
import com.example.teacherscheduler.viewmodel.DepartmentViewModel
import kotlinx.coroutines.launch

/**
 * DepartmentScreen - Redesigned with soft UI layout system
 *
 * Design:
 * - White background (no heavy colors)
 * - 24dp horizontal padding
 * - Clean pill-style tabs
 * - Section-based layout
 * - Modern minimal style
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentScreen(
    modifier: Modifier = Modifier,
    viewModel: DepartmentViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    val tabs = listOf("Tasks", "Notices", "Resources")

    Scaffold(
        containerColor = BackgroundPrimary
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header with proper top spacing
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SoftLayoutDimens.screenPadding)
                    .padding(top = SoftLayoutDimens.topSpacing)
            ) {
                Text(
                    text = "Department",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp
                    ),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(SoftLayoutDimens.sectionSpacing))
            }

            // Soft Tab Row
            SoftPillTabRow(
                tabs = tabs,
                selectedTabIndex = pagerState.currentPage,
                onTabSelected = { index ->
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                modifier = Modifier.padding(horizontal = SoftLayoutDimens.screenPadding)
            )

            Spacer(modifier = Modifier.height(SoftLayoutDimens.sectionSpacing))

            // Horizontal Pager for tab content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> DepartmentTasksScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = SoftLayoutDimens.screenPadding)
                    )
                    1 -> DepartmentNoticesScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = SoftLayoutDimens.screenPadding)
                    )
                    2 -> DepartmentResourcesScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = SoftLayoutDimens.screenPadding)
                    )
                }
            }
        }
    }
}

/**
 * SoftPillTabRow - Clean pill-style tab row
 */
@Composable
private fun SoftPillTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SoftUIColors.ChipBackground)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTabIndex == index
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else Color.Transparent,
                animationSpec = tween(durationMillis = 200),
                label = "tabBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) SoftUIColors.AccentLavender else TextSecondary,
                animationSpec = tween(durationMillis = 200),
                label = "tabText"
            )

            Surface(
                onClick = { onTabSelected(index) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = backgroundColor,
                shadowElevation = if (isSelected) 2.dp else 0.dp
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        ),
                        color = textColor
                    )
                }
            }
        }
    }
}
