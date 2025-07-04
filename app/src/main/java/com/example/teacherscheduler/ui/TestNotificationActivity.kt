package com.example.teacherscheduler.ui

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.teacherscheduler.R
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import com.example.teacherscheduler.notification.NotificationHelper
import com.example.teacherscheduler.util.NotificationDebugHelper
import java.util.*

class TestNotificationActivity : AppCompatActivity() {

    private lateinit var notificationHelper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_notification)

        notificationHelper = NotificationHelper(this)
        
        // Check notification settings and log debug info
        NotificationDebugHelper.checkNotificationSettings(this)

        setupButtons()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnTestImmediateNotification).setOnClickListener {
            testImmediateNotification()
        }

        findViewById<Button>(R.id.btnTestClassReminders).setOnClickListener {
            testClassReminders()
        }

        findViewById<Button>(R.id.btnTestMeetingReminders).setOnClickListener {
            testMeetingReminders()
        }

        findViewById<Button>(R.id.btnTestSnooze).setOnClickListener {
            testSnoozeFunction()
        }
    }

    private fun testImmediateNotification() {
        notificationHelper.showNotification(
            notificationId = 99999,
            title = "🔔 TEST IMMEDIATE",
            message = "This should appear right now with sound & vibration!",
            type = NotificationHelper.TYPE_CLASS,
            itemId = 999,
            reminderMinutes = 0
        )
        Toast.makeText(this, "Immediate notification shown!", Toast.LENGTH_SHORT).show()
    }

    private fun testClassReminders() {
        val testClass = Class(
            id = 888,
            subject = "TEST CLASS",
            department = "TEST DEPT",
            roomNumber = "TEST ROOM",
            startDate = Calendar.getInstance().time,
            endDate = Calendar.getInstance().time,
            startTime = Calendar.getInstance().apply {
                add(Calendar.MINUTE, 6) // 6 minutes from now
            }.time,
            endTime = Calendar.getInstance().apply {
                add(Calendar.MINUTE, 66) // 1 hour 6 minutes from now
            }.time,
            isRecurring = false,
            isActive = true
        )

        notificationHelper.scheduleClassNotifications(testClass)

        Toast.makeText(this,
            "✅ TEST CLASS scheduled!\nYou should get notifications at:\n" +
                    "• Now + 1 min (5 min before)\n" +
                    "• Now + 2 min (4 min before)\n" +
                    "• Now + 3 min (3 min before)\n" +
                    "• Now + 4 min (2 min before)\n" +
                    "• Now + 5 min (1 min before)\n" +
                    "• Now + 6 min (EXACT TIME)",
            Toast.LENGTH_LONG).show()
    }

    private fun testMeetingReminders() {
        val testMeeting = Meeting(
            id = 777,
            title = "TEST MEETING",
            withWhom = "TEST PERSON",
            location = "TEST LOCATION",
            notes = "Test notes",
            startDate = Calendar.getInstance().time,
            endDate = Calendar.getInstance().time,
            startTime = Calendar.getInstance().apply {
                add(Calendar.MINUTE, 6) // 6 minutes from now
            }.time,
            endTime = Calendar.getInstance().apply {
                add(Calendar.MINUTE, 66) // 1 hour 6 minutes from now
            }.time,
            isActive = true
        )

        notificationHelper.scheduleMeetingNotifications(testMeeting)

        Toast.makeText(this,
            "✅ TEST MEETING scheduled!\nSame timing as class test",
            Toast.LENGTH_LONG).show()
    }

    private fun testSnoozeFunction() {
        // Show a notification immediately to test snooze
        notificationHelper.showNotification(
            notificationId = 77777,
            title = "🔔 TEST SNOOZE - Tap snooze button!",
            message = "This notification should reappear in 2 minutes when you snooze it",
            type = NotificationHelper.TYPE_MEETING,
            itemId = 777,
            reminderMinutes = 0
        )
        Toast.makeText(this, "Snooze test notification shown! Tap the snooze button.", Toast.LENGTH_LONG).show()
    }
}