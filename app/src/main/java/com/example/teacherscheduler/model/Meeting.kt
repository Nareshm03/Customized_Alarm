// Meeting.kt
package com.example.teacherscheduler.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "meetings")
data class Meeting(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val title: String,
    val withWhom: String,
    val with: String = "", // Alias for Firestore compatibility
    val location: String = "",
    val notes: String = "",
    val startDate: Date,
    val endDate: Date,
    val date: Date = Date(), // Alias for Firestore compatibility
    val startTime: Date,
    val endTime: Date,
    val isActive: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val reminderMinutes: Int = 15,
    val semesterId: Long = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSyncTimestamp: Long = 0
) {
    fun getFormattedDateTime(): String {
        val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return "${dateFormat.format(startDate)}\n${timeFormat.format(startTime)} - ${timeFormat.format(endTime)}"
    }

    fun getFormattedDate(): String {
        val dateFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        return dateFormat.format(startDate)
    }

    fun getFormattedTime(): String {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return "${timeFormat.format(startTime)} - ${timeFormat.format(endTime)}"
    }
    
    // Helper methods for enhanced dashboard
    fun getStartDateTime(): Long {
        // Combine startDate and startTime into a single timestamp
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        val timeCalendar = Calendar.getInstance()
        timeCalendar.time = startTime
        
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // Debug logging
        val timeZone = java.util.TimeZone.getDefault()
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", java.util.Locale.getDefault())
        dateFormat.timeZone = timeZone
        
        android.util.Log.d("MeetingTimeDebug", "=== MEETING START TIME CALCULATION ===")
        android.util.Log.d("MeetingTimeDebug", "Title: $title")
        android.util.Log.d("MeetingTimeDebug", "Device timezone: ${timeZone.displayName} (${timeZone.id})")
        android.util.Log.d("MeetingTimeDebug", "Start date: ${dateFormat.format(startDate)}")
        android.util.Log.d("MeetingTimeDebug", "Start time: ${dateFormat.format(startTime)}")
        android.util.Log.d("MeetingTimeDebug", "Combined result: ${dateFormat.format(java.util.Date(calendar.timeInMillis))}")
        
        return calendar.timeInMillis
    }
    
    fun getEndDateTime(): Long {
        // Combine endDate and endTime into a single timestamp
        val calendar = Calendar.getInstance()
        calendar.time = endDate
        
        val timeCalendar = Calendar.getInstance()
        timeCalendar.time = endTime
        
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        return calendar.timeInMillis
    }
    
    fun getDurationMinutes(): Int {
        return ((getEndDateTime() - getStartDateTime()) / (1000 * 60)).toInt()
    }
}