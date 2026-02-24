package com.example.teacherscheduler.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.SettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationSettings(
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val classNotificationsEnabled: Boolean = true,
    val meetingNotificationsEnabled: Boolean = true,
    val reminderIntervals: List<Int> = listOf(0, 5, 15, 30)
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val settingsManager = SettingsManager(context)
    
    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<NotificationSettings> = _settings.asStateFlow()
    
    private fun loadSettings(): NotificationSettings {
        return NotificationSettings(
            notificationsEnabled = settingsManager.areNotificationsEnabled(),
            soundEnabled = settingsManager.isSoundEnabled(),
            classNotificationsEnabled = settingsManager.areClassNotificationsEnabled(),
            meetingNotificationsEnabled = settingsManager.areMeetingNotificationsEnabled(),
            reminderIntervals = settingsManager.getReminderIntervals()
        )
    }
    
    fun setNotificationsEnabled(enabled: Boolean) {
        _settings.update { it.copy(notificationsEnabled = enabled) }
    }
    
    fun setSoundEnabled(enabled: Boolean) {
        _settings.update { it.copy(soundEnabled = enabled) }
    }
    
    fun setClassNotificationsEnabled(enabled: Boolean) {
        _settings.update { it.copy(classNotificationsEnabled = enabled) }
    }
    
    fun setMeetingNotificationsEnabled(enabled: Boolean) {
        _settings.update { it.copy(meetingNotificationsEnabled = enabled) }
    }
    
    fun setReminderIntervals(intervals: List<Int>) {
        _settings.update { it.copy(reminderIntervals = intervals) }
    }
    
    fun saveSettings() {
        viewModelScope.launch {
            val current = _settings.value
            settingsManager.setNotificationsEnabled(current.notificationsEnabled)
            settingsManager.setSoundEnabled(current.soundEnabled)
            settingsManager.setClassNotificationsEnabled(current.classNotificationsEnabled)
            settingsManager.setMeetingNotificationsEnabled(current.meetingNotificationsEnabled)
            settingsManager.setReminderIntervals(current.reminderIntervals)
        }
    }
}
