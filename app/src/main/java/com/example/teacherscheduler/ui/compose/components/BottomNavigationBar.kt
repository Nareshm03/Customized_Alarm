package com.example.teacherscheduler.ui.compose.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.teacherscheduler.ui.compose.AppRoutes

/**
 * BottomNavigationBar - Application's main navigation bar.
 * Uses the SoftBottomNavigation component for a consistent UI.
 *
 * All routes come from [AppRoutes] – single source of truth.
 */
@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem("Home",     Icons.Outlined.Dashboard,      AppRoutes.DASHBOARD),
        BottomNavItem("Schedule", Icons.Outlined.CalendarMonth,   AppRoutes.SCHEDULE),
        BottomNavItem("Classes",  Icons.Outlined.School,          AppRoutes.CLASSES),
        BottomNavItem("Meetings", Icons.Outlined.Event,           AppRoutes.MEETINGS),
        BottomNavItem("Tasks",    Icons.AutoMirrored.Outlined.Assignment, AppRoutes.TASKS),
        BottomNavItem("Notices",  Icons.Outlined.Notifications,  AppRoutes.NOTICES),
    )

    SoftBottomNavigation(
        items = items,
        selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0),
        onItemSelected = { index -> onNavigate(items[index].route) },
        modifier = modifier
    )
}
