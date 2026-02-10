package com.example.teacherscheduler.util

import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import java.util.*

object QuickStats {
    
    data class DailyStats(
        val totalClasses: Int,
        val totalMeetings: Int,
        val totalHours: Float,
        val nextEvent: String
    )
    
    fun calculateDailyStats(classes: List<Class>, meetings: List<Meeting>): DailyStats {
        val now = System.currentTimeMillis()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis
        
        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis
        
        val todayClasses = classes.filter { 
            it.getStartDateTime() in todayStart..todayEnd 
        }
        
        val todayMeetings = meetings.filter { 
            it.getStartDateTime() in todayStart..todayEnd 
        }
        
        val totalMinutes = todayClasses.sumOf { it.getDurationMinutes() } + 
                          todayMeetings.sumOf { it.getDurationMinutes() }
        
        val nextEvent = findNextEvent(classes, meetings, now)
        
        return DailyStats(
            totalClasses = todayClasses.size,
            totalMeetings = todayMeetings.size,
            totalHours = totalMinutes / 60f,
            nextEvent = nextEvent
        )
    }
    
    private fun findNextEvent(classes: List<Class>, meetings: List<Meeting>, now: Long): String {
        val upcomingClasses = classes.filter { it.getStartDateTime() > now }
            .sortedBy { it.getStartDateTime() }
        
        val upcomingMeetings = meetings.filter { it.getStartDateTime() > now }
            .sortedBy { it.getStartDateTime() }
        
        val nextClass = upcomingClasses.firstOrNull()
        val nextMeeting = upcomingMeetings.firstOrNull()
        
        return when {
            nextClass == null && nextMeeting == null -> "No upcoming events"
            nextClass == null -> "Meeting: ${nextMeeting?.title}"
            nextMeeting == null -> "Class: ${nextClass.subject}"
            nextClass.getStartDateTime() < nextMeeting.getStartDateTime() -> "Class: ${nextClass.subject}"
            else -> "Meeting: ${nextMeeting.title}"
        }
    }
}
