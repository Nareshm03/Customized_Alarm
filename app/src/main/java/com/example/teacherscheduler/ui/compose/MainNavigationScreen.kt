package com.example.teacherscheduler.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationScreen(
    onProfileClick: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    SoftIconButton(
                        icon = Icons.Outlined.Person,
                        onClick = onProfileClick,
                        contentDescription = "Profile",
                        backgroundColor = Color.White,
                        size = 44.dp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        },
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo("dashboard") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(padding)
        ) {
            composable("dashboard") {
                EnhancedDashboardScreen(
                    onNavigateToAddClass = { },
                    onNavigateToAddMeeting = { },
                    onClassClick = { }
                )
            }
            composable("timetable") {
                TimetablePlaceholder()
            }
            composable("classes") {
                ClassesScreen(
                    onClassClick = { },
                    onAddClass = { }
                )
            }
            composable("meetings") {
                MeetingsPlaceholder()
            }
            composable("todos") {
                ToDosPlaceholder()
            }
        }
    }
}

@Composable
private fun TimetablePlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        SoftEmptyStateCard(
            icon = Icons.Outlined.CalendarMonth,
            title = "Timetable",
            subtitle = "View your weekly schedule"
        )
    }
}

@Composable
private fun MeetingsPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        SoftEmptyStateCard(
            icon = Icons.Outlined.Event,
            title = "Meetings",
            subtitle = "Manage your meetings"
        )
    }
}

@Composable
private fun ToDosPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        SoftEmptyStateCard(
            icon = Icons.AutoMirrored.Outlined.Assignment,
            title = "To-Dos",
            subtitle = "Track your tasks"
        )
    }
}
