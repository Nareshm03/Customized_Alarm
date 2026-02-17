package com.example.teacherscheduler.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*
import com.example.teacherscheduler.viewmodel.ClassesViewModel
import com.example.teacherscheduler.viewmodel.ClassesData
import com.example.teacherscheduler.viewmodel.UiState

/**
 * ClassesScreen - Redesigned with soft UI layout system
 *
 * Design:
 * - White/soft background
 * - 24dp horizontal padding
 * - 32dp top spacing
 * - Large rounded cards (24dp)
 * - Section-based layout
 * - Minimal visual clutter
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    viewModel: ClassesViewModel = hiltViewModel(),
    onClassClick: (Long) -> Unit,
    onAddClass: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    val classes = when (uiState) {
        is UiState.Success -> (uiState as UiState.Success<ClassesData>).data.classes
        else -> emptyList()
    }

    val filteredClasses = if (searchQuery.isEmpty()) {
        classes
    } else {
        classes.filter {
            it.subject.contains(searchQuery, ignoreCase = true) ||
            it.department.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = BackgroundPrimary,
        floatingActionButton = {
            IconCircleButton(
                icon = Icons.Default.Add,
                onClick = onAddClass,
                contentDescription = "Add Class",
                size = 56.dp
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = SoftLayoutDimens.screenPadding,
                end = SoftLayoutDimens.screenPadding,
                top = SoftLayoutDimens.topSpacing,
                bottom = SoftLayoutDimens.bottomSafeArea
            ),
            verticalArrangement = Arrangement.spacedBy(SoftLayoutDimens.sectionSpacing)
        ) {
            // Header
            item {
                Text(
                    text = "Classes",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
            }

            // Search Field
            item {
                SoftSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Search classes..."
                )
            }

            // Classes Section
            if (filteredClasses.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Outlined.School,
                        title = if (searchQuery.isEmpty()) "No classes yet" else "No matching classes",
                        subtitle = if (searchQuery.isEmpty()) "Add your first class to get started" else "Try a different search term",
                        actionText = if (searchQuery.isEmpty()) "Add Class" else null,
                        onActionClick = if (searchQuery.isEmpty()) onAddClass else null
                    )
                }
            } else {
                item {
                    Text(
                        text = "All Classes (${filteredClasses.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextPrimary
                    )
                }

                itemsIndexed(
                    items = filteredClasses,
                    key = { _, item -> item.id }
                ) { _, classItem ->
                    SoftClassCard(
                        classItem = classItem,
                        onClick = { onClassClick(classItem.id) }
                    )
                }
            }
        }
    }
}

// ============================================================================
// SEARCH FIELD
// ============================================================================

@Composable
private fun SoftSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                color = TextTertiary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = TextSecondary
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = TextSecondary
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF7F4EF),
            unfocusedContainerColor = Color(0xFFF7F4EF),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = Color(0xFFD8B4A0)
        )
    )
}

// ============================================================================
// CLASS CARD
// ============================================================================

@Composable
private fun SoftClassCard(
    classItem: Class,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SoftContentCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SoftLayoutDimens.itemSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with soft background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF7F4EF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.School,
                    contentDescription = null,
                    tint = Color(0xFF2B2B2B),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = classItem.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "${classItem.department} • ${classItem.room}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // Time
            Text(
                text = classItem.getFormattedTime().split(" - ").firstOrNull() ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}
