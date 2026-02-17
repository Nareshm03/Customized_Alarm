package com.example.teacherscheduler.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*

/**
 * Example screen demonstrating TwoColumnGridCardLayout
 */
@Composable
fun GridCardLayoutExamplesScreen() {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = SoftLayoutDimens.screenPadding)
                .padding(top = SoftLayoutDimens.topSpacing)
        ) {
            // Header
            Text(
                text = "Grid Card Examples",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp
                ),
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Grid with sample items
            TwoColumnGridCardLayout(
                items = createSampleGridItems(),
                onItemClick = { item ->
                    // Handle item click
                    println("Clicked: ${item.title}")
                },
                contentPadding = PaddingValues(bottom = 100.dp)
            )
        }
    }
}

/**
 * Real-world example: Classes grid view
 */
@Composable
fun ClassesGridViewExample() {
    val classItems = remember {
        listOf(
            GridCardItem(
                id = "math101",
                icon = Icons.Outlined.Calculate,
                title = "Mathematics 101",
                subtitle = "Room 301 • 10:00 AM",
                chips = listOf("Mon", "Wed", "Fri"),
                progress = 0.75f,
                progressLabel = "Semester",
                iconBackgroundColor = SoftUIColors.LavenderGradientStart,
                iconTint = SoftUIColors.AccentLavender,
                progressColor = SoftUIColors.AccentLavender
            ),
            GridCardItem(
                id = "physics",
                icon = Icons.Outlined.Science,
                title = "Physics Lab",
                subtitle = "Lab 202 • 2:00 PM",
                chips = listOf("Tue", "Thu"),
                progress = 0.50f,
                progressLabel = "Progress",
                iconBackgroundColor = SoftUIColors.BlueGradientStart,
                iconTint = SoftUIColors.AccentBlue,
                progressColor = SoftUIColors.AccentBlue
            ),
            GridCardItem(
                id = "english",
                icon = Icons.Outlined.Book,
                title = "English Lit",
                subtitle = "Room 105 • Daily",
                chips = listOf("Reading", "Writing"),
                progress = 0.85f,
                progressLabel = "Complete",
                iconBackgroundColor = SoftUIColors.MintGradientStart,
                iconTint = SoftUIColors.AccentMint,
                progressColor = SoftUIColors.AccentMint
            ),
            GridCardItem(
                id = "history",
                icon = Icons.Outlined.HistoryEdu,
                title = "World History",
                subtitle = "Room 210 • 11:00 AM",
                chips = listOf("Mon", "Wed"),
                progress = 0.40f,
                progressLabel = "Progress",
                iconBackgroundColor = SoftUIColors.CoralGradientStart,
                iconTint = SoftUIColors.AccentCoral,
                progressColor = SoftUIColors.AccentCoral
            )
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            SoftFloatingActionButton(
                onClick = { /* Add class */ },
                icon = Icons.Outlined.Add,
                contentDescription = "Add Class"
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header with top padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SoftLayoutDimens.screenPadding)
                    .padding(top = SoftLayoutDimens.topSpacing)
            ) {
                Text(
                    text = "My Classes",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp
                    ),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "4 active classes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Grid
            TwoColumnGridCardLayout(
                items = classItems,
                onItemClick = { classItem ->
                    // Navigate to class details
                    println("View class: ${classItem.title}")
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = SoftLayoutDimens.screenPadding,
                    end = SoftLayoutDimens.screenPadding,
                    bottom = 100.dp
                )
            )
        }
    }
}

/**
 * Example: Subjects overview grid
 */
@Composable
fun SubjectsOverviewGridExample() {
    val subjects = remember {
        listOf(
            GridCardItem(
                id = "1",
                icon = Icons.Outlined.Functions,
                title = "Mathematics",
                subtitle = "3 classes",
                chips = listOf("Algebra", "Calculus"),
                progress = 0.70f,
                progressLabel = "Overall",
                iconBackgroundColor = SoftUIColors.LavenderGradientStart,
                iconTint = SoftUIColors.AccentLavender,
                progressColor = SoftUIColors.AccentLavender
            ),
            GridCardItem(
                id = "2",
                icon = Icons.Outlined.Science,
                title = "Sciences",
                subtitle = "4 classes",
                chips = listOf("Physics", "Chemistry", "Biology"),
                progress = 0.55f,
                progressLabel = "Overall",
                iconBackgroundColor = SoftUIColors.BlueGradientStart,
                iconTint = SoftUIColors.AccentBlue,
                progressColor = SoftUIColors.AccentBlue
            ),
            GridCardItem(
                id = "3",
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                title = "Languages",
                subtitle = "2 classes",
                chips = listOf("English", "Spanish"),
                progress = 0.80f,
                progressLabel = "Overall",
                iconBackgroundColor = SoftUIColors.MintGradientStart,
                iconTint = SoftUIColors.AccentMint,
                progressColor = SoftUIColors.AccentMint
            ),
            GridCardItem(
                id = "4",
                icon = Icons.Outlined.Public,
                title = "Social Studies",
                subtitle = "2 classes",
                chips = listOf("History", "Geography"),
                progress = 0.45f,
                progressLabel = "Overall",
                iconBackgroundColor = SoftUIColors.PeachGradientStart,
                iconTint = SoftUIColors.AccentPeach,
                progressColor = SoftUIColors.AccentPeach
            ),
            GridCardItem(
                id = "5",
                icon = Icons.Outlined.Palette,
                title = "Arts",
                subtitle = "1 class",
                chips = listOf("Visual Arts"),
                progress = 0.90f,
                progressLabel = "Overall",
                iconBackgroundColor = SoftUIColors.CoralGradientStart,
                iconTint = SoftUIColors.AccentCoral,
                progressColor = SoftUIColors.AccentCoral
            ),
            GridCardItem(
                id = "6",
                icon = Icons.Outlined.Psychology,
                title = "Electives",
                subtitle = "2 classes",
                chips = listOf("Psychology"),
                progress = 0.65f,
                progressLabel = "Overall",
                iconBackgroundColor = SoftUIColors.LavenderGradientEnd,
                iconTint = SoftUIColors.AccentLavender,
                progressColor = SoftUIColors.AccentLavender
            )
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SoftLayoutDimens.screenPadding)
                    .padding(top = SoftLayoutDimens.topSpacing)
            ) {
                Text(
                    text = "Subjects Overview",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp
                    ),
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TwoColumnGridCardLayout(
                items = subjects,
                onItemClick = { subject ->
                    println("View subject: ${subject.title}")
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = SoftLayoutDimens.screenPadding,
                    end = SoftLayoutDimens.screenPadding,
                    bottom = 24.dp
                )
            )
        }
    }
}

