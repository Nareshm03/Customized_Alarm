package com.example.teacherscheduler.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

class CompletionStatusHelper(private val context: Context) {
    
    companion object {
        private const val PREF_ATTENDED_CLASSES = "attended_classes"
        private const val PREF_COMPLETED_MEETINGS = "completed_meetings"
    }
    
    fun isClassAttendedToday(classId: Long): Boolean {
        val prefs = context.getSharedPreferences(PREF_ATTENDED_CLASSES, Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val key = "${classId}_$today"
        return prefs.getBoolean(key, false)
    }
    
    fun isMeetingCompleted(meetingId: Long): Boolean {
        val prefs = context.getSharedPreferences(PREF_COMPLETED_MEETINGS, Context.MODE_PRIVATE)
        return prefs.getBoolean(meetingId.toString(), false)
    }
    
    fun markClassAsAttended(classId: Long) {
        val prefs = context.getSharedPreferences(PREF_ATTENDED_CLASSES, Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val key = "${classId}_$today"
        prefs.edit().putBoolean(key, true).apply()
    }
    
    fun markMeetingAsCompleted(meetingId: Long) {
        val prefs = context.getSharedPreferences(PREF_COMPLETED_MEETINGS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(meetingId.toString(), true).apply()
    }
    
    fun unmarkClassAsAttended(classId: Long) {
        val prefs = context.getSharedPreferences(PREF_ATTENDED_CLASSES, Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val key = "${classId}_$today"
        prefs.edit().remove(key).apply()
    }
    
    fun unmarkMeetingAsCompleted(meetingId: Long) {
        val prefs = context.getSharedPreferences(PREF_COMPLETED_MEETINGS, Context.MODE_PRIVATE)
        prefs.edit().remove(meetingId.toString()).apply()
    }
}