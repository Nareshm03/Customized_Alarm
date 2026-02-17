package com.example.teacherscheduler.util

import android.content.Context
import android.util.Log
import com.example.teacherscheduler.data.local.AppDatabase
import com.example.teacherscheduler.model.TaskStatus
import com.example.teacherscheduler.model.ToDo
import com.example.teacherscheduler.notification.EnhancedNotificationHelper
import kotlinx.coroutines.flow.first

/**
 * TaskLifecycleManager - Manages task lifecycle transitions and automated notifications
 *
 * STEP 4: Task Lifecycle Management
 *
 * Lifecycle States:
 * - ASSIGNED: Task created and assigned
 * - IN_PROGRESS: Teacher started working on it
 * - COMPLETED: Task finished
 * - OVERDUE: Past due date and not completed
 *
 * Rules:
 * - Reminders stop after completion
 * - Overdue tasks notify HOD automatically
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
class TaskLifecycleManager(private val context: Context) {

    private val database = AppDatabase.getInstance(context)
    private val todoDao = database.todoDao()
    private val notificationHelper = EnhancedNotificationHelper(context)

    companion object {
        private const val TAG = "TaskLifecycleManager"

        @Volatile
        @Suppress("StaticFieldLeak") // Application context is safe
        private var instance: TaskLifecycleManager? = null

        fun getInstance(context: Context): TaskLifecycleManager {
            return instance ?: synchronized(this) {
                instance ?: TaskLifecycleManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // ==================== LIFECYCLE TRANSITIONS ====================

    /**
     * Start task (ASSIGNED → IN_PROGRESS)
     */
    suspend fun startTask(taskId: Long): Result<Unit> {
        return try {
            val task = todoDao.getToDoById(taskId)

            if (task == null) {
                return Result.failure(Exception("Task not found"))
            }

            if (!task.canStartTask()) {
                return Result.failure(Exception("Task cannot be started from current state: ${task.status}"))
            }

            todoDao.startTask(taskId)
            Log.d(TAG, "Task $taskId started: ${task.title}")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting task $taskId", e)
            Result.failure(e)
        }
    }

    /**
     * Complete task (Any state → COMPLETED)
     * Rule: Reminders stop after completion
     */
    suspend fun completeTask(taskId: Long, notes: String = ""): Result<Unit> {
        return try {
            val task = todoDao.getToDoById(taskId)

            if (task == null) {
                return Result.failure(Exception("Task not found"))
            }

            if (!task.canCompleteTask()) {
                return Result.failure(Exception("Task cannot be completed from current state: ${task.status}"))
            }

            // Mark as completed
            todoDao.completeTask(taskId, notes = notes)

            // Cancel any pending reminders (rule: reminders stop after completion)
            cancelTaskReminders(task)

            Log.d(TAG, "Task $taskId completed: ${task.title}")

            // Send completion notification to HOD if department task
            if (task.isDepartmentTaskType() && task.assignedBy.isNotEmpty()) {
                sendCompletionNotificationToHOD(task)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error completing task $taskId", e)
            Result.failure(e)
        }
    }

    /**
     * Update task status manually
     */
    suspend fun updateTaskStatus(taskId: Long, newStatus: TaskStatus): Result<Unit> {
        return try {
            val task = todoDao.getToDoById(taskId)

            if (task == null) {
                return Result.failure(Exception("Task not found"))
            }

            // Check if transition is allowed
            if (!task.getAllowedTransitions().contains(newStatus)) {
                return Result.failure(Exception("Invalid transition from ${task.status} to $newStatus"))
            }

            val startedAt = if (newStatus == TaskStatus.IN_PROGRESS && task.startedAt == null) {
                System.currentTimeMillis()
            } else {
                task.startedAt
            }

            todoDao.updateTaskStatus(taskId, newStatus, startedAt)
            Log.d(TAG, "Task $taskId status updated to $newStatus")

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task status $taskId", e)
            Result.failure(e)
        }
    }

    // ==================== AUTOMATED NOTIFICATIONS ====================

    /**
     * Check and notify HOD about overdue tasks
     * Rule: Overdue notifies HOD automatically
     */
    suspend fun checkAndNotifyOverdueTasks(): Int {
        return try {
            val overdueTasks = todoDao.getOverdueTasksForHODNotification()
            var notifiedCount = 0

            overdueTasks.forEach { task ->
                if (sendOverdueNotificationToHOD(task)) {
                    todoDao.markOverdueNotificationSent(task.id)
                    notifiedCount++
                }
            }

            if (notifiedCount > 0) {
                Log.d(TAG, "Notified HODs about $notifiedCount overdue tasks")
            }

            notifiedCount
        } catch (e: Exception) {
            Log.e(TAG, "Error checking overdue tasks", e)
            0
        }
    }

    /**
     * Send overdue notification to HOD
     */
    private fun sendOverdueNotificationToHOD(task: ToDo): Boolean {
        return try {
            val title = "⚠️ Task Overdue"
            val message = buildString {
                append("Task: ${task.title}\n")
                append("Assigned to: ${task.assignedToName}\n")
                append("Due: ${task.getFormattedDueDate()}\n")
                append("Overdue by: ${task.getLifecycleDescription()}")
            }

            // Send notification to HOD
            notificationHelper.sendCustomNotification(
                title = title,
                message = message,
                channelId = EnhancedNotificationHelper.CHANNEL_ID_REMINDERS
            )

            Log.d(TAG, "Overdue notification sent to HOD for task: ${task.title}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sending overdue notification", e)
            false
        }
    }

    /**
     * Send completion notification to HOD
     */
    private fun sendCompletionNotificationToHOD(task: ToDo) {
        try {
            val title = "✅ Task Completed"
            val message = buildString {
                append("Task: ${task.title}\n")
                append("Completed by: ${task.assignedToName}\n")
                if (task.completionNotes.isNotEmpty()) {
                    append("Notes: ${task.completionNotes}")
                }
            }

            notificationHelper.sendCustomNotification(
                title = title,
                message = message,
                channelId = EnhancedNotificationHelper.CHANNEL_ID_REMINDERS
            )

            Log.d(TAG, "Completion notification sent to HOD for task: ${task.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending completion notification", e)
        }
    }

    /**
     * Cancel task reminders
     * Rule: Reminders stop after completion
     */
    private fun cancelTaskReminders(task: ToDo) {
        try {
            // Cancel any scheduled notifications for this task
            notificationHelper.cancelNotification(task.id.toInt())
            Log.d(TAG, "Cancelled reminders for completed task: ${task.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling reminders", e)
        }
    }

    /**
     * Schedule reminders for active tasks
     * Rule: Only send reminders for non-completed tasks
     */
    suspend fun scheduleTaskReminders() {
        try {
            val tasksForReminders = todoDao.getTasksForReminders()

            tasksForReminders.forEach { task ->
                if (task.shouldSendReminders()) {
                    scheduleReminderForTask(task)
                }
            }

            Log.d(TAG, "Scheduled reminders for ${tasksForReminders.size} tasks")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminders", e)
        }
    }

    /**
     * Schedule reminder for a specific task
     */
    private fun scheduleReminderForTask(task: ToDo) {
        try {
            if (task.dueDate == null || !task.shouldSendReminders()) return

            val reminderTime = task.dueDate.time - (task.reminderMinutes * 60 * 1000)
            val now = System.currentTimeMillis()

            if (reminderTime > now) {
                notificationHelper.scheduleTaskReminder(
                    taskId = task.id.toInt(),
                    title = task.title,
                    description = task.description,
                    reminderTime = reminderTime
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminder for task ${task.id}", e)
        }
    }

    // ==================== LIFECYCLE STATISTICS ====================

    /**
     * Get lifecycle statistics for user
     */
    suspend fun getLifecycleStats(userId: String): LifecycleStatistics {
        return try {
            val stats = todoDao.getLifecycleStatistics(userId)

            LifecycleStatistics(
                assigned = stats["ASSIGNED"] ?: 0,
                inProgress = stats["IN_PROGRESS"] ?: 0,
                completed = stats["COMPLETED"] ?: 0,
                overdue = stats["OVERDUE"] ?: 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting lifecycle stats", e)
            LifecycleStatistics()
        }
    }

    /**
     * Get tasks by lifecycle status
     */
    suspend fun getTasksByStatus(userId: String, status: TaskStatus) =
        todoDao.getTasksByStatus(userId, status).first()

    /**
     * Check all tasks and update overdue status
     */
    suspend fun updateOverdueStatuses(): Int {
        return try {
            val allTasks = todoDao.getAllToDosSync()
            var updatedCount = 0

            allTasks.forEach { task ->
                if (task.isOverdue() && task.status != TaskStatus.COMPLETED && task.status != TaskStatus.OVERDUE) {
                    todoDao.updateTaskStatus(task.id, TaskStatus.OVERDUE)
                    updatedCount++
                }
            }

            if (updatedCount > 0) {
                Log.d(TAG, "Updated $updatedCount tasks to OVERDUE status")
            }

            updatedCount
        } catch (e: Exception) {
            Log.e(TAG, "Error updating overdue statuses", e)
            0
        }
    }
}

/**
 * Lifecycle statistics data class
 */
@Suppress("unused")
data class LifecycleStatistics(
    val assigned: Int = 0,
    val inProgress: Int = 0,
    val completed: Int = 0,
    val overdue: Int = 0
) {
    val total: Int get() = assigned + inProgress + completed + overdue
    val activeCount: Int get() = assigned + inProgress
    val completionRate: Float get() = if (total > 0) (completed.toFloat() / total) * 100 else 0f
}

