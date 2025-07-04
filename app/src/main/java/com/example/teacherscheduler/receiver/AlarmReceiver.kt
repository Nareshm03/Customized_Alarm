package com.example.teacherscheduler.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.teacherscheduler.notification.NotificationHelper
import com.example.teacherscheduler.notification.EnhancedNotificationHelper
import java.util.Date

// Extension function to safely check if a nullable Boolean is true
private fun Boolean?.orFalse(): Boolean = this ?: false

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received with action: ${intent.action}")

        // Handle notification actions
        when (intent.action) {
            "SNOOZE" -> {
                handleSnoozeAction(context, intent)
                return
            }
            "MARK_DONE" -> {
                handleMarkDoneAction(context, intent)
                return
            }
        }

        // Use modern wake lock approach
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "TeacherScheduler:AlarmWakeLock"
        )

        try {
            wakeLock.acquire(60000) // Hold for 1 minute max

            val notificationId = intent.getIntExtra(EnhancedNotificationHelper.EXTRA_NOTIFICATION_ID, 0)
            val message = intent.getStringExtra(EnhancedNotificationHelper.EXTRA_MESSAGE) ?: ""
            val type = intent.getStringExtra(EnhancedNotificationHelper.EXTRA_TYPE) ?: EnhancedNotificationHelper.TYPE_CLASS
            val itemId = intent.getLongExtra(EnhancedNotificationHelper.EXTRA_ITEM_ID, 0L)
            val reminderMinutes = intent.getIntExtra(EnhancedNotificationHelper.EXTRA_REMINDER_MINUTES, 0)
            val eventStartTime = intent.getLongExtra(EnhancedNotificationHelper.EXTRA_EVENT_START_TIME, 0L)
            val subjectOrTitle = intent.getStringExtra(EnhancedNotificationHelper.EXTRA_SUBJECT_OR_TITLE) ?: "Event"

            // Calculate actual time remaining with more precision
            val now = System.currentTimeMillis()
            val millisRemaining = eventStartTime - now
            val actualMinutesRemaining = (millisRemaining / (1000 * 60)).toInt()
            val secondsRemainder = ((millisRemaining % (1000 * 60)) / 1000).toInt()
            
            // Log precise timing for debugging
            Log.d(TAG, "Event time: ${Date(eventStartTime)}, Current time: ${Date(now)}")
            Log.d(TAG, "Time remaining: $actualMinutesRemaining min, $secondsRemainder sec")
            
            // Skip if the notification is for a time that's already passed (more than 5 minutes ago)
            if (actualMinutesRemaining < -5 && !intent.getStringExtra(NotificationHelper.EXTRA_TITLE)?.contains("Snoozed").orFalse()) {
                Log.d(TAG, "Skipping notification as event started more than 5 minutes ago")
                return
            }
            
            // Don't cancel future notifications - let them fire at their scheduled times
            // Only cancel the current notification ID to avoid duplicates
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(notificationId)
            
            // Create dynamic title based on actual time remaining with strict timing
            val title = when {
                // Only show "STARTING NOW" if we're within 30 seconds of the start time
                actualMinutesRemaining == 0 && secondsRemainder <= 30 -> {
                    if (type == NotificationHelper.TYPE_CLASS) "🔔 CLASS STARTING NOW: $subjectOrTitle"
                    else "🔔 MEETING STARTING NOW: $subjectOrTitle"
                }
                // For the 0-minute notification that's not within 30 seconds
                actualMinutesRemaining == 0 -> {
                    if (type == NotificationHelper.TYPE_CLASS) "⏰ Class in less than 1 minute: $subjectOrTitle"
                    else "⏰ Meeting in less than 1 minute: $subjectOrTitle"
                }
                // For 1-minute notifications, be precise
                actualMinutesRemaining == 1 -> {
                    if (type == NotificationHelper.TYPE_CLASS) "⏰ Class in 1 minute: $subjectOrTitle"
                    else "⏰ Meeting in 1 minute: $subjectOrTitle"
                }
                // For 2-minute notifications, be precise
                actualMinutesRemaining == 2 -> {
                    if (type == NotificationHelper.TYPE_CLASS) "⏰ Class in 2 minutes: $subjectOrTitle"
                    else "⏰ Meeting in 2 minutes: $subjectOrTitle"
                }
                // For 5-minute notifications, be precise
                actualMinutesRemaining == 5 -> {
                    if (type == NotificationHelper.TYPE_CLASS) "⏰ Class in 5 minutes: $subjectOrTitle"
                    else "⏰ Meeting in 5 minutes: $subjectOrTitle"
                }
                // For all other times, use exact minutes without rounding
                else -> {
                    if (type == NotificationHelper.TYPE_CLASS) "⏰ Class in $actualMinutesRemaining minutes: $subjectOrTitle"
                    else "⏰ Meeting in $actualMinutesRemaining minutes: $subjectOrTitle"
                }
            }

            Log.d(TAG, "Showing notification: $title (scheduled for ${reminderMinutes}min, actual ${actualMinutesRemaining}min remaining)")

            val enhancedNotificationHelper = EnhancedNotificationHelper(context)
            val channelId = if (type == EnhancedNotificationHelper.TYPE_CLASS) {
                EnhancedNotificationHelper.CHANNEL_ID_CLASSES
            } else {
                EnhancedNotificationHelper.CHANNEL_ID_MEETINGS
            }
            
            enhancedNotificationHelper.showNotification(
                notificationId = notificationId,
                title = title,
                message = message,
                type = type,
                itemId = itemId,
                channelId = channelId
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error handling alarm: ${e.message}", e)
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }

    private fun handleSnoozeAction(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra(EnhancedNotificationHelper.EXTRA_ITEM_ID, 0L)
        val type = intent.getStringExtra(EnhancedNotificationHelper.EXTRA_TYPE) ?: EnhancedNotificationHelper.TYPE_CLASS
        
        Log.d(TAG, "Snoozing notification for $type with ID: $itemId")
        
        // Cancel current notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(itemId.toInt())
        
        // Schedule a new notification in 5 minutes
        val enhancedNotificationHelper = EnhancedNotificationHelper(context)
        val snoozeTime = System.currentTimeMillis() + (5 * 60 * 1000) // 5 minutes
        
        val title = if (type == EnhancedNotificationHelper.TYPE_CLASS) "Snoozed Class Reminder" else "Snoozed Meeting Reminder"
        val message = "Your snoozed reminder is ready!"
        
        enhancedNotificationHelper.showNotification(
            notificationId = (itemId + 50000).toInt(), // Different ID for snoozed notification
            title = title,
            message = message,
            type = type,
            itemId = itemId,
            channelId = EnhancedNotificationHelper.CHANNEL_ID_REMINDERS
        )
    }

    private fun handleMarkDoneAction(context: Context, intent: Intent) {
        val itemId = intent.getLongExtra(EnhancedNotificationHelper.EXTRA_ITEM_ID, 0L)
        val type = intent.getStringExtra(EnhancedNotificationHelper.EXTRA_TYPE) ?: EnhancedNotificationHelper.TYPE_CLASS
        
        Log.d(TAG, "Marking as done: $type with ID: $itemId")
        
        // Cancel current notification
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(itemId.toInt())
        
        // Mark as completed in database
        try {
            val repository = com.example.teacherscheduler.data.Repository(context)
            
            when (type) {
                EnhancedNotificationHelper.TYPE_CLASS -> {
                    // For classes, we'll add a completion entry or update status
                    Log.d(TAG, "Marking class $itemId as attended")
                    // Since classes are recurring, we mark today's session as attended
                    markClassAsAttended(context, itemId)
                }
                EnhancedNotificationHelper.TYPE_MEETING -> {
                    // For meetings, mark as completed
                    Log.d(TAG, "Marking meeting $itemId as completed")
                    markMeetingAsCompleted(context, itemId)
                }
            }
            
            // Show confirmation
            android.widget.Toast.makeText(context, "Marked as done!", android.widget.Toast.LENGTH_SHORT).show()
            
            // Send broadcast to refresh UI
            val refreshIntent = Intent("com.example.teacherscheduler.REFRESH_UI")
            refreshIntent.putExtra("item_type", type)
            refreshIntent.putExtra("item_id", itemId)
            context.sendBroadcast(refreshIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error marking as done", e)
            android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun markClassAsAttended(context: Context, classId: Long) {
        // Since we don't have an attendance table, we could use SharedPreferences
        // or create a simple mechanism to track attended classes for today
        val prefs = context.getSharedPreferences("attended_classes", Context.MODE_PRIVATE)
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val key = "${classId}_$today"
        
        prefs.edit().putBoolean(key, true).apply()
        Log.d(TAG, "Class $classId marked as attended for $today")
    }
    
    private fun markMeetingAsCompleted(context: Context, meetingId: Long) {
        // Use SharedPreferences to mark meeting as completed
        val prefs = context.getSharedPreferences("completed_meetings", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(meetingId.toString(), true).apply()
        Log.d(TAG, "Meeting $meetingId marked as completed")
    }
}