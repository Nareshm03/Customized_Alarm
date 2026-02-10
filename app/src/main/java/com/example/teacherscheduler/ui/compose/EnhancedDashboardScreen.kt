package com.example.teacherscheduler.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.teacherscheduler.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedDashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToAddClass: () -> Unit,
    onNavigateToAddMeeting: () -> Unit,
    onClassClick: (Long) -> Unit
) {
    val uiState by viewModel.dashboardState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.greeting) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = onNavigateToAddClass,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.Add, "Add Class")
                }
                FloatingActionButton(onClick = onNavigateToAddMeeting) {
                    Icon(Icons.Default.Event, "Add Meeting")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatisticsCards(
                    todayClassesCount = uiState.todayClassesCount,
                    upcomingMeetingsCount = uiState.upcomingMeetingsCount
                )
            }
            
            item {
                Text("Today's Classes", style = MaterialTheme.typography.titleLarge)
            }
            
            if (uiState.todayClasses.isEmpty() && !uiState.isLoading) {
                item {
                    LottieEmptyState(
                        animationUrl = LottieAnimations.EMPTY_CLASSES,
                        message = "No classes scheduled for today",
                        modifier = Modifier.height(300.dp)
                    )
                }
            } else {
                items(uiState.todayClasses, key = { it.id }) { classItem ->
                    ClassCard(
                        classItem = classItem,
                        onClick = { onClassClick(classItem.id) }
                    )
                }
            }
            
            item {
                Text(
                    "Upcoming Meetings",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            if (uiState.upcomingMeetings.isEmpty() && !uiState.isLoading) {
                item {
                    LottieEmptyState(
                        animationUrl = LottieAnimations.EMPTY_MEETINGS,
                        message = "No upcoming meetings",
                        modifier = Modifier.height(300.dp)
                    )
                }
            } else {
                items(uiState.upcomingMeetings, key = { it.id }) { meeting ->
                    MeetingCard(meeting)
                }
            }
        }
    }
}

@Composable
private fun StatisticsCards(
    todayClassesCount: Int,
    upcomingMeetingsCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(todayClassesCount.toString(), style = MaterialTheme.typography.displayMedium)
                Text("Today's Classes")
            }
        }
        
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(upcomingMeetingsCount.toString(), style = MaterialTheme.typography.displayMedium)
                Text("Upcoming Meetings")
            }
        }
    }
}

@Composable
private fun ClassCard(
    classItem: com.example.teacherscheduler.model.Class,
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
                Text(
                    classItem.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text("${classItem.department} • ${classItem.room}", 
                     style = MaterialTheme.typography.bodyMedium)
            }
            Text(classItem.getFormattedTime())
        }
    }
}

@Composable
private fun MeetingCard(meeting: com.example.teacherscheduler.model.Meeting) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(meeting.title, style = MaterialTheme.typography.titleMedium)
                Text(meeting.location, style = MaterialTheme.typography.bodyMedium)
            }
            Text(meeting.getFormattedTime())
        }
    }
}
