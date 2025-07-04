package com.example.teacherscheduler.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.Meeting

import com.example.teacherscheduler.notification.EnhancedNotificationHelper
import kotlinx.coroutines.launch
import java.util.Date

class MeetingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)
    private val notificationHelper = EnhancedNotificationHelper(application)
    
    val allMeetings: LiveData<List<Meeting>> = repository.getAllActiveMeetings()
    
    fun getMeetingsForDay(date: Date): LiveData<List<Meeting>> {
        return repository.getMeetingsForDay(date)
    }
    
    fun insertMeeting(meeting: Meeting) = viewModelScope.launch {
        val id = repository.insertMeeting(meeting)
        
        // Schedule notifications
        val meetingWithId = meeting.copy(id = id)
        if (meetingWithId.notificationsEnabled) {
            notificationHelper.scheduleMeetingNotifications(meetingWithId)
        }
    }
    
    fun updateMeeting(meeting: Meeting) = viewModelScope.launch {
        repository.updateMeeting(meeting)
        
        // Cancel old notifications and schedule new ones
        notificationHelper.cancelNotifications(meeting.id, EnhancedNotificationHelper.TYPE_MEETING)
        if (meeting.notificationsEnabled) {
            notificationHelper.scheduleMeetingNotifications(meeting)
        }
    }
    
    fun deleteMeeting(meeting: Meeting) = viewModelScope.launch {
        repository.deleteMeeting(meeting)
        
        // Cancel notifications
        notificationHelper.cancelNotifications(meeting.id, EnhancedNotificationHelper.TYPE_MEETING)
    }
    
    suspend fun getMeetingById(id: Long): Meeting? {
        return repository.getMeetingById(id)
    }
}