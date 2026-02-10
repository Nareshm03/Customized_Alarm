package com.example.teacherscheduler.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.notification.EnhancedNotificationHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class ClassViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)
    private val notificationHelper = EnhancedNotificationHelper(application)
    
    val allClasses: StateFlow<List<Class>> = repository.getAllActiveClasses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun getClassesForDay(date: Date): StateFlow<List<Class>> {
        return repository.getClassesForDay(date)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
    
    fun insertClass(classItem: Class) = viewModelScope.launch {
        val id = repository.insertClass(classItem)
        
        // Schedule notifications
        val classWithId = classItem.copy(id = id)
        if (classWithId.notificationsEnabled) {
            notificationHelper.scheduleClassNotifications(classWithId)
        }
    }
    
    fun updateClass(classItem: Class) = viewModelScope.launch {
        repository.updateClass(classItem)
        
        // Cancel old notifications and schedule new ones
        notificationHelper.cancelNotifications(classItem.id, EnhancedNotificationHelper.TYPE_CLASS)
        if (classItem.notificationsEnabled) {
            notificationHelper.scheduleClassNotifications(classItem)
        }
    }
    
    fun deleteClass(classItem: Class) = viewModelScope.launch {
        repository.deleteClass(classItem)
        
        // Cancel notifications
        notificationHelper.cancelNotifications(classItem.id, EnhancedNotificationHelper.TYPE_CLASS)
    }
    
    suspend fun getClassById(id: Long): Class? {
        return repository.getClassById(id)
    }
}