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
import com.example.teacherscheduler.model.ToDo
import com.example.teacherscheduler.notification.EnhancedNotificationHelper
import com.example.teacherscheduler.util.DataSyncWorker
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class Repository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val classDao = database.classDao()
    private val meetingDao = database.meetingDao()
    private val todoDao = database.todoDao()

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
    
    fun getAllActiveClasses(): Flow<List<Class>> {
        return classDao.getAllActiveClasses()
    }
    
    suspend fun getAllActiveClassesSync(): List<Class> {
        return classDao.getAllActiveClassesSync()
    }
    
    suspend fun getClassById(id: Long): Class? {
        return classDao.getClassById(id)
    }
    
    fun getClassesForDay(date: Date): Flow<List<Class>> {
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
    
    fun getAllActiveMeetings(): Flow<List<Meeting>> {
        return meetingDao.getAllActiveMeetings()
    }
    
    suspend fun getAllActiveMeetingsSync(): List<Meeting> {
        return meetingDao.getAllActiveMeetingsSync()
    }
    
    suspend fun getMeetingById(id: Long): Meeting {
        return meetingDao.getMeetingById(id) ?: throw Exception("Meeting not found")
    }
    
    suspend fun getAllClassesDirect(): List<Class> {
        return classDao.getAllActiveClassesSync()
    }
    
    suspend fun getAllMeetingsDirect(): List<Meeting> {
        return meetingDao.getAllActiveMeetingsSync()
    }
    
    suspend fun getAllActiveClassesList(): List<Class> {
        return classDao.getAllActiveClassesSync()
    }
    
    suspend fun getAllActiveMeetingsList(): List<Meeting> {
        return meetingDao.getAllActiveMeetingsSync()
    }
    
    fun getMeetingsForDay(date: Date): Flow<List<Meeting>> {
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
        val liveData = MutableLiveData<AppSettings>()
        liveData.value = settingsManager.getSettings()
        return liveData
    }
    
    fun getSettingsSync(): AppSettings {
        return settingsManager.getSettings()
    }
    
    fun updateSettings(settings: AppSettings) {
        settingsManager.saveSettings(settings)
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
        val timestamp = System.currentTimeMillis()
        if (!firebaseService.isSignedIn()) {
            return 0
        }
        
        // Sync classes
        val unsyncedClasses = classDao.getUnsyncedClasses(timestamp)
        for (classItem in unsyncedClasses) {
            if (firebaseService.syncClass(classItem)) {
                classDao.update(classItem.copy(lastSyncTimestamp = timestamp))
            }
        }
        
        // Sync meetings
        val unsyncedMeetings = meetingDao.getUnsyncedMeetings(timestamp)
        for (meeting in unsyncedMeetings) {
            if (firebaseService.syncMeeting(meeting)) {
                meetingDao.update(meeting.copy(lastSyncTimestamp = timestamp))
            }
        }
        
        if (syncFromCloud()) {
            settingsManager.updateLastSyncTime(timestamp)
            return timestamp
        }
        return 0
    }
    
    // ToDo operations
    suspend fun insertToDo(todo: ToDo): Long {
        val roomId = todoDao.insert(todo)

        // Schedule notifications if enabled and has due date
        if (todo.notificationsEnabled && todo.dueDate != null) {
            // TODO: Add notification scheduling for todos
        }

        scheduleSync()
        return roomId
    }

    suspend fun updateToDo(todo: ToDo) {
        todoDao.update(todo)
        scheduleSync()
    }

    suspend fun deleteToDo(todo: ToDo) {
        val inactiveTodo = todo.copy(isActive = false)
        todoDao.update(inactiveTodo)
        scheduleSync()
    }

    suspend fun toggleToDoCompletion(id: Long, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        todoDao.updateCompletionStatus(id, isCompleted, completedAt)
        scheduleSync()
    }

    fun getAllActiveToDos(): Flow<List<ToDo>> {
        return todoDao.getAllActiveToDos()
    }

    fun getAllToDos(): Flow<List<ToDo>> {
        return todoDao.getAllToDos()
    }

    fun getCompletedToDos(): Flow<List<ToDo>> {
        return todoDao.getCompletedToDos()
    }

    fun getOverdueToDos(): Flow<List<ToDo>> {
        return todoDao.getOverdueToDos(System.currentTimeMillis())
    }

    fun getToDosByCategory(category: String): Flow<List<ToDo>> {
        return todoDao.getToDosByCategory(category)
    }

    fun getToDosByPriority(priority: ToDo.Priority): Flow<List<ToDo>> {
        return todoDao.getToDosByPriority(priority.value)
    }

    suspend fun getToDoById(id: Long): ToDo? {
        return todoDao.getToDoById(id)
    }

    suspend fun getAllToDosSync(): List<ToDo> {
        return todoDao.getAllToDosSync()
    }

    fun getActiveToDosCount(): Flow<Int> {
        return todoDao.getActiveToDosCount()
    }

    fun getOverdueToDosCount(): Flow<Int> {
        return todoDao.getOverdueToDosCount(System.currentTimeMillis())
    }

    fun getAllCategories(): Flow<List<String>> {
        return todoDao.getAllCategories()
    }

    private suspend fun syncFromCloud(): Boolean {
        return try {
            val cloudClasses = firebaseService.getClasses()
            for (cloudClass in cloudClasses) {
                val localClass = classDao.getClassById(cloudClass.id)
                if (localClass == null) {
                    classDao.insert(cloudClass)
                } else if (cloudClass.lastSyncTimestamp > localClass.lastSyncTimestamp) {
                    classDao.update(cloudClass)
                }
            }
            
            val cloudMeetings = firebaseService.getMeetings()
            for (cloudMeeting in cloudMeetings) {
                val localMeeting = meetingDao.getMeetingById(cloudMeeting.id)
                if (localMeeting == null) {
                    meetingDao.insert(cloudMeeting)
                } else if (cloudMeeting.lastSyncTimestamp > localMeeting.lastSyncTimestamp) {
                    meetingDao.update(cloudMeeting)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}