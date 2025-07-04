package com.example.teacherscheduler.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.data.remote.FirebaseService
import com.example.teacherscheduler.model.AppSettings

import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)
    
    val settings: LiveData<AppSettings?> = repository.getSettings()

    
    private val _lastSyncTime = MutableLiveData<String>()
    val lastSyncTime: LiveData<String> = _lastSyncTime
    
    private val _syncInProgress = MutableLiveData<Boolean>()
    val syncInProgress: LiveData<Boolean> = _syncInProgress
    
    init {
        loadLastSyncTime()
    }
    
    private fun loadLastSyncTime() {
        viewModelScope.launch {
            val settings = repository.getSettingsSync()
            updateLastSyncTimeDisplay(settings.lastSyncTimestamp)
        }
    }
    
    private fun updateLastSyncTimeDisplay(timestamp: Long) {
        if (timestamp > 0) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(Date(timestamp))
            _lastSyncTime.value = "Last sync: $formattedDate"
        } else {
            _lastSyncTime.value = "Last sync: Never"
        }
    }
    
    fun saveSettings(
        classNotificationsEnabled: Boolean,
        meetingNotificationsEnabled: Boolean,
        autoSyncEnabled: Boolean
    ) {
        viewModelScope.launch {
            val currentSettings = repository.getSettingsSync()
            val updatedSettings = currentSettings.copy(
                classNotificationsEnabled = classNotificationsEnabled,
                meetingNotificationsEnabled = meetingNotificationsEnabled,
                autoSync = autoSyncEnabled
            )
            repository.updateSettings(updatedSettings)
        }
    }
    
    fun performSync() {
        viewModelScope.launch {
            _syncInProgress.value = true
            val timestamp = repository.performSync()
            updateLastSyncTimeDisplay(timestamp)
            _syncInProgress.value = false
        }
    }
    

}