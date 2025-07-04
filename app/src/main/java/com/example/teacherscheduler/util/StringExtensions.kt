package com.example.teacherscheduler.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Extension function to convert a time string (HH:mm) to a Calendar
 */
fun String.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    try {
        val parts = this.split(":")
        if (parts.size == 2) {
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
        }
    } catch (e: Exception) {
        // If parsing fails, just return the current time
    }
    return calendar
}

/**
 * Extension function to convert a time string (h:mm a) to a Calendar
 * Example: "3:00 PM" -> Calendar with hour=15, minute=0
 */
fun String.toCalendarFromAmPm(): Calendar {
    val calendar = Calendar.getInstance()
    try {
        val format = SimpleDateFormat("h:mm a", Locale.US)
        val date = format.parse(this)
        if (date != null) {
            calendar.time = date
        }
    } catch (e: Exception) {
        // If parsing fails, just return the current time
    }
    return calendar
}

/**
 * Format a time string (HH:mm or h:mm a) to a readable format
 */
fun String.formatTime(): String {
    try {
        // Try to parse as 24-hour format first
        val parts = this.split(":")
        if (parts.size == 2) {
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            val amPm = if (hour < 12) "AM" else "PM"
            val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            return String.format("%d:%02d %s", hour12, minute, amPm)
        }
        
        // If not in 24-hour format, return as is (assuming it's already formatted)
        return this
    } catch (e: Exception) {
        // If parsing fails, return the original string
        return this
    }
}

/**
 * Format a time range from two string times
 */
fun formatTimeRange(start: String, end: String): String {
    return "${start.formatTime()} - ${end.formatTime()}"
}