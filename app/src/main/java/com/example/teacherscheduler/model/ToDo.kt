package com.example.teacherscheduler.model

import androidx.core.graphics.toColorInt
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
@Entity(
    tableName = "todos",
    indices = [
        Index(value = ["isCompleted", "dueDate"]),
        Index(value = ["priority"]),
        Index(value = ["category"]),
        Index(value = ["taskType"]),
        Index(value = ["assignedTo"]),
        Index(value = ["status"]),
        Index(value = ["lastSyncTimestamp"])
    ]
)
data class ToDo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val description: String = "",
    val category: String = "",
    val priority: Priority = Priority.MEDIUM,
    val dueDate: Date? = null,
    val reminderTime: Date? = null,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notificationsEnabled: Boolean = true,
    val reminderMinutes: Int = 15,
    val tags: List<String> = emptyList(),
    val isActive: Boolean = true,
    val semesterId: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncTimestamp: Long = 0,

    // STEP 3: Department Task Fields
    val taskType: TaskType = TaskType.PERSONAL,
    val isDepartmentTask: Boolean = false,
    val assignedBy: String = "",
    val assignedByName: String = "",
    val assignedTo: String = "",
    val assignedToName: String = "",          // Cached assignee name
    val isBulkTask: Boolean = false,
    val departmentId: Long = 0,
    val completionNotes: String = "",

    // STEP 4: Lifecycle Fields
    val status: TaskStatus = TaskStatus.ASSIGNED,
    val startedAt: Long? = null,
    val overdueNotificationSent: Boolean = false,
    val lastStatusChange: Long = System.currentTimeMillis()
) {
    enum class Priority(val value: Int, val displayName: String) {
        LOW(0, "Low"),
        MEDIUM(1, "Medium"),
        HIGH(2, "High"),
        URGENT(3, "Urgent");

        companion object {
            fun fromValue(value: Int): Priority {
                return entries.find { it.value == value } ?: MEDIUM
            }
        }
    }

    fun getFormattedDueDate(): String {
        return if (dueDate != null) {
            val dateFormat = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault())
            dateFormat.format(dueDate)
        } else {
            "No due date"
        }
    }

    fun getFormattedDueDateTime(): String {
        return if (dueDate != null) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            "${dateFormat.format(dueDate)} at ${timeFormat.format(dueDate)}"
        } else {
            "No due date"
        }
    }

    fun isOverdue(): Boolean {
        return dueDate != null && !isCompleted && dueDate.before(Date())
    }

    fun isDueSoon(): Boolean {
        if (dueDate == null || isCompleted) return false
        val now = Calendar.getInstance()
        val due = Calendar.getInstance().apply { time = dueDate }
        val hoursDiff = (due.timeInMillis - now.timeInMillis) / (1000 * 60 * 60)
        return hoursDiff in 0..24
    }

    fun getPriorityColor(): Int {
        return when (priority) {
            Priority.LOW -> "#4CAF50".toColorInt() // Green
            Priority.MEDIUM -> "#FF9800".toColorInt() // Orange
            Priority.HIGH -> "#F44336".toColorInt() // Red
            Priority.URGENT -> "#D32F2F".toColorInt() // Dark Red
        }
    }

    // ==================== DEPARTMENT TASK HELPER METHODS ====================

    /**
     * Check if this is a personal task
     */
    fun isPersonalTask(): Boolean = taskType == TaskType.PERSONAL

    /**
     * Check if this is a department task
     */
    fun isDepartmentTaskType(): Boolean = taskType == TaskType.DEPARTMENT || isDepartmentTask

    /**
     * Check if this task is assigned to all teachers (bulk task)
     */
    fun isAssignedToAll(): Boolean = isBulkTask || assignedTo == "ALL"

    /**
     * Check if task is assigned to specific user
     */
    fun isAssignedToUser(userId: String): Boolean {
        return when {
            isPersonalTask() -> true // Personal tasks are always "assigned" to creator
            isAssignedToAll() -> true // Bulk tasks visible to all
            assignedTo == userId -> true // Specifically assigned
            else -> false
        }
    }

    /**
     * Get task type badge text
     */
    fun getTaskTypeBadge(): String = when (taskType) {
        TaskType.PERSONAL -> "Personal"
        TaskType.DEPARTMENT -> if (isBulkTask) "Department (All)" else "Department"
    }

    /**
     * Get task visibility description
     */
    fun getVisibilityDescription(): String = when {
        isPersonalTask() -> "Private to you"
        isBulkTask -> "Assigned to all teachers"
        isDepartmentTaskType() -> "Assigned by $assignedByName"
        else -> "Unknown"
    }

    /**
     * Check if user can edit this task
     */
    fun canUserEdit(userId: String, isHOD: Boolean): Boolean = when {
        isPersonalTask() -> true // Can edit own tasks
        isDepartmentTaskType() && isHOD -> true // HOD can edit department tasks
        else -> false // Can't edit others' department tasks
    }

    /**
     * Check if user can delete this task
     */
    fun canUserDelete(userId: String, isHOD: Boolean): Boolean = when {
        isPersonalTask() -> true // Can delete own tasks
        isDepartmentTaskType() && isHOD && assignedBy == userId -> true // Creator can delete
        else -> false
    }

    // ==================== STEP 4: LIFECYCLE METHODS ====================

    /**
     * Get current lifecycle status
     */
    fun getLifecycleStatus(): TaskStatus {
        // Auto-calculate OVERDUE if past due and not completed
        return when {
            isCompleted -> TaskStatus.COMPLETED
            isOverdue() && status != TaskStatus.COMPLETED -> TaskStatus.OVERDUE
            else -> status
        }
    }

    /**
     * Check if task should send reminders
     * Rule: Reminders stop after completion
     */
    fun shouldSendReminders(): Boolean {
        return notificationsEnabled && !isCompleted && status != TaskStatus.COMPLETED
    }

    /**
     * Check if HOD should be notified about overdue status
     * Rule: Notify HOD when task becomes overdue
     */
    fun shouldNotifyHODOverdue(): Boolean {
        return isDepartmentTaskType() &&
               isOverdue() &&
               !isCompleted &&
               !overdueNotificationSent &&
               assignedBy.isNotEmpty()
    }

    /**
     * Get status color
     */
    fun getStatusColor(): Int {
        val actualStatus = getLifecycleStatus()
        return actualStatus.color.toColorInt()
    }

    /**
     * Get status badge text
     */
    fun getStatusBadge(): String = getLifecycleStatus().displayName

    /**
     * Check if task can transition to IN_PROGRESS
     */
    fun canStartTask(): Boolean {
        return status == TaskStatus.ASSIGNED && !isCompleted
    }

    /**
     * Check if task can be marked complete
     */
    fun canCompleteTask(): Boolean {
        return status.isActive() && !isCompleted
    }

    /**
     * Get lifecycle description for UI
     */
    fun getLifecycleDescription(): String = when (getLifecycleStatus()) {
        TaskStatus.ASSIGNED -> "Task assigned, not started yet"
        TaskStatus.PENDING -> "Task pending, not started yet"
        TaskStatus.IN_PROGRESS -> "Work in progress${startedAt?.let { " since ${formatDate(it)}" } ?: ""}"
        TaskStatus.COMPLETED -> "Completed${completedAt?.let { " on ${formatDate(it)}" } ?: ""}"
        TaskStatus.OVERDUE -> "Overdue by ${getOverdueDuration()}"
    }

    /**
     * Calculate how long task has been overdue
     */
    private fun getOverdueDuration(): String {
        if (dueDate == null) return "unknown"
        val now = System.currentTimeMillis()
        val overdueMillis = now - dueDate.time
        val days = overdueMillis / (1000 * 60 * 60 * 24)
        val hours = (overdueMillis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)

        return when {
            days > 0 -> "$days day${if (days > 1) "s" else ""}"
            hours > 0 -> "$hours hour${if (hours > 1) "s" else ""}"
            else -> "less than an hour"
        }
    }

    /**
     * Format timestamp for display
     */
    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    /**
     * Get next allowed status transitions
     */
    fun getAllowedTransitions(): List<TaskStatus> = when (status) {
        TaskStatus.ASSIGNED -> listOf(TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED)
        TaskStatus.PENDING -> listOf(TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED)
        TaskStatus.IN_PROGRESS -> listOf(TaskStatus.COMPLETED)
        TaskStatus.OVERDUE -> listOf(TaskStatus.IN_PROGRESS, TaskStatus.COMPLETED)
        TaskStatus.COMPLETED -> emptyList() // Terminal state
    }
}
