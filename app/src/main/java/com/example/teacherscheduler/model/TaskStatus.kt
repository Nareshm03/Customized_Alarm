package com.example.teacherscheduler.model

/**
 * Task Status - Lifecycle states for department tasks
 */
enum class TaskStatus(val displayName: String, val color: String) {
    ASSIGNED("Assigned", "#FFC107"),        // Yellow - Task assigned, not started
    PENDING("Pending", "#FFC107"),          // Alias for ASSIGNED if needed
    IN_PROGRESS("In Progress", "#2196F3"),  // Blue - Work in progress
    COMPLETED("Completed", "#4CAF50"),      // Green - Task completed
    OVERDUE("Overdue", "#F44336");          // Red - Past due date

    fun isActive(): Boolean = this != COMPLETED
}
