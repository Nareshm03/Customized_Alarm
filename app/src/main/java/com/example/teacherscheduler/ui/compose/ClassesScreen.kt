package com.example.teacherscheduler.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.viewmodel.ClassesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassesScreen(
    viewModel: ClassesViewModel = hiltViewModel(),
    onClassClick: (Long) -> Unit,
    onAddClass: () -> Unit
) {
    val classes by viewModel.classes.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearch = { isSearchActive = false },
                        expanded = isSearchActive,
                        onExpandedChange = { isSearchActive = it },
                        placeholder = { Text("Search classes...") },
                        leadingIcon = { Icon(Icons.Default.Search, "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, "Clear")
                                }
                            }
                        }
                    )
                },
                expanded = isSearchActive,
                onExpandedChange = { isSearchActive = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (!isSearchActive) 16.dp else 0.dp)
            ) {
                FilteredClassList(
                    classes = classes.filter { 
                        it.title.contains(searchQuery, ignoreCase = true) ||
                        it.department.contains(searchQuery, ignoreCase = true)
                    },
                    onClassClick = { 
                        onClassClick(it)
                        isSearchActive = false
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClass) {
                Icon(Icons.Default.Add, "Add Class")
            }
        }
    ) { padding ->
        val filteredClasses = if (searchQuery.isEmpty()) classes 
            else classes.filter { 
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.department.contains(searchQuery, ignoreCase = true)
            }

        if (filteredClasses.isEmpty()) {
            LottieEmptyState(
                animationUrl = LottieAnimations.EMPTY_CLASSES,
                message = if (searchQuery.isEmpty()) "No classes yet" else "No matching classes",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredClasses, key = { it.id }) { classItem ->
                    ClassCard(
                        classItem = classItem,
                        onClick = { onClassClick(classItem.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilteredClassList(
    classes: List<Class>,
    onClassClick: (Long) -> Unit
) {
    LazyColumn {
        items(classes, key = { it.id }) { classItem ->
            ListItem(
                headlineContent = { Text(text = classItem.title) },
                supportingContent = { Text(text = "${classItem.department} • ${classItem.room}") },
                leadingContent = { Icon(Icons.Default.School, null) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ClassCard(
    classItem: Class,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(classItem.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${classItem.department} • ${classItem.room}", 
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text(text = classItem.startTime.toString())
        }
    }
}
