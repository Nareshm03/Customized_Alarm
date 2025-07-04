package com.example.teacherscheduler.util

import java.util.Calendar
import java.util.Date

/**
 * Extension function to convert a Date to a Calendar
 */
fun Date.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar
}

/**
 * Extension function to format a date as a string
 */
fun Date.formatTime(): String {
    val calendar = this.toCalendar()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val amPm = if (hour < 12) "AM" else "PM"
    val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    return String.format("%d:%02d %s", hour12, minute, amPm)
}

/**
 * Extension function to format a date range as a string
 */
fun formatTimeRange(start: Date, end: Date): String {
    return "${start.formatTime()} - ${end.formatTime()}"
}

/**
 * Extension function to get day of week as an integer (1-7, where 1 is Sunday)
 */
fun Date.getDayOfWeek(): Int {
    val calendar = this.toCalendar()
    return calendar.get(Calendar.DAY_OF_WEEK)
}

/**
 * Extension function to check if a date is today
 */
fun Date.isToday(): Boolean {
    val today = Calendar.getInstance()
    val thisDate = this.toCalendar()
    
    return today.get(Calendar.YEAR) == thisDate.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == thisDate.get(Calendar.DAY_OF_YEAR)
}

/**
 * Extension function to check if a date is tomorrow
 */
fun Date.isTomorrow(): Boolean {
    val tomorrow = Calendar.getInstance()
    tomorrow.add(Calendar.DAY_OF_YEAR, 1)
    val thisDate = this.toCalendar()
    
    return tomorrow.get(Calendar.YEAR) == thisDate.get(Calendar.YEAR) &&
            tomorrow.get(Calendar.DAY_OF_YEAR) == thisDate.get(Calendar.DAY_OF_YEAR)
}