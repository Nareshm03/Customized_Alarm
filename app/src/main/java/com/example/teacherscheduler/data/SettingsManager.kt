package com.example.teacherscheduler.data

import android.content.Context
import android.content.SharedPreferences
import com.example.teacherscheduler.model.AppSettings

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "teacher_scheduler_settings", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_CLASS_NOTIFICATIONS = "class_notifications_enabled"
        private const val KEY_MEETING_NOTIFICATIONS = "meeting_notifications_enabled"
        private const val KEY_GLOBAL_NOTIFICATIONS = "global_notifications_enabled"
        private const val KEY_AUTO_SYNC = "auto_sync"
        private const val KEY_REMINDER_TIME = "reminder_time"
        private const val KEY_REMINDER_INTERVALS = "reminder_intervals"
        private const val KEY_NOTIFICATION_SOUND = "notification_sound"
        private const val KEY_NOTIFICATION_VIBRATION = "notification_vibration"
        private const val KEY_ADVANCE_NOTIFICATION = "advance_notification"
        private const val KEY_THEME = "theme"
        private const val KEY_CURRENT_SEMESTER_ID = "current_semester_id"
        private const val KEY_LAST_SYNC = "last_sync_timestamp"
    }
    
    fun saveSettings(settings: AppSettings) {
        prefs.edit().apply {
            putBoolean(KEY_CLASS_NOTIFICATIONS, settings.classNotificationsEnabled)
            putBoolean(KEY_MEETING_NOTIFICATIONS, settings.meetingNotificationsEnabled)
            putInt(KEY_REMINDER_TIME, settings.reminderTime)
            putBoolean(KEY_AUTO_SYNC, settings.autoSync)
            putBoolean(KEY_NOTIFICATION_SOUND, settings.notificationSound)
            putBoolean(KEY_NOTIFICATION_VIBRATION, settings.notificationVibration)
            putBoolean(KEY_ADVANCE_NOTIFICATION, settings.advanceNotification)
            putInt(KEY_THEME, settings.theme)
            putLong(KEY_CURRENT_SEMESTER_ID, settings.currentSemesterId)
            putLong(KEY_LAST_SYNC, settings.lastSyncTimestamp)
            apply()
        }
    }
    
    fun getSettings(): AppSettings {
        return AppSettings(
            classNotificationsEnabled = prefs.getBoolean(KEY_CLASS_NOTIFICATIONS, true),
            meetingNotificationsEnabled = prefs.getBoolean(KEY_MEETING_NOTIFICATIONS, true),
            reminderTime = prefs.getInt(KEY_REMINDER_TIME, 15),
            autoSync = prefs.getBoolean(KEY_AUTO_SYNC, false),
            notificationSound = prefs.getBoolean(KEY_NOTIFICATION_SOUND, true),
            notificationVibration = prefs.getBoolean(KEY_NOTIFICATION_VIBRATION, true),
            advanceNotification = prefs.getBoolean(KEY_ADVANCE_NOTIFICATION, true),
            theme = prefs.getInt(KEY_THEME, 0),
            currentSemesterId = prefs.getLong(KEY_CURRENT_SEMESTER_ID, 0),
            lastSyncTimestamp = prefs.getLong(KEY_LAST_SYNC, 0)
        )
    }
    
    fun updateLastSyncTime(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC, timestamp).apply()
    }
    
    // New enhanced methods for notification settings
    fun areNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_GLOBAL_NOTIFICATIONS, true)
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GLOBAL_NOTIFICATIONS, enabled).apply()
    }
    
    fun areClassNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_CLASS_NOTIFICATIONS, true)
    }
    
    fun setClassNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CLASS_NOTIFICATIONS, enabled).apply()
    }
    
    fun areMeetingNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_MEETING_NOTIFICATIONS, true)
    }
    
    fun setMeetingNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MEETING_NOTIFICATIONS, enabled).apply()
    }
    
    fun isSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_SOUND, true)
    }
    
    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATION_SOUND, enabled).apply()
    }
    
    fun getReminderIntervals(): List<Int> {
        val intervalsString = prefs.getString(KEY_REMINDER_INTERVALS, "0,1,2,3,5,15,30,45,60")
        return intervalsString?.split(",")?.mapNotNull { it.toIntOrNull() } ?: listOf(0, 1, 2, 3, 5, 15, 30, 45, 60)
    }
    
    fun setReminderIntervals(intervals: List<Int>) {
        val intervalsString = intervals.joinToString(",")
        prefs.edit().putString(KEY_REMINDER_INTERVALS, intervalsString).apply()
    }

}