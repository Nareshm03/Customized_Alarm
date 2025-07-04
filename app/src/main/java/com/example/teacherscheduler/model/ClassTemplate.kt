package com.example.teacherscheduler.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "class_templates")
data class ClassTemplate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val subject: String,
    val department: String,
    val roomNumber: String,
    val duration: Int, // in minutes
    val defaultNotificationsEnabled: Boolean = true,
    val defaultReminderIntervals: String = "15,30,60", // comma-separated minutes
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = 0
) {
    fun getReminderIntervals(): List<Int> {
        return defaultReminderIntervals.split(",").mapNotNull { it.toIntOrNull() }
    }
    
    fun createClass(startDateTime: Long): Class {
        val endDateTime = startDateTime + (duration * 60 * 1000)
        
        // Convert timestamps to Date objects
        val startDate = Date(startDateTime)
        val endDate = Date(startDateTime) // Same day
        val startTime = Date(startDateTime)
        val endTime = Date(endDateTime)
        
        return Class(
            subject = subject,
            department = department,
            roomNumber = roomNumber,
            startDate = startDate,
            endDate = endDate,
            startTime = startTime,
            endTime = endTime,
            notificationsEnabled = defaultNotificationsEnabled,
            isRecurring = false
        )
    }
}