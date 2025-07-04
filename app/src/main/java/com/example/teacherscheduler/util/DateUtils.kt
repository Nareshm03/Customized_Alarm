package com.example.teacherscheduler.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Formats a date in a user-friendly format
 */
fun formatDate(date: Date): String {
    val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    return dateFormat.format(date)
}

/**
 * Formats a date in a short format
 */
fun formatShortDate(date: Date): String {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return dateFormat.format(date)
}

/**
 * Formats a time in a user-friendly format using SimpleDateFormat
 */
fun formatTime(time: Date): String {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    return timeFormat.format(time)
}