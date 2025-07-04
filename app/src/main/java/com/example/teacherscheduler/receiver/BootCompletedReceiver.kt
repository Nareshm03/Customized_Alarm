package com.example.teacherscheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.notification.EnhancedNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON") {
            
            Log.d(TAG, "Device boot completed, rescheduling alarms")
            
            CoroutineScope(Dispatchers.IO).launch {
                rescheduleAllAlarms(context)
            }
        }
    }

    private suspend fun rescheduleAllAlarms(context: Context) {
        try {
            val repository = Repository(context)
            val notificationHelper = EnhancedNotificationHelper(context)

            // Reschedule all active classes
            val classes = repository.getAllActiveClassesList()
            classes.forEach { classItem ->
                if (classItem.notificationsEnabled) {
                    notificationHelper.scheduleClassNotifications(classItem)
                }
            }

            // Reschedule all active meetings
            val meetings = repository.getAllActiveMeetingsList()
            meetings.forEach { meeting ->
                if (meeting.notificationsEnabled) {
                    notificationHelper.scheduleMeetingNotifications(meeting)
                }
            }

            Log.d(TAG, "Rescheduled alarms for ${classes.size} classes and ${meetings.size} meetings")

        } catch (e: Exception) {
            Log.e(TAG, "Error rescheduling alarms: ${e.message}", e)
        }
    }
}