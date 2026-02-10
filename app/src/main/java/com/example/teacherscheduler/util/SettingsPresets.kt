package com.example.teacherscheduler.util

import com.example.teacherscheduler.model.AppSettings

object SettingsPresets {
    
    fun getMaxNotificationsPreset(): AppSettings {
        return AppSettings(
            classNotificationsEnabled = true,
            meetingNotificationsEnabled = true,
            reminderTime = 15,
            notificationSound = true,
            notificationVibration = true,
            autoSync = true
        )
    }
    
    fun getMinimalNotificationsPreset(): AppSettings {
        return AppSettings(
            classNotificationsEnabled = true,
            meetingNotificationsEnabled = true,
            reminderTime = 15,
            notificationSound = false,
            notificationVibration = true,
            autoSync = true
        )
    }
    
    fun getSilentModePreset(): AppSettings {
        return AppSettings(
            classNotificationsEnabled = false,
            meetingNotificationsEnabled = false,
            reminderTime = 0,
            notificationSound = false,
            notificationVibration = false,
            autoSync = true
        )
    }
}
