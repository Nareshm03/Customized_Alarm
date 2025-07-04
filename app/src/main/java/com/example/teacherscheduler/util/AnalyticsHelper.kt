package com.example.teacherscheduler.util

import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

data class TeachingStatistics(
    val totalClasses: Int,
    val totalMeetings: Int,
    val totalTeachingHours: Double,
    val averageClassDuration: Double,
    val mostActiveDay: String,
    val mostActiveTimeSlot: String,
    val departmentBreakdown: Map<String, Int>,
    val weeklyScheduleLoad: Map<String, Int>,
    val upcomingClassesThisWeek: Int,
    val upcomingMeetingsThisWeek: Int,
    val completedClassesThisMonth: Int,
    val completedMeetingsThisMonth: Int
)

data class ProductivityInsights(
    val averageClassesPerDay: Double,
    val averageMeetingsPerDay: Double,
    val busiestDayOfWeek: String,
    val quietestDayOfWeek: String,
    val peakTeachingHours: List<String>,
    val recommendedBreakTimes: List<String>,
    val workloadTrend: String // "increasing", "decreasing", "stable"
)

class AnalyticsHelper(private val repository: Repository) {

    suspend fun getTeachingStatistics(
        startDate: Long = getStartOfMonth(),
        endDate: Long = getEndOfMonth()
    ): TeachingStatistics = withContext(Dispatchers.IO) {
        
        val classes = repository.getClassesForDateRange(startDate, endDate)
        val meetings = repository.getMeetingsForDateRange(startDate, endDate)
        
        val totalTeachingHours = classes.sumOf { 
            (it.getEndDateTime() - it.getStartDateTime()) / (1000.0 * 60 * 60) 
        }
        
        val averageClassDuration = if (classes.isNotEmpty()) {
            classes.sumOf { (it.getEndDateTime() - it.getStartDateTime()) / (1000.0 * 60) } / classes.size
        } else 0.0
        
        val dayOfWeekCounts = classes.groupBy { 
            getDayOfWeek(it.getStartDateTime()) 
        }.mapValues { it.value.size }
        
        val mostActiveDay = dayOfWeekCounts.maxByOrNull { it.value }?.key ?: "No data"
        
        val timeSlotCounts = classes.groupBy { 
            getTimeSlot(it.getStartDateTime()) 
        }.mapValues { it.value.size }
        
        val mostActiveTimeSlot = timeSlotCounts.maxByOrNull { it.value }?.key ?: "No data"
        
        val departmentBreakdown = classes.groupBy { it.department }.mapValues { it.value.size }
        
        val weeklyScheduleLoad = getWeeklyScheduleLoad(classes)
        
        val now = System.currentTimeMillis()
        val weekStart = getStartOfWeek(now)
        val weekEnd = getEndOfWeek(now)
        val monthStart = getStartOfMonth(now)
        val monthEnd = getEndOfMonth(now)
        
        val upcomingClassesThisWeek = repository.getClassesForDateRange(now, weekEnd).size
        val upcomingMeetingsThisWeek = repository.getMeetingsForDateRange(now, weekEnd).size
        val completedClassesThisMonth = repository.getClassesForDateRange(monthStart, now).size
        val completedMeetingsThisMonth = repository.getMeetingsForDateRange(monthStart, now).size
        
        TeachingStatistics(
            totalClasses = classes.size,
            totalMeetings = meetings.size,
            totalTeachingHours = totalTeachingHours,
            averageClassDuration = averageClassDuration,
            mostActiveDay = mostActiveDay,
            mostActiveTimeSlot = mostActiveTimeSlot,
            departmentBreakdown = departmentBreakdown,
            weeklyScheduleLoad = weeklyScheduleLoad,
            upcomingClassesThisWeek = upcomingClassesThisWeek,
            upcomingMeetingsThisWeek = upcomingMeetingsThisWeek,
            completedClassesThisMonth = completedClassesThisMonth,
            completedMeetingsThisMonth = completedMeetingsThisMonth
        )
    }

    suspend fun getProductivityInsights(): ProductivityInsights = withContext(Dispatchers.IO) {
        val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        val now = System.currentTimeMillis()
        
        val classes = repository.getClassesForDateRange(thirtyDaysAgo, now)
        val meetings = repository.getMeetingsForDateRange(thirtyDaysAgo, now)
        
        val averageClassesPerDay = classes.size / 30.0
        val averageMeetingsPerDay = meetings.size / 30.0
        
        val dayOfWeekCounts = classes.groupBy { getDayOfWeek(it.getStartDateTime()) }
            .mapValues { it.value.size }
        
        val busiestDay = dayOfWeekCounts.maxByOrNull { it.value }?.key ?: "Monday"
        val quietestDay = dayOfWeekCounts.minByOrNull { it.value }?.key ?: "Sunday"
        
        val hourCounts = classes.groupBy { getHourOfDay(it.getStartDateTime()) }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
        
        val peakHours = hourCounts.take(3).map { "${it.first}:00" }
        
        val recommendedBreaks = findRecommendedBreakTimes(classes)
        
        val workloadTrend = calculateWorkloadTrend(classes)
        
        ProductivityInsights(
            averageClassesPerDay = averageClassesPerDay,
            averageMeetingsPerDay = averageMeetingsPerDay,
            busiestDayOfWeek = busiestDay,
            quietestDayOfWeek = quietestDay,
            peakTeachingHours = peakHours,
            recommendedBreakTimes = recommendedBreaks,
            workloadTrend = workloadTrend
        )
    }

    private fun getDayOfWeek(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Unknown"
        }
    }

    private fun getTimeSlot(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        return when (hour) {
            in 6..8 -> "Early Morning (6-8 AM)"
            in 9..11 -> "Morning (9-11 AM)"
            in 12..14 -> "Afternoon (12-2 PM)"
            in 15..17 -> "Late Afternoon (3-5 PM)"
            in 18..20 -> "Evening (6-8 PM)"
            else -> "Other"
        }
    }

    private fun getHourOfDay(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.HOUR_OF_DAY)
    }

    private fun getWeeklyScheduleLoad(classes: List<Class>): Map<String, Int> {
        return classes.groupBy { getDayOfWeek(it.getStartDateTime()) }
            .mapValues { it.value.size }
    }

    private fun findRecommendedBreakTimes(classes: List<Class>): List<String> {
        // Analyze gaps between classes to recommend break times
        val sortedClasses = classes.sortedBy { it.getStartDateTime() }
        val gaps = mutableListOf<String>()
        
        for (i in 0 until sortedClasses.size - 1) {
            val currentEnd = sortedClasses[i].getEndDateTime()
            val nextStart = sortedClasses[i + 1].getStartDateTime()
            val gapMinutes = (nextStart - currentEnd) / (1000 * 60)
            
            if (gapMinutes >= 30) { // 30+ minute gap
                val breakTime = Calendar.getInstance()
                breakTime.timeInMillis = currentEnd + (gapMinutes / 2 * 60 * 1000) // Middle of gap
                val timeStr = String.format("%02d:%02d", 
                    breakTime.get(Calendar.HOUR_OF_DAY),
                    breakTime.get(Calendar.MINUTE))
                gaps.add(timeStr)
            }
        }
        
        return gaps.take(3) // Top 3 recommended break times
    }

    private fun calculateWorkloadTrend(classes: List<Class>): String {
        if (classes.size < 14) return "stable" // Need at least 2 weeks of data
        
        val sortedClasses = classes.sortedBy { it.getStartDateTime() }
        val midPoint = sortedClasses.size / 2
        
        val firstHalfCount = sortedClasses.take(midPoint).size
        val secondHalfCount = sortedClasses.drop(midPoint).size
        
        return when {
            secondHalfCount > firstHalfCount * 1.2 -> "increasing"
            secondHalfCount < firstHalfCount * 0.8 -> "decreasing"
            else -> "stable"
        }
    }

    private fun getStartOfWeek(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfWeek(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    private fun getStartOfMonth(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfMonth(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}