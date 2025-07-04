package com.example.teacherscheduler.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.teacherscheduler.MainActivity
import com.example.teacherscheduler.R
import com.example.teacherscheduler.data.SettingsManager
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import com.example.teacherscheduler.receiver.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.*

class EnhancedNotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID_CLASSES = "teacher_scheduler_classes_v2"
        const val CHANNEL_ID_MEETINGS = "teacher_scheduler_meetings_v2"
        const val CHANNEL_ID_REMINDERS = "teacher_scheduler_reminders_v2"

        // Notification types
        const val TYPE_CLASS = "class"
        const val TYPE_MEETING = "meeting"
        const val TYPE_REMINDER = "reminder"

        // Intent extras
        const val EXTRA_NOTIFICATION_ID = "notification_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_TYPE = "type"
        const val EXTRA_ITEM_ID = "item_id"
        const val EXTRA_REMINDER_MINUTES = "reminder_minutes"
        const val EXTRA_EVENT_START_TIME = "event_start_time"
        const val EXTRA_SUBJECT_OR_TITLE = "subject_or_title"

        private const val TAG = "EnhancedNotificationHelper"
    }

    private val notificationManager = NotificationManagerCompat.from(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val settingsManager = SettingsManager(context)
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = getAlarmSound()
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                .setLegacyStreamType(android.media.AudioManager.STREAM_ALARM)
                .build()

            // Classes channel
            val classChannel = NotificationChannel(
                CHANNEL_ID_CLASSES,
                "Class Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming classes"
                enableLights(true)
                enableVibration(true)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // Meetings channel
            val meetingChannel = NotificationChannel(
                CHANNEL_ID_MEETINGS,
                "Meeting Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming meetings"
                enableLights(true)
                enableVibration(true)
                setSound(soundUri, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // Reminders channel
            val reminderChannel = NotificationChannel(
                CHANNEL_ID_REMINDERS,
                "Reminder Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General reminder notifications"
                enableLights(true)
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(classChannel)
            manager.createNotificationChannel(meetingChannel)
            manager.createNotificationChannel(reminderChannel)
        }
    }

    private fun getAlarmSound(): android.net.Uri {
        // Try different sound types in order of preference
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        
        val soundUri = alarmUri ?: notificationUri ?: ringtoneUri
        
        Log.d(TAG, "Enhanced - Alarm sound URI: $soundUri")
        Log.d(TAG, "Enhanced - Alarm URI available: ${alarmUri != null}")
        Log.d(TAG, "Enhanced - Notification URI available: ${notificationUri != null}")
        
        return soundUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }

    fun checkSoundSettings() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        Log.d(TAG, "=== SOUND DEBUG INFO ===")
        val notificationsEnabled = notificationManager.areNotificationsEnabled()
        Log.d(TAG, "Notifications enabled: $notificationsEnabled")
        
        val dndPolicy = notificationManager.currentInterruptionFilter
        Log.d(TAG, "Do Not Disturb policy: $dndPolicy")
        Log.d(TAG, "DND Active: ${dndPolicy != NotificationManager.INTERRUPTION_FILTER_ALL}")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val classChannel = notificationManager.getNotificationChannel(CHANNEL_ID_CLASSES)
            val meetingChannel = notificationManager.getNotificationChannel(CHANNEL_ID_MEETINGS)
            
            Log.d(TAG, "Class channel importance: ${classChannel?.importance}")
            Log.d(TAG, "Class channel sound: ${classChannel?.sound}")
            Log.d(TAG, "Meeting channel importance: ${meetingChannel?.importance}")
            Log.d(TAG, "Meeting channel sound: ${meetingChannel?.sound}")
        }
    }

    fun scheduleClassNotifications(classItem: Class) {
        val globalEnabled = settingsManager.areNotificationsEnabled()
        val classEnabled = settingsManager.areClassNotificationsEnabled()
        
        Log.d(TAG, "============= SCHEDULING CLASS NOTIFICATIONS =============")
        Log.d(TAG, "Class: ${classItem.subject}")
        Log.d(TAG, "Global notifications enabled: $globalEnabled")
        Log.d(TAG, "Class notifications enabled: $classEnabled")
        Log.d(TAG, "Class notifications enabled on item: ${classItem.notificationsEnabled}")
        
        if (!globalEnabled || !classEnabled || !classItem.notificationsEnabled) {
            Log.d(TAG, "Notifications disabled - skipping scheduling")
            return
        }

        val allReminderIntervals = settingsManager.getReminderIntervals()
        Log.d(TAG, "All reminder intervals: $allReminderIntervals")
        
        val startDateTime = classItem.getStartDateTime()
        val currentTime = System.currentTimeMillis()
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timeUntilClass = (startDateTime - currentTime) / (60 * 1000) // minutes
        
        Log.d(TAG, "Class start time: ${timeFormat.format(Date(startDateTime))}")
        Log.d(TAG, "Current time: ${timeFormat.format(Date(currentTime))}")
        Log.d(TAG, "Time until class: $timeUntilClass minutes")
        
        if (startDateTime <= currentTime) {
            Log.w(TAG, "Class start time is in the past - skipping notifications")
            return
        }
        
        // Filter intervals to only include those that haven't passed yet
        val validReminderIntervals = allReminderIntervals.filter { minutes ->
            val triggerTime = startDateTime - (minutes * 60 * 1000)
            triggerTime > currentTime
        }
        
        Log.d(TAG, "Valid reminder intervals (not in past): $validReminderIntervals")
        
        if (validReminderIntervals.isEmpty()) {
            Log.w(TAG, "No valid reminder intervals - class is too soon!")
            return
        }
        
        validReminderIntervals.forEach { minutes ->
            val triggerTime = startDateTime - (minutes * 60 * 1000)
            val notificationId = classItem.id.toInt() * 1000 + minutes
            
            Log.d(TAG, "--- Reminder: $minutes minutes before ---")
            Log.d(TAG, "Notification ID: $notificationId")
            Log.d(TAG, "Trigger time: ${timeFormat.format(Date(triggerTime))}")
            
            if (triggerTime > currentTime) {
                scheduleNotification(
                    notificationId,
                    classItem.subject,
                    createClassNotificationMessage(classItem, minutes),
                    triggerTime,
                    TYPE_CLASS,
                    classItem.id,
                    minutes,
                    CHANNEL_ID_CLASSES
                )
                Log.d(TAG, "✓ Scheduled notification for $minutes minutes before")
            } else {
                Log.w(TAG, "✗ Trigger time is in the past - skipping")
            }
        }
        Log.d(TAG, "========================================================")
    }

    fun scheduleMeetingNotifications(meeting: Meeting) {
        val globalEnabled = settingsManager.areNotificationsEnabled()
        val meetingEnabled = settingsManager.areMeetingNotificationsEnabled()
        
        Log.d(TAG, "Scheduling meeting notifications for: ${meeting.title}")
        Log.d(TAG, "Global notifications enabled: $globalEnabled")
        Log.d(TAG, "Meeting notifications enabled: $meetingEnabled")
        
        if (!globalEnabled || !meetingEnabled) {
            Log.d(TAG, "Notifications disabled - skipping scheduling")
            return
        }

        val reminderIntervals = settingsManager.getReminderIntervals()
        
        reminderIntervals.forEach { minutes ->
            scheduleNotification(
                meeting.id.toInt() * 1000 + minutes, // Unique ID
                meeting.title,
                createMeetingNotificationMessage(meeting, minutes),
                meeting.getStartDateTime() - (minutes * 60 * 1000),
                TYPE_MEETING,
                meeting.id,
                minutes,
                CHANNEL_ID_MEETINGS
            )
        }
    }

    private fun scheduleNotification(
        notificationId: Int,
        title: String,
        message: String,
        triggerTime: Long,
        type: String,
        itemId: Long,
        reminderMinutes: Int,
        channelId: String
    ) {
        val currentTime = System.currentTimeMillis()
        val timeUntilTrigger = triggerTime - currentTime
        
        val timeZone = java.util.TimeZone.getDefault()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", java.util.Locale.getDefault())
        dateFormat.timeZone = timeZone
        
        Log.d(TAG, "=== SCHEDULING NOTIFICATION ===")
        Log.d(TAG, "Title: $title")
        Log.d(TAG, "Device timezone: ${timeZone.displayName} (${timeZone.id})")
        Log.d(TAG, "Current time: ${dateFormat.format(java.util.Date(currentTime))}")
        Log.d(TAG, "Trigger time: ${dateFormat.format(java.util.Date(triggerTime))}")
        Log.d(TAG, "Time until trigger: ${timeUntilTrigger / 1000}s (${timeUntilTrigger / 60000}min)")
        
        if (triggerTime <= currentTime) {
            Log.d(TAG, "Notification time has passed, skipping: $title")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
            putExtra(EXTRA_TYPE, type)
            putExtra(EXTRA_ITEM_ID, itemId)
            putExtra(EXTRA_REMINDER_MINUTES, reminderMinutes)
            putExtra(EXTRA_EVENT_START_TIME, triggerTime + (reminderMinutes * 60 * 1000))
            putExtra(EXTRA_SUBJECT_OR_TITLE, title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (canScheduleExactAlarms()) {
                // Permission check is done in canScheduleExactAlarms()
                @Suppress("MissingPermission")
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled exact notification: $title at ${Date(triggerTime)}")
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
                Log.d(TAG, "Scheduled approximate notification: $title at ${Date(triggerTime)}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule notification: $title", e)
        }
    }

    fun cancelClassNotifications(classId: Long) {
        val reminderIntervals = settingsManager.getReminderIntervals()
        
        reminderIntervals.forEach { minutes ->
            val notificationId = classId.toInt() * 1000 + minutes
            cancelNotification(notificationId)
        }
    }

    fun cancelMeetingNotifications(meetingId: Long) {
        val reminderIntervals = settingsManager.getReminderIntervals()
        
        reminderIntervals.forEach { minutes ->
            val notificationId = meetingId.toInt() * 1000 + minutes
            cancelNotification(notificationId)
        }
    }

    private fun cancelNotification(notificationId: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled notification with ID: $notificationId")
    }

    fun cancelNotifications(itemId: Long, type: String) {
        val reminderIntervals = settingsManager.getReminderIntervals()
        
        reminderIntervals.forEach { minutes ->
            val notificationId = itemId.toInt() * 1000 + minutes
            cancelNotification(notificationId)
        }
        
        Log.d(TAG, "Cancelled all notifications for $type ID: $itemId")
    }

    fun showNotification(
        notificationId: Int,
        title: String,
        message: String,
        type: String,
        itemId: Long,
        channelId: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val iconRes = when (type) {
            TYPE_CLASS -> R.drawable.ic_class_24
            TYPE_MEETING -> R.drawable.ic_meeting
            else -> R.drawable.ic_notifications_24
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVibrate(longArrayOf(0, 1000, 500, 1000, 500, 1000))
            .setSound(getAlarmSound())
            .setOnlyAlertOnce(false)
            .setFullScreenIntent(pendingIntent, true)

        // Add action buttons
        addNotificationActions(builder, type, itemId)

        try {
            notificationManager.notify(notificationId, builder.build())
            Log.d(TAG, "Notification shown: $title")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification: $title", e)
        }
    }

    private fun addNotificationActions(
        builder: NotificationCompat.Builder,
        type: String,
        itemId: Long
    ) {
        // Snooze action
        val snoozeIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "SNOOZE"
            putExtra(EXTRA_ITEM_ID, itemId)
            putExtra(EXTRA_TYPE, type)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            itemId.toInt() + 10000,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark as done action
        val doneIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = "MARK_DONE"
            putExtra(EXTRA_ITEM_ID, itemId)
            putExtra(EXTRA_TYPE, type)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            itemId.toInt() + 20000,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        builder.addAction(
            R.drawable.ic_snooze,
            "Snooze 5min",
            snoozePendingIntent
        )
        
        builder.addAction(
            R.drawable.ic_check,
            "Mark Done",
            donePendingIntent
        )
    }

    private fun createClassNotificationMessage(classItem: Class, reminderMinutes: Int): String {
        val startTime = timeFormat.format(Date(classItem.getStartDateTime()))
        
        return when (reminderMinutes) {
            0 -> "🔔 Your ${classItem.subject} class is starting now in ${classItem.roomNumber}"
            1 -> "⏰ Your ${classItem.subject} class starts in 1 minute at $startTime in ${classItem.roomNumber}"
            2 -> "⏰ Your ${classItem.subject} class starts in 2 minutes at $startTime in ${classItem.roomNumber}"
            3 -> "⏰ Your ${classItem.subject} class starts in 3 minutes at $startTime in ${classItem.roomNumber}"
            5 -> "⏰ Your ${classItem.subject} class starts in 5 minutes at $startTime in ${classItem.roomNumber}"
            else -> "📚 Your ${classItem.subject} class starts in $reminderMinutes minutes at $startTime in ${classItem.roomNumber}"
        }
    }

    private fun createMeetingNotificationMessage(meeting: Meeting, reminderMinutes: Int): String {
        val startTime = timeFormat.format(Date(meeting.getStartDateTime()))
        val location = if (meeting.location.isNotEmpty()) " at ${meeting.location}" else ""
        
        return when (reminderMinutes) {
            0 -> "Your meeting '${meeting.title}' is starting now$location"
            1 -> "Your meeting '${meeting.title}' starts in 1 minute at $startTime$location"
            else -> "Your meeting '${meeting.title}' starts in $reminderMinutes minutes at $startTime$location"
        }
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Test method to send immediate notification for debugging
     */
    fun sendTestNotification() {
        Log.d(TAG, "========= SENDING TEST NOTIFICATION =========")
        
        val currentTime = System.currentTimeMillis()
        val testTriggerTime = currentTime + 5000 // 5 seconds from now
        
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        Log.d(TAG, "Current time: ${timeFormat.format(Date(currentTime))}")
        Log.d(TAG, "Test trigger time: ${timeFormat.format(Date(testTriggerTime))}")
        Log.d(TAG, "Can schedule exact alarms: ${canScheduleExactAlarms()}")
        
        // Send immediate notification first
        sendImmediateNotification("🧪 Test Notification", "This is a test notification to verify the system is working")
        
        // Also schedule one for 5 seconds later
        scheduleNotification(
            99999, // Test notification ID
            "🧪 Scheduled Test",
            "This scheduled test notification should appear in 5 seconds",
            testTriggerTime,
            TYPE_REMINDER,
            0,
            0,
            CHANNEL_ID_REMINDERS
        )
        
        Log.d(TAG, "Test notifications sent!")
    }

    /**
     * Send immediate notification without scheduling
     */
    private fun sendImmediateNotification(title: String, message: String) {
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(longArrayOf(0, 500, 100, 500))
                .build()

            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(88888, notification)
            
            Log.d(TAG, "Immediate notification sent: $title")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send immediate notification", e)
        }
    }

}