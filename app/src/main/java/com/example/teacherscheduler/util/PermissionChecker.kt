package com.example.teacherscheduler.util

import com.example.teacherscheduler.model.ToDo
import com.example.teacherscheduler.model.UserProfile

/**
 * Permission Checker - Centralized permission rules
 * 
 * STEP 10: Permission Rules Matrix
 * 
 * Action              | HOD | Teacher
 * --------------------|-----|--------
 * Assign task         | ✅  | ❌
 * View all tasks      | ✅  | ❌
 * Update assigned task| ❌  | ✅
 * Send announcement   | ✅  | ❌
 * Edit department task| ✅  | ❌
 * Delete dept task    | ✅  | ❌
 * Complete task       | ✅  | ✅
 */
object PermissionChecker {

    /**
     * Can user assign tasks?
     */
    fun canAssignTasks(user: UserProfile): Boolean = user.isHOD()

    /**
     * Can user view all department tasks?
     */
    fun canViewAllTasks(user: UserProfile): Boolean = user.isHOD()

    /**
     * Can user send announcements?
     */
    fun canSendAnnouncements(user: UserProfile): Boolean = user.isHOD()

    /**
     * Can user edit this task?
     */
    fun canEditTask(user: UserProfile, task: ToDo): Boolean {
        return when {
            task.isPersonalTask() -> true // Own tasks
            task.isDepartmentTaskType() && user.isHOD() -> true // HOD can edit dept tasks
            else -> false
        }
    }

    /**
     * Can user delete this task?
     */
    fun canDeleteTask(user: UserProfile, task: ToDo): Boolean {
        return when {
            task.isPersonalTask() -> true // Own tasks
            task.isDepartmentTaskType() && user.isHOD() && task.assignedBy == user.id -> true
            else -> false
        }
    }

    /**
     * Can user update task status?
     */
    fun canUpdateTaskStatus(user: UserProfile, task: ToDo): Boolean {
        return when {
            task.isPersonalTask() -> true // Own tasks
            task.isDepartmentTaskType() && task.isAssignedToUser(user.id) -> true // Assigned tasks
            task.isDepartmentTaskType() && user.isHOD() -> true // HOD can update
            else -> false
        }
    }

    /**
     * Can user complete this task?
     */
    fun canCompleteTask(user: UserProfile, task: ToDo): Boolean {
        return canUpdateTaskStatus(user, task) && task.canCompleteTask()
    }

    /**
     * Can user access HOD dashboard?
     */
    fun canAccessHODDashboard(user: UserProfile): Boolean = user.isHOD()

    /**
     * Can user view department analytics?
     */
    fun canViewDepartmentAnalytics(user: UserProfile): Boolean = user.isHOD()

    /**
     * Get permission summary for UI
     */
    fun getPermissionSummary(user: UserProfile): Map<String, Boolean> {
        return mapOf(
            "assignTasks" to canAssignTasks(user),
            "viewAllTasks" to canViewAllTasks(user),
            "sendAnnouncements" to canSendAnnouncements(user),
            "accessDashboard" to canAccessHODDashboard(user),
            "viewAnalytics" to canViewDepartmentAnalytics(user)
        )
    }
}
