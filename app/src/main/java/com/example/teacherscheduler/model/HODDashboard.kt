package com.example.teacherscheduler.model

/**
 * HOD Dashboard Analytics
 * Aggregated statistics for department task monitoring
 */
data class HODDashboard(
    val totalTasksAssigned: Int = 0,
    val completedTasks: Int = 0,
    val pendingTasks: Int = 0,
    val overdueTasks: Int = 0,
    val inProgressTasks: Int = 0,
    val teacherBreakdown: List<TeacherTaskStats> = emptyList()
) {
    val completionRate: Float
        get() = if (totalTasksAssigned > 0) {
            (completedTasks.toFloat() / totalTasksAssigned) * 100
        } else 0f

    val overdueRate: Float
        get() = if (totalTasksAssigned > 0) {
            (overdueTasks.toFloat() / totalTasksAssigned) * 100
        } else 0f
}

/**
 * Teacher-wise task statistics
 */
data class TeacherTaskStats(
    val teacherId: String,
    val teacherName: String,
    val totalAssigned: Int = 0,
    val completed: Int = 0,
    val pending: Int = 0,
    val overdue: Int = 0
) {
    val completionRate: Float
        get() = if (totalAssigned > 0) {
            (completed.toFloat() / totalAssigned) * 100
        } else 0f
}
