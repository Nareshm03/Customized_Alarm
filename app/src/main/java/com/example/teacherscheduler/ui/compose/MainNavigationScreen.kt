package com.example.teacherscheduler.ui.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*

/**
 * Single source of truth for all route names.
 */
object AppRoutes {
    // Main tabs (shown in bottom nav)
    const val DASHBOARD = "dashboard"
    const val SCHEDULE = "schedule"
    const val CLASSES = "classes"
    const val MEETINGS = "meetings"
    const val TASKS = "tasks"
    const val NOTICES = "notices"

    // Detail / Add screens (no bottom nav)
    const val ADD_CLASS = "add_class"
    const val ADD_MEETING = "add_meeting"
    const val ADD_TASK = "add_task"
    const val CREATE_NOTICE = "create_notice"

    // Utility screens (no bottom nav)
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
}

/** Routes that show the bottom navigation bar. */
private val TAB_ROUTES = setOf(
    AppRoutes.DASHBOARD,
    AppRoutes.SCHEDULE,
    AppRoutes.CLASSES,
    AppRoutes.MEETINGS,
    AppRoutes.TASKS,
    AppRoutes.NOTICES
)

// ─────────────────────────────────────────────────────────────────────────────
// Root Composable – ONE NavController, ONE Scaffold, ONE NavHost
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MainNavigationScreen(
    onProfileClick: () -> Unit
) {
    // ── Single NavController for the entire app ──
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar only on main tab screens
    val showBottomBar = currentRoute in TAB_ROUTES

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute ?: AppRoutes.DASHBOARD,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            // Pop up to the start destination of the graph to avoid
                            // building up a large stack of destinations on the back stack.
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = AppRoutes.DASHBOARD,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(250)) },
            exitTransition = { fadeOut(animationSpec = tween(250)) },
        ) {

            // ═════════════════════════════════════════════════════════════════
            //  TAB SCREENS (bottom nav visible)
            // ═════════════════════════════════════════════════════════════════

            composable(AppRoutes.DASHBOARD) {
                EnhancedDashboardScreen(
                    onNavigateToAddClass = { navController.navigate(AppRoutes.ADD_CLASS) },
                    onNavigateToAddMeeting = { navController.navigate(AppRoutes.ADD_MEETING) },
                    onClassClick = { /* TODO: class detail */ },
                    onNavigateToSettings = { navController.navigate(AppRoutes.SETTINGS) },
                    onNavigateToNotifications = { navController.navigate(AppRoutes.NOTIFICATIONS) },
                    onNavigateToProfile = onProfileClick,
                )
            }

            composable(AppRoutes.SCHEDULE) {
                TimetableScreen(
                    onAddClass = { navController.navigate(AppRoutes.ADD_CLASS) },
                    onAddMeeting = { navController.navigate(AppRoutes.ADD_MEETING) },
                )
            }

            composable(AppRoutes.CLASSES) {
                ClassesScreen(
                    onClassClick = { /* TODO: class detail */ },
                    onAddClass = { navController.navigate(AppRoutes.ADD_CLASS) },
                )
            }

            composable(AppRoutes.MEETINGS) {
                MeetingsScreen(
                    onMeetingClick = { /* TODO: meeting detail */ },
                    onAddMeeting = { navController.navigate(AppRoutes.ADD_MEETING) },
                )
            }

            composable(AppRoutes.TASKS) {
                ToDosScreen(
                    onToDoClick = { /* TODO: task detail */ },
                    onAddToDo = { navController.navigate(AppRoutes.ADD_TASK) },
                )
            }

            composable(AppRoutes.NOTICES) {
                NoticeBoardScreen(
                    onCreateNotice = { navController.navigate(AppRoutes.CREATE_NOTICE) }
                )
            }

            // ═════════════════════════════════════════════════════════════════
            //  ADD / EDIT SCREENS (no bottom nav, slide-up transition)
            // ═════════════════════════════════════════════════════════════════

            composable(
                AppRoutes.ADD_CLASS,
                enterTransition = { slideInVertically(tween(350)) { it } + fadeIn(tween(350)) },
                exitTransition  = { slideOutVertically(tween(350)) { it } + fadeOut(tween(350)) },
            ) {
                AddEditClassScreen(
                    onSave   = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                )
            }

            composable(
                AppRoutes.ADD_MEETING,
                enterTransition = { slideInVertically(tween(350)) { it } + fadeIn(tween(350)) },
                exitTransition  = { slideOutVertically(tween(350)) { it } + fadeOut(tween(350)) },
            ) {
                AddEditMeetingScreen(
                    onSave   = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                )
            }

            composable(
                AppRoutes.ADD_TASK,
                enterTransition = { slideInVertically(tween(350)) { it } + fadeIn(tween(350)) },
                exitTransition  = { slideOutVertically(tween(350)) { it } + fadeOut(tween(350)) },
            ) {
                AddEditToDoScreen(
                    onSave   = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                )
            }

            composable(
                AppRoutes.CREATE_NOTICE,
                enterTransition = { slideInVertically(tween(350)) { it } + fadeIn(tween(350)) },
                exitTransition  = { slideOutVertically(tween(350)) { it } + fadeOut(tween(350)) },
            ) {
                CreateNoticeScreen(
                    onSave   = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() },
                )
            }

            // ═════════════════════════════════════════════════════════════════
            //  UTILITY SCREENS (no bottom nav, slide-up transition)
            // ═════════════════════════════════════════════════════════════════

            composable(
                AppRoutes.SETTINGS,
                enterTransition = { slideInVertically(tween(350)) { it } + fadeIn(tween(350)) },
                exitTransition  = { slideOutVertically(tween(350)) { it } + fadeOut(tween(350)) },
            ) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(
                AppRoutes.NOTIFICATIONS,
                enterTransition = { slideInVertically(tween(350)) { it } + fadeIn(tween(350)) },
                exitTransition  = { slideOutVertically(tween(350)) { it } + fadeOut(tween(350)) },
            ) {
                NotificationsScreen(
                    onNavigateBack = { navController.popBackStack() },
                )
            }

            composable(
                AppRoutes.PROFILE,
                enterTransition = { slideInVertically(tween(350)) { it } + fadeIn(tween(350)) },
                exitTransition  = { slideOutVertically(tween(350)) { it } + fadeOut(tween(350)) },
            ) {
                ProfileScreen()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Placeholder for Add Task (until full screen is built)
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskPlaceholder(onCancel: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SoftTopAppBar(
                title = "Add Task",
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary,
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SoftEmptyStateCard(
                    icon = Icons.AutoMirrored.Outlined.Assignment,
                    title = "Add Task",
                    subtitle = "Task creation coming soon",
                )
                TextButton(onClick = onCancel) {
                    Text("Cancel")
                }
            }
        }
    }
}
