package com.example.teacherscheduler.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.teacherscheduler.data.local.AppDatabase
import com.example.teacherscheduler.data.remote.FirebaseService
import com.example.teacherscheduler.model.AppSettings
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import com.example.teacherscheduler.notification.NotificationHelper
import com.example.teacherscheduler.notification.EnhancedNotificationHelper
import com.example.teacherscheduler.util.DataSyncWorker
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class Repository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val classDao = database.classDao()
    private val meetingDao = database.meetingDao()

    private val firebaseService = FirebaseService()
    private val workManager = WorkManager.getInstance(context)
    private val notificationHelper = EnhancedNotificationHelper(context)
    
    init {
        // Schedule periodic sync
        schedulePeriodicSync()
    }
    
    // Class operations
    suspend fun insertClass(classItem: Class): Long {
        // Insert into Room database
        val roomId = classDao.insert(classItem)
        
        // Schedule notifications
        if (classItem.notificationsEnabled) {
            notificationHelper.scheduleClassNotifications(classItem)
        }
        
        // Schedule sync with Firebase
        scheduleSync()
        
        return roomId
    }
    
    suspend fun updateClass(classItem: Class) {
        // Cancel old notifications
        notificationHelper.cancelClassNotifications(classItem.id)
        
        // Update in Room
        classDao.update(classItem)
        
        // Schedule new notifications
        if (classItem.notificationsEnabled) {
            notificationHelper.scheduleClassNotifications(classItem)
        }
        
        // Schedule sync with Firebase
        scheduleSync()
    }
    
    suspend fun deleteClass(classItem: Class) {
        // Cancel notifications
        notificationHelper.cancelClassNotifications(classItem.id)
        
        // Mark as inactive instead of deleting
        val inactiveClass = classItem.copy(isActive = false)
        classDao.update(inactiveClass)
        
        // Schedule sync with Firebase
        scheduleSync()
    }
    
    fun getAllActiveClasses(): LiveData<List<Class>> {
        return classDao.getAllActiveClasses()
    }
    
    suspend fun getAllActiveClassesSync(): List<Class> {
        return classDao.getAllActiveClassesSync()
    }
    
    suspend fun getAllActiveClassesList(): List<Class> {
        return classDao.getAllActiveClassesSync()
    }
    
    suspend fun getClassById(id: Long): Class? {
        return classDao.getClassById(id)
    }
    
    fun getClassesForDay(date: Date): LiveData<List<Class>> {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val dateString = dateFormat.format(date)
        
        return classDao.getClassesForDay(dateString)
    }
    
    // Meeting operations
    suspend fun insertMeeting(meeting: Meeting): Long {
        // Insert into Room database
        val roomId = meetingDao.insert(meeting)
        
        // Schedule notifications
        if (meeting.notificationsEnabled) {
            notificationHelper.scheduleMeetingNotifications(meeting)
        }
        
        // Schedule sync with Firebase
        scheduleSync()
        
        return roomId
    }
    
    suspend fun updateMeeting(meeting: Meeting) {
        // Cancel old notifications
        notificationHelper.cancelMeetingNotifications(meeting.id)
        
        // Update in Room
        meetingDao.update(meeting)
        
        // Schedule new notifications
        if (meeting.notificationsEnabled) {
            notificationHelper.scheduleMeetingNotifications(meeting)
        }
        
        // Schedule sync with Firebase
        scheduleSync()
    }
    
    suspend fun deleteMeeting(meeting: Meeting) {
        // Cancel notifications
        notificationHelper.cancelMeetingNotifications(meeting.id)
        
        // Mark as inactive instead of deleting
        val inactiveMeeting = meeting.copy(isActive = false)
        meetingDao.update(inactiveMeeting)
        
        // Schedule sync with Firebase
        scheduleSync()
    }
    
    fun getAllActiveMeetings(): LiveData<List<Meeting>> {
        return meetingDao.getAllActiveMeetings()
    }
    
    suspend fun getAllActiveMeetingsSync(): List<Meeting> {
        return meetingDao.getAllActiveMeetingsSync()
    }
    
    suspend fun getAllActiveMeetingsList(): List<Meeting> {
        return meetingDao.getAllActiveMeetingsSync()
    }
    
    suspend fun getMeetingById(id: Long): Meeting {
        return meetingDao.getMeetingById(id) ?: throw Exception("Meeting not found")
    }
    
    fun getMeetingsForDay(date: Date): LiveData<List<Meeting>> {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val dateString = dateFormat.format(date)
        
        return meetingDao.getMeetingsForDay(dateString)
    }
    
    // Date range methods for dashboard
    suspend fun getClassesForDateRange(startTime: Long, endTime: Long): List<Class> {
        val allClasses = classDao.getClassesForDateRange(startTime, endTime)
        return allClasses.filter { classItem ->
            if (classItem.isRecurring) {
                // For recurring classes, check if any day in the range matches the days of week
                val startCal = Calendar.getInstance().apply { timeInMillis = startTime }
                val endCal = Calendar.getInstance().apply { timeInMillis = endTime }
                
                var currentCal = startCal.clone() as Calendar
                while (currentCal.timeInMillis <= endCal.timeInMillis) {
                    val dayOfWeek = currentCal.get(Calendar.DAY_OF_WEEK)
                    if (classItem.daysOfWeek.contains(dayOfWeek)) {
                        return@filter true
                    }
                    currentCal.add(Calendar.DAY_OF_YEAR, 1)
                }
                false
            } else {
                // For non-recurring classes, check if the class time overlaps with the range
                val classStart = classItem.getStartDateTime()
                val classEnd = classItem.getEndDateTime()
                classStart <= endTime && classEnd >= startTime
            }
        }
    }
    
    suspend fun getMeetingsForDateRange(startTime: Long, endTime: Long): List<Meeting> {
        val allMeetings = meetingDao.getMeetingsForDateRange(startTime, endTime)
        return allMeetings.filter { meeting ->
            val meetingStart = meeting.getStartDateTime()
            val meetingEnd = meeting.getEndDateTime()
            // Check if meeting overlaps with the requested time range
            meetingStart <= endTime && meetingEnd >= startTime
        }
    }
    

    
    // Settings operations using SettingsManager
    private val settingsManager = SettingsManager(context)
    
    fun getSettings(): LiveData<AppSettings?> {
        // Create a LiveData wrapper for the settings
        val liveData = MutableLiveData<AppSettings>()
        liveData.value = settingsManager.getSettings()
        return liveData
    }
    
    fun getSettingsSync(): AppSettings {
        return settingsManager.getSettings()
    }
    
    fun updateSettings(settings: AppSettings) {
        // Update using SettingsManager
        settingsManager.saveSettings(settings)
        
        // If auto sync is enabled, schedule sync
        if (settings.autoSync) {
            scheduleSync()
        }
    }
    

    
    // Synchronization methods
    private fun scheduleSync() {
        val syncWorkRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()
        
        workManager.enqueueUniqueWork(
            "data_sync",
            ExistingWorkPolicy.REPLACE,
            syncWorkRequest
        )
    }
    
    private fun schedulePeriodicSync() {
        val syncWorkRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
            .setInitialDelay(15, TimeUnit.MINUTES)
            .build()
        
        workManager.enqueueUniqueWork(
            "periodic_sync",
            ExistingWorkPolicy.KEEP,
            syncWorkRequest
        )
    }
    
    suspend fun performSync(): Long {
        // First, sync local changes to cloud
        val timestamp = System.currentTimeMillis()
        
        // Check if user is authenticated
        if (!firebaseService.isSignedIn()) {
            android.util.Log.w("Repository", "Sync failed: User not signed in")
            return 0
        }
        
        // Check if auto sync is enabled (allow manual sync to proceed regardless)
        val settings = settingsManager.getSettings()
        android.util.Log.d("Repository", "Auto sync enabled: ${settings.autoSync}")
        android.util.Log.d("Repository", "User signed in: ${firebaseService.isSignedIn()}")
        

        
        // Sync classes
        val unsyncedClasses = classDao.getUnsyncedClasses(timestamp)
        android.util.Log.d("Repository", "Found ${unsyncedClasses.size} unsynced classes")
        
        for (classItem in unsyncedClasses) {
            val success = firebaseService.syncClass(classItem)
            if (success) {
                // Update sync timestamp
                val syncedClass = classItem.copy(lastSyncTimestamp = timestamp)
                classDao.update(syncedClass)
                android.util.Log.d("Repository", "✓ Synced class: ${classItem.subject}")
            } else {
                android.util.Log.w("Repository", "✗ Failed to sync class: ${classItem.subject}")
            }
        }
        
        // Sync meetings
        val unsyncedMeetings = meetingDao.getUnsyncedMeetings(timestamp)
        android.util.Log.d("Repository", "Found ${unsyncedMeetings.size} unsynced meetings")
        
        for (meeting in unsyncedMeetings) {
            val success = firebaseService.syncMeeting(meeting)
            if (success) {
                // Update sync timestamp
                val syncedMeeting = meeting.copy(lastSyncTimestamp = timestamp)
                meetingDao.update(syncedMeeting)
                android.util.Log.d("Repository", "✓ Synced meeting: ${meeting.title}")
            } else {
                android.util.Log.w("Repository", "✗ Failed to sync meeting: ${meeting.title}")
            }
        }
        
        // Then, sync from cloud to local
        val cloudSyncSuccess = syncFromCloud()
        
        if (cloudSyncSuccess) {
            // Update last sync timestamp in settings
            settingsManager.updateLastSyncTime(timestamp)
            android.util.Log.d("Repository", "Sync completed successfully")
            return timestamp
        } else {
            android.util.Log.w("Repository", "Sync failed during cloud sync")
            return 0
        }
    }
    
    suspend fun performSyncDebug(): String {
        val debug = StringBuilder()
        debug.appendLine("=== SYNC DEBUG INFO ===")
        
        try {
            // Check authentication
            val isSignedIn = firebaseService.isSignedIn()
            debug.appendLine("User signed in: $isSignedIn")
            
            if (!isSignedIn) {
                debug.appendLine("ERROR: User not authenticated")
                return debug.toString()
            }
            
            // Check settings
            val settings = settingsManager.getSettings()
            debug.appendLine("Auto sync enabled: ${settings.autoSync}")
            
            // Check local data
            val unsyncedClasses = classDao.getUnsyncedClasses(System.currentTimeMillis())
            val unsyncedMeetings = meetingDao.getUnsyncedMeetings(System.currentTimeMillis())
            debug.appendLine("Unsynced classes: ${unsyncedClasses.size}")
            debug.appendLine("Unsynced meetings: ${unsyncedMeetings.size}")
            
            // Test Firebase connection
            debug.appendLine("Testing Firebase connection...")
            val cloudClasses = firebaseService.getClasses()
            debug.appendLine("Cloud classes retrieved: ${cloudClasses.size}")
            
            val cloudMeetings = firebaseService.getMeetings()
            debug.appendLine("Cloud meetings retrieved: ${cloudMeetings.size}")
            
            debug.appendLine("Firebase connection: SUCCESS")
            
        } catch (e: Exception) {
            debug.appendLine("ERROR: ${e.message}")
            android.util.Log.e("Repository", "Sync debug failed", e)
        }
        
        return debug.toString()
    }
    
    private suspend fun syncFromCloud(): Boolean {
        return try {
            android.util.Log.d("Repository", "Starting sync from cloud")
            
            // Get classes from cloud
            val cloudClasses = firebaseService.getClasses()
            android.util.Log.d("Repository", "Downloaded ${cloudClasses.size} classes from cloud")
            
            for (cloudClass in cloudClasses) {
                val localClass = classDao.getClassById(cloudClass.id)
                
                if (localClass == null) {
                    // New class from cloud
                    classDao.insert(cloudClass)
                    android.util.Log.d("Repository", "↓ Inserted new class from cloud: ${cloudClass.subject}")
                } else if (cloudClass.lastSyncTimestamp > localClass.lastSyncTimestamp) {
                    // Cloud class is newer
                    classDao.update(cloudClass)
                    android.util.Log.d("Repository", "↓ Updated class from cloud: ${cloudClass.subject}")
                }
            }
            
            // Get meetings from cloud
            val cloudMeetings = firebaseService.getMeetings()
            android.util.Log.d("Repository", "Downloaded ${cloudMeetings.size} meetings from cloud")
            
            for (cloudMeeting in cloudMeetings) {
                val localMeeting = meetingDao.getMeetingById(cloudMeeting.id)
                
                if (localMeeting == null) {
                    // New meeting from cloud
                    meetingDao.insert(cloudMeeting)
                    android.util.Log.d("Repository", "↓ Inserted new meeting from cloud: ${cloudMeeting.title}")
                } else if (cloudMeeting.lastSyncTimestamp > localMeeting.lastSyncTimestamp) {
                    // Cloud meeting is newer
                    meetingDao.update(cloudMeeting)
                    android.util.Log.d("Repository", "↓ Updated meeting from cloud: ${cloudMeeting.title}")
                }
            }
            
            android.util.Log.d("Repository", "Cloud sync completed successfully")
            true
        } catch (e: Exception) {
            android.util.Log.e("Repository", "Cloud sync failed: ${e.message}", e)
            false
        }
    }
}