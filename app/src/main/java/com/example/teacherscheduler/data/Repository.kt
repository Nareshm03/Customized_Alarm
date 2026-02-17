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
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Repository - Single Source of Truth for all app data
 *
 * Design Principles:
 * - All data exposed as Flow<T>
 * - No manual refresh needed
 * - All operations trigger Flow updates automatically
 * - Comprehensive reactive data streams
 * - No hardcoded counts - all derived from flows
 */
class Repository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val classDao = database.classDao()
    private val meetingDao = database.meetingDao()
    private val todoDao = database.todoDao()

    private val firebaseService = FirebaseService()
    private val workManager = WorkManager.getInstance(context)
    private val notificationHelper = EnhancedNotificationHelper(context)
    private val settingsManager = SettingsManager(context)

    init {
        // Schedule periodic sync
        schedulePeriodicSync()
    }

    // ==================== CLASSES - REACTIVE DATA FLOWS ====================

    /**
     * Primary source of truth for all active classes
     * All UI should observe this flow
     */
    fun getAllActiveClasses(): Flow<List<Class>> {
        return classDao.getAllActiveClasses()
    }

    /**
     * Get classes for specific day - reactive
     */
    fun getClassesForDay(date: Date): Flow<List<Class>> {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val dateString = dateFormat.format(date)
        return classDao.getClassesForDay(dateString)
    }

    /**
     * Reactive count of all active classes
     */
    fun getActiveClassesCount(): Flow<Int> {
        return getAllActiveClasses().map { it.size }
    }

    /**
     * Reactive count of today's classes
     */
    fun getTodayClassesCount(): Flow<Int> {
        return getTodayClasses().map { it.size }
    }

    /**
     * Get today's classes - reactive
     */
    fun getTodayClasses(): Flow<List<Class>> {
        return getAllActiveClasses().map { classes ->
            filterTodayClasses(classes)
        }
    }

    /**
     * Get this week's classes - reactive
     */
    fun getWeekClasses(): Flow<List<Class>> {
        return getAllActiveClasses().map { classes ->
            filterWeekClasses(classes)
        }
    }

    /**
     * Get classes by department - reactive
     */
    fun getClassesByDepartment(department: String): Flow<List<Class>> {
        return getAllActiveClasses().map { classes ->
            classes.filter { it.department.equals(department, ignoreCase = true) }
        }
    }

    // Class operations (mutations)
    suspend fun insertClass(classItem: Class): Long {
        val roomId = classDao.insert(classItem)
        if (classItem.notificationsEnabled) {
            notificationHelper.scheduleClassNotifications(classItem)
        }
        scheduleSync()
        DataEventManager.emit(DataEventManager.DataEvent.ClassAdded)
        return roomId
    }

    suspend fun updateClass(classItem: Class) {
        notificationHelper.cancelClassNotifications(classItem.id)
        classDao.update(classItem)
        if (classItem.notificationsEnabled) {
            notificationHelper.scheduleClassNotifications(classItem)
        }
        scheduleSync()
        DataEventManager.emit(DataEventManager.DataEvent.ClassUpdated)
    }

    suspend fun deleteClass(classItem: Class) {
        notificationHelper.cancelClassNotifications(classItem.id)
        val inactiveClass = classItem.copy(isActive = false)
        classDao.update(inactiveClass)
        scheduleSync()
        DataEventManager.emit(DataEventManager.DataEvent.ClassDeleted)
    }

    // Sync operations (non-reactive)
    suspend fun getAllActiveClassesSync(): List<Class> {
        return classDao.getAllActiveClassesSync()
    }

    suspend fun getClassById(id: Long): Class? {
        return classDao.getClassById(id)
    }

    // ==================== MEETINGS - REACTIVE DATA FLOWS ====================

    /**
     * Primary source of truth for all active meetings
     * All UI should observe this flow
     */
    fun getAllActiveMeetings(): Flow<List<Meeting>> {
        return meetingDao.getAllActiveMeetings()
    }

    /**
     * Get meetings for specific day - reactive
     */
    fun getMeetingsForDay(date: Date): Flow<List<Meeting>> {
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val dateString = dateFormat.format(date)
        return meetingDao.getMeetingsForDay(dateString)
    }

    /**
     * Reactive count of all active meetings
     */
    fun getActiveMeetingsCount(): Flow<Int> {
        return getAllActiveMeetings().map { it.size }
    }

    /**
     * Get upcoming meetings (next 7 days) - reactive
     */
    fun getUpcomingMeetings(): Flow<List<Meeting>> {
        return getAllActiveMeetings().map { meetings ->
            filterUpcomingMeetings(meetings)
        }
    }

    /**
     * Reactive count of upcoming meetings
     */
    fun getUpcomingMeetingsCount(): Flow<Int> {
        return getUpcomingMeetings().map { it.size }
    }

    /**
     * Get today's meetings - reactive
     */
    fun getTodayMeetings(): Flow<List<Meeting>> {
        return getAllActiveMeetings().map { meetings ->
            filterTodayMeetings(meetings)
        }
    }

    /**
     * Get this week's meetings - reactive
     */
    fun getWeekMeetings(): Flow<List<Meeting>> {
        return getAllActiveMeetings().map { meetings ->
            filterWeekMeetings(meetings)
        }
    }

    // Meeting operations (mutations)
    suspend fun insertMeeting(meeting: Meeting): Long {
        val roomId = meetingDao.insert(meeting)
        if (meeting.notificationsEnabled) {
            notificationHelper.scheduleMeetingNotifications(meeting)
        }
        scheduleSync()
        DataEventManager.emit(DataEventManager.DataEvent.MeetingAdded)
        return roomId
    }

    suspend fun updateMeeting(meeting: Meeting) {
        notificationHelper.cancelMeetingNotifications(meeting.id)
        meetingDao.update(meeting)
        if (meeting.notificationsEnabled) {
            notificationHelper.scheduleMeetingNotifications(meeting)
        }
        scheduleSync()
        DataEventManager.emit(DataEventManager.DataEvent.MeetingUpdated)
    }

    suspend fun deleteMeeting(meeting: Meeting) {
        notificationHelper.cancelMeetingNotifications(meeting.id)
        val inactiveMeeting = meeting.copy(isActive = false)
        meetingDao.update(inactiveMeeting)
        scheduleSync()
        DataEventManager.emit(DataEventManager.DataEvent.MeetingDeleted)
    }

    // Sync operations (non-reactive)
    suspend fun getAllActiveMeetingsSync(): List<Meeting> {
        return meetingDao.getAllActiveMeetingsSync()
    }

    suspend fun getMeetingById(id: Long): Meeting {
        return meetingDao.getMeetingById(id) ?: throw Exception("Meeting not found")
    }

    // Legacy methods for backward compatibility
    suspend fun getAllClassesDirect(): List<Class> = getAllActiveClassesSync()
    suspend fun getAllMeetingsDirect(): List<Meeting> = getAllActiveMeetingsSync()
    suspend fun getAllActiveClassesList(): List<Class> = getAllActiveClassesSync()
    suspend fun getAllActiveMeetingsList(): List<Meeting> = getAllActiveMeetingsSync()

    // ==================== TODOS - REACTIVE DATA FLOWS ====================

    /**
     * Primary source of truth for all active todos
     * All UI should observe this flow
     */
    fun getAllActiveToDos(): Flow<List<ToDo>> {
        return todoDao.getAllActiveToDos()
    }

    /**
     * Get all todos (including completed) - reactive
     */
    fun getAllToDos(): Flow<List<ToDo>> {
        return todoDao.getAllToDos()
    }

    /**
     * Get completed todos - reactive
     */
    fun getCompletedToDos(): Flow<List<ToDo>> {
        return todoDao.getCompletedToDos()
    }

    /**
     * Get overdue todos - reactive
     */
    fun getOverdueToDos(): Flow<List<ToDo>> {
        return todoDao.getOverdueToDos(System.currentTimeMillis())
    }

    /**
     * Get todos by category - reactive
     */
    fun getToDosByCategory(category: String): Flow<List<ToDo>> {
        return todoDao.getToDosByCategory(category)
    }

    /**
     * Get todos by priority - reactive
     */
    fun getToDosByPriority(priority: ToDo.Priority): Flow<List<ToDo>> {
        return todoDao.getToDosByPriority(priority.value)
    }

    /**
     * Reactive count of active todos
     */
    fun getActiveToDosCount(): Flow<Int> {
        return todoDao.getActiveToDosCount()
    }

    /**
     * Reactive count of overdue todos
     */
    fun getOverdueToDosCount(): Flow<Int> {
        return todoDao.getOverdueToDosCount(System.currentTimeMillis())
    }

    /**
     * Get all categories - reactive
     */
    fun getAllCategories(): Flow<List<String>> {
        return todoDao.getAllCategories()
    }

    /**
     * Get urgent todos (HIGH and URGENT priority) - reactive
     */
    fun getUrgentToDos(): Flow<List<ToDo>> {
        return getAllActiveToDos().map { todos ->
            todos.filter {
                it.priority == ToDo.Priority.URGENT || it.priority == ToDo.Priority.HIGH
            }.take(5)
        }
    }

    // ToDo operations (mutations)
    suspend fun insertToDo(todo: ToDo): Long {
        val roomId = todoDao.insert(todo)
        if (todo.notificationsEnabled && todo.dueDate != null) {
            // TODO: Add notification scheduling for todos
        }
        scheduleSync()
        DataEventManager.emit(DataEventManager.DataEvent.TaskUpdated)
        return roomId
    }

    suspend fun updateToDo(todo: ToDo) {
        todoDao.update(todo)
        scheduleSync()
        DataEventManager.emit(DataEventManager.DataEvent.TaskUpdated)
    }

    suspend fun deleteToDo(todo: ToDo) {
        val inactiveTodo = todo.copy(isActive = false)
        todoDao.update(inactiveTodo)
        scheduleSync()
        DataEventManager.emit(DataEventManager.DataEvent.TaskUpdated)
    }

    suspend fun toggleToDoCompletion(id: Long, isCompleted: Boolean) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        todoDao.updateCompletionStatus(id, isCompleted, completedAt)
        scheduleSync()
        DataEventManager.emit(DataEventManager.DataEvent.TaskCompleted)
    }

    // Sync operations (non-reactive)
    suspend fun getToDoById(id: Long): ToDo? {
        return todoDao.getToDoById(id)
    }

    suspend fun getAllToDosSync(): List<ToDo> {
        return todoDao.getAllToDosSync()
    }

    // ==================== HELPER METHODS ====================

    private fun filterTodayClasses(classes: List<Class>): List<Class> {
        val today = Calendar.getInstance()
        return classes.filter { classItem ->
            if (classItem.isRecurring) {
                val todayDayOfWeek = today.get(Calendar.DAY_OF_WEEK)
                classItem.daysOfWeek.contains(todayDayOfWeek)
            } else {
                val classDate = Calendar.getInstance().apply { timeInMillis = classItem.startDate.time }
                classDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                classDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            }
        }
    }

    private fun filterWeekClasses(classes: List<Class>): List<Class> {
        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val endOfWeek = startOfWeek.clone() as Calendar
        endOfWeek.add(Calendar.DAY_OF_WEEK, 7)

        return classes.filter { classItem ->
            if (classItem.isRecurring) {
                true // Recurring classes are always in the week
            } else {
                val classTime = classItem.startDate.time
                classTime >= startOfWeek.timeInMillis && classTime < endOfWeek.timeInMillis
            }
        }
    }

    private fun filterTodayMeetings(meetings: List<Meeting>): List<Meeting> {
        val today = Calendar.getInstance()
        return meetings.filter { meeting ->
            val meetingDate = Calendar.getInstance().apply { timeInMillis = meeting.startDate.time }
            meetingDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            meetingDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }
    }

    private fun filterUpcomingMeetings(meetings: List<Meeting>): List<Meeting> {
        val now = System.currentTimeMillis()
        val nextWeek = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7)
        }.timeInMillis

        return meetings.filter { meeting ->
            val startTime = meeting.getStartDateTime()
            startTime > now && startTime <= nextWeek
        }
    }

    private fun filterWeekMeetings(meetings: List<Meeting>): List<Meeting> {
        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val endOfWeek = startOfWeek.clone() as Calendar
        endOfWeek.add(Calendar.DAY_OF_WEEK, 7)

        return meetings.filter { meeting ->
            val meetingTime = meeting.startDate.time
            meetingTime >= startOfWeek.timeInMillis && meetingTime < endOfWeek.timeInMillis
        }
    }

    // ==================== DATE RANGE QUERIES (FOR TIMETABLE) ====================

    suspend fun getClassesForDateRange(startTime: Long, endTime: Long): List<Class> {
        val allClasses = classDao.getClassesForDateRange(startTime, endTime)
        return allClasses.filter { classItem ->
            if (classItem.isRecurring) {
                // For recurring classes, check if any day in the range matches the days of week
                val startCal = Calendar.getInstance().apply { timeInMillis = startTime }
                val endCal = Calendar.getInstance().apply { timeInMillis = endTime }

                val currentCal = startCal.clone() as Calendar
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

    // ==================== SETTINGS ====================

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

    // ==================== SYNCHRONIZATION ====================

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
