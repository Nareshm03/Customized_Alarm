package com.example.teacherscheduler.util

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationManagerCompat

object NotificationDebugHelper {
    private const val TAG = "NotificationDebug"

    fun checkNotificationSettings(context: Context) {
        Log.d(TAG, "=== NOTIFICATION DEBUG INFO ===")

        // Check if notifications are enabled
        val areNotificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        Log.d(TAG, "Notifications enabled: $areNotificationsEnabled")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Check individual channels
            val classChannel = notificationManager.getNotificationChannel("teacher_scheduler_classes")
            val meetingChannel = notificationManager.getNotificationChannel("teacher_scheduler_meetings")

            Log.d(TAG, "Class channel importance: ${classChannel?.importance}")
            Log.d(TAG, "Meeting channel importance: ${meetingChannel?.importance}")
        }

        // Check exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            val canScheduleExact = alarmManager.canScheduleExactAlarms()
            Log.d(TAG, "Can schedule exact alarms: $canScheduleExact")
        }

        Log.d(TAG, "=== END DEBUG INFO ===")
    }
}