package com.example.teacherscheduler.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.teacherscheduler.notification.EnhancedNotificationHelper
import java.util.*

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "NotificationAction"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val notificationId = intent.getIntExtra(EnhancedNotificationHelper.EXTRA_NOTIFICATION_ID, 0)

        Log.d(TAG, "Action received: $action for notification $notificationId")

        // Acquire wake lock to ensure processing completes
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "TeacherScheduler:NotificationActionWakeLock"
        )

        try {
            wakeLock.acquire(30000) // Hold for 30 seconds max

            when (action) {
                "DISMISS" -> {
                    dismissNotification(context, notificationId)
                }
                "SNOOZE" -> {
                    snoozeNotification(context, intent, notificationId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling notification action: ${e.message}", e)
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }

    private fun dismissNotification(context: Context, notificationId: Int) {
        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.cancel(notificationId)
        Log.d(TAG, "Dismissed notification: $notificationId")
    }

    private fun snoozeNotification(context: Context, intent: Intent, notificationId: Int) {
        // Dismiss current notification
        dismissNotification(context, notificationId)

        // Get notification details
        val title = intent.getStringExtra(EnhancedNotificationHelper.EXTRA_TITLE) ?: "Reminder"
        val message = intent.getStringExtra(EnhancedNotificationHelper.EXTRA_MESSAGE) ?: ""
        val type = intent.getStringExtra(EnhancedNotificationHelper.EXTRA_TYPE) ?: EnhancedNotificationHelper.TYPE_CLASS
        val itemId = intent.getLongExtra(EnhancedNotificationHelper.EXTRA_ITEM_ID, 0L)

        // Cancel all other pending notifications for this item to avoid duplicates
        val notificationHelper = EnhancedNotificationHelper(context)
        notificationHelper.cancelNotifications(itemId, type)

        // Schedule new notification 2 minutes later
        val snoozeTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, 2)
        }.timeInMillis

        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EnhancedNotificationHelper.EXTRA_NOTIFICATION_ID, notificationId + 500000) // Snooze ID range
            putExtra(EnhancedNotificationHelper.EXTRA_TITLE, "$title (Snoozed)")
            putExtra(EnhancedNotificationHelper.EXTRA_MESSAGE, message)
            putExtra(EnhancedNotificationHelper.EXTRA_TYPE, type)
            putExtra(EnhancedNotificationHelper.EXTRA_ITEM_ID, itemId)
            putExtra(EnhancedNotificationHelper.EXTRA_REMINDER_MINUTES, 0) // Treat as exact time
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId + 500000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    snoozeTime,
                    pendingIntent
                )
            }
            Log.d(TAG, "Snoozed notification for 2 minutes")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule snooze alarm: ${e.message}", e)
        }
    }
}