package com.example.teacherscheduler.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Long = 1, // Only one settings record
    val classNotificationsEnabled: Boolean = true,
    val meetingNotificationsEnabled: Boolean = true,
    val reminderTime: Int = 15,
    val autoSync: Boolean = false,
    val notificationSound: Boolean = true,
    val notificationVibration: Boolean = true,
    val advanceNotification: Boolean = true,
    val theme: Int = 0,
    val currentSemesterId: Long = 0,
    val lastSyncTimestamp: Long = 0
)