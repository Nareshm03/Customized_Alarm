package com.example.teacherscheduler.ui.compose.examples

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.teacherscheduler.ui.compose.HodAssignTaskScreen

/**
 * Example usage of HodAssignTaskScreen
 *
 * This file shows how to integrate the HOD Assign Task Screen
 * into your navigation graph or activity.
 */

/**
 * Example 1: Basic usage with navigation
 */
@Composable
fun ExampleWithNavigation(
    navController: NavController,
    departmentId: Long
) {
    HodAssignTaskScreen(
        departmentId = departmentId,
        onTaskAssigned = {
            // Navigate back after successful assignment
            navController.popBackStack()
        }
    )
}

/**
 * Example 2: Usage with custom callback
 */
@Composable
fun ExampleWithCustomCallback(
    departmentId: Long,
    onSuccess: () -> Unit
) {
    HodAssignTaskScreen(
        departmentId = departmentId,
        onTaskAssigned = {
            // Perform custom action after task assignment
            // e.g., refresh data, show toast, etc.
            onSuccess()
        }
    )
}

/**
 * Example 3: Usage in a Navigation Graph
 *
 * Add this to your NavHost composable:
 *
 * ```kotlin
 * composable(
 *     route = "hod_assign_task/{departmentId}",
 *     arguments = listOf(navArgument("departmentId") { type = NavType.LongType })
 * ) { backStackEntry ->
 *     val departmentId = backStackEntry.arguments?.getLong("departmentId") ?: 0L
 *     HodAssignTaskScreen(
 *         departmentId = departmentId,
 *         onTaskAssigned = {
 *             navController.navigate("hod_dashboard") {
 *                 popUpTo("hod_dashboard") { inclusive = false }
 *             }
 *         }
 *     )
 * }
 * ```
 */

/**
 * Example 4: Navigate to this screen
 *
 * From any composable or activity:
 *
 * ```kotlin
 * // Navigate to assign task screen
 * navController.navigate("hod_assign_task/$departmentId")
 * ```
 */

/**
 * Example 5: Usage with ViewModel dependency injection (Hilt)
 *
 * If using Hilt, you can inject the ViewModel:
 *
 * ```kotlin
 * @Composable
 * fun ExampleWithHilt(
 *     departmentId: Long,
 *     viewModel: DepartmentViewModel = hiltViewModel()
 * ) {
 *     HodAssignTaskScreen(
 *         departmentId = departmentId,
 *         onTaskAssigned = { /* ... */ },
 *         viewModel = viewModel
 *     )
 * }
 * ```
 */

/**
 * Example 6: Testing scenario
 *
 * ```kotlin
 * @Test
 * fun testAssignTaskScreen() {
 *     composeTestRule.setContent {
 *         HodAssignTaskScreen(
 *             departmentId = 1L,
 *             onTaskAssigned = { /* assertion here */ }
 *         )
 *     }
 *
 *     // Test interactions
 *     composeTestRule.onNodeWithText("Task Title").performTextInput("Test Task")
 *     composeTestRule.onNodeWithText("Assign Task").performClick()
 * }
 * ```
 */

