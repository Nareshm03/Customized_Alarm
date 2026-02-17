package com.example.teacherscheduler.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.teacherscheduler.data.local.ToDoDao
import com.example.teacherscheduler.model.*
import kotlinx.coroutines.flow.map

/**
 * HOD Dashboard Repository
 * Provides aggregated analytics for department task monitoring
 */
class HODDashboardRepository(private val todoDao: ToDoDao) {

    /**
     * Get complete HOD dashboard analytics
     */
    fun getDashboardAnalytics(departmentId: Long): LiveData<HODDashboard> {
        return todoDao.getDepartmentTasks(departmentId).map { tasks ->
            val completed = tasks.count { it.status == TaskStatus.COMPLETED }
            val overdue = tasks.count { it.getLifecycleStatus() == TaskStatus.OVERDUE }
            val inProgress = tasks.count { it.status == TaskStatus.IN_PROGRESS }
            val pending = tasks.count { it.status == TaskStatus.ASSIGNED }

            // Teacher-wise breakdown
            val teacherStats = tasks
                .filter { !it.isBulkTask } // Exclude bulk tasks from individual stats
                .groupBy { it.assignedTo }
                .map { (teacherId, teacherTasks) ->
                    TeacherTaskStats(
                        teacherId = teacherId,
                        teacherName = teacherTasks.firstOrNull()?.assignedTo ?: "Unknown",
                        totalAssigned = teacherTasks.size,
                        completed = teacherTasks.count { it.status == TaskStatus.COMPLETED },
                        pending = teacherTasks.count { it.status == TaskStatus.ASSIGNED },
                        overdue = teacherTasks.count { it.getLifecycleStatus() == TaskStatus.OVERDUE }
                    )
                }
                .sortedByDescending { it.totalAssigned }

            HODDashboard(
                totalTasksAssigned = tasks.size,
                completedTasks = completed,
                pendingTasks = pending,
                overdueTasks = overdue,
                inProgressTasks = inProgress,
                teacherBreakdown = teacherStats
            )
        }.asLiveData()
    }

    /**
     * Get quick stats for dashboard header
     */
    suspend fun getQuickStats(departmentId: Long): Map<String, Int> {
        val tasks = todoDao.getDepartmentTasksSync(departmentId)
        return mapOf(
            "total" to tasks.size,
            "completed" to tasks.count { it.status == TaskStatus.COMPLETED },
            "overdue" to tasks.count { it.getLifecycleStatus() == TaskStatus.OVERDUE },
            "pending" to tasks.count { it.status == TaskStatus.ASSIGNED }
        )
    }
}
