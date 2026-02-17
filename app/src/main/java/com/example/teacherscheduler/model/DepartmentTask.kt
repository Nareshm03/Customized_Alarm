package com.example.teacherscheduler.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * DepartmentTask - Tasks assigned to department members
 *
 * Enables HOD to:
 * - Assign tasks to specific teachers or all teachers
 * - Bulk task assignment
 * - Track task completion
 * - Monitor department workload
 */
@Entity(
    tableName = "department_tasks",
    foreignKeys = [
        ForeignKey(
            entity = Department::class,
            parentColumns = ["id"],
            childColumns = ["departmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["departmentId"]),
        Index(value = ["assignedTo"]),
        Index(value = ["assignedBy"]),
        Index(value = ["status"]),
        Index(value = ["dueDate"]),
        Index(value = ["isActive"])
    ]
)
data class DepartmentTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Department reference
    val departmentId: Long,

    // Task details
    val title: String,
    val description: String = "",
    val category: TaskCategory = TaskCategory.GENERAL,
    val priority: TaskPriority = TaskPriority.MEDIUM,

    // Assignment
    val assignedBy: String,                  // User ID of assigner (usually HOD)
    val assignedByName: String = "",         // Cached assigner name
    val assignedTo: String,                  // User ID of assignee ("ALL" for all teachers)
    val assignedToName: String = "",         // Cached assignee name
    val isGroupTask: Boolean = false,        // True if assigned to all teachers

    // Dates
    val assignedDate: Long = System.currentTimeMillis(),
    val dueDate: Long,                       // Due date/time
    val completedDate: Long? = null,         // When task was completed

    // Status
    val status: TaskStatus = TaskStatus.ASSIGNED,
    val isActive: Boolean = true,

    // Completion details
    val completionNotes: String = "",        // Notes added on completion
    val attachmentUrl: String = "",          // Optional file attachment

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * No-arg constructor for Room
     */
    constructor() : this(
        id = 0,
        departmentId = 0,
        title = "",
        description = "",
        category = TaskCategory.GENERAL,
        priority = TaskPriority.MEDIUM,
        assignedBy = "",
        assignedByName = "",
        assignedTo = "",
        assignedToName = "",
        isGroupTask = false,
        assignedDate = System.currentTimeMillis(),
        dueDate = System.currentTimeMillis(),
        completedDate = null,
        status = TaskStatus.ASSIGNED,
        isActive = true,
        completionNotes = "",
        attachmentUrl = "",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    /**
     * Check if task is overdue
     */
    fun isOverdue(): Boolean {
        return status != TaskStatus.COMPLETED && dueDate < System.currentTimeMillis()
    }

    /**
     * Check if task is assigned to all teachers
     */
    fun isAssignedToAll(): Boolean = assignedTo == "ALL" || isGroupTask

    /**
     * Get status color
     */
    fun getStatusColor(): String = status.color

    /**
     * Get priority color
     */
    fun getPriorityColor(): String = when (priority) {
        TaskPriority.URGENT -> "#F44336"    // Red
        TaskPriority.HIGH -> "#FF9800"      // Orange
        TaskPriority.MEDIUM -> "#FFC107"    // Amber
        TaskPriority.LOW -> "#4CAF50"       // Green
    }
}

/**
 * Task categories
 */
enum class TaskCategory {
    GENERAL,            // General tasks
    EXAM_DUTY,          // Exam invigilation
    REPORT_SUBMISSION,  // Report/document submission
    MEETING,            // Meeting attendance
    ADMINISTRATIVE,     // Administrative work
    ACADEMIC,           // Academic tasks
    EVENT,              // Event-related tasks
    OTHER               // Other tasks
}

/**
 * Task priority
 */
enum class TaskPriority {
    URGENT,             // Immediate action required
    HIGH,               // High priority
    MEDIUM,             // Normal priority
    LOW                 // Low priority
}
