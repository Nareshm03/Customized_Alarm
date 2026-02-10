package com.example.teacherscheduler.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(
    tableName = "todos",
    indices = [
        Index(value = ["isCompleted", "dueDate"]),
        Index(value = ["priority"]),
        Index(value = ["category"]),
        Index(value = ["lastSyncTimestamp"])
    ]
)
data class ToDo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val description: String = "",
    val category: String = "", // e.g., "Grading", "Lesson Planning", "Administrative", etc.
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
    val lastSyncTimestamp: Long = 0
) {
    enum class Priority(val value: Int, val displayName: String) {
        LOW(0, "Low"),
        MEDIUM(1, "Medium"),
        HIGH(2, "High"),
        URGENT(3, "Urgent");

        companion object {
            fun fromValue(value: Int): Priority {
                return values().find { it.value == value } ?: MEDIUM
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
            Priority.LOW -> android.graphics.Color.parseColor("#4CAF50") // Green
            Priority.MEDIUM -> android.graphics.Color.parseColor("#FF9800") // Orange
            Priority.HIGH -> android.graphics.Color.parseColor("#F44336") // Red
            Priority.URGENT -> android.graphics.Color.parseColor("#D32F2F") // Dark Red
        }
    }
}

