package com.example.teacherscheduler.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIFICATION_SOUND = booleanPreferencesKey("notification_sound")
        val NOTIFICATION_VIBRATION = booleanPreferencesKey("notification_vibration")
        val DEFAULT_REMINDER_TIME = intPreferencesKey("default_reminder_time")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }
    val notificationSound: Flow<Boolean> = dataStore.data.map { it[NOTIFICATION_SOUND] ?: true }
    val notificationVibration: Flow<Boolean> = dataStore.data.map { it[NOTIFICATION_VIBRATION] ?: true }
    val defaultReminderTime: Flow<Int> = dataStore.data.map { it[DEFAULT_REMINDER_TIME] ?: 15 }
    val themeMode: Flow<String> = dataStore.data.map { it[THEME_MODE] ?: "system" }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setNotificationSound(enabled: Boolean) {
        dataStore.edit { it[NOTIFICATION_SOUND] = enabled }
    }

    suspend fun setNotificationVibration(enabled: Boolean) {
        dataStore.edit { it[NOTIFICATION_VIBRATION] = enabled }
    }

    suspend fun setDefaultReminderTime(minutes: Int) {
        dataStore.edit { it[DEFAULT_REMINDER_TIME] = minutes }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[THEME_MODE] = mode }
    }
}
