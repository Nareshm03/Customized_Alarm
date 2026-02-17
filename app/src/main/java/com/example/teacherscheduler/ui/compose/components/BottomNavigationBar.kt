package com.example.teacherscheduler.ui.compose.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * BottomNavigationBar - Application's main navigation bar.
 * Uses the SoftBottomNavigation component for a consistent UI.
 */
@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // These BottomNavItem objects refer to the data class defined in SoftBottomNavigation.kt
    val items = listOf(
        BottomNavItem("Dashboard", Icons.Outlined.Dashboard, "dashboard"),
        BottomNavItem("Timetable", Icons.Outlined.CalendarMonth, "timetable"),
        BottomNavItem("Classes", Icons.Outlined.School, "classes"),
        BottomNavItem("Meetings", Icons.Outlined.Event, "meetings"),
        BottomNavItem("To-Dos", Icons.AutoMirrored.Outlined.Assignment, "todos")
    )

    SoftBottomNavigation(
        items = items,
        selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0),
        onItemSelected = { index -> onNavigate(items[index].route) },
        modifier = modifier
    )
}
