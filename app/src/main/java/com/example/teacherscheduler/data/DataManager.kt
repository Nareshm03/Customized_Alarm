package com.example.teacherscheduler.data

import android.content.Context
import android.util.Log
import com.example.teacherscheduler.model.AppSettings
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.ClassItem
import com.example.teacherscheduler.model.Meeting
import com.example.teacherscheduler.model.MeetingItem
import com.example.teacherscheduler.model.UserProfile
import com.example.teacherscheduler.notification.EnhancedNotificationHelper
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

object DataManager {
    private val TAG = "DataManager"
    private val classes = mutableListOf<Class>()
    private val meetings = mutableListOf<Meeting>()
    private var nextClassId = 1L
    private var nextMeetingId = 1L
    private lateinit var notificationHelper: EnhancedNotificationHelper
    private lateinit var settingsManager: SettingsManager
    private lateinit var firestoreManager: FirestoreManager
    private lateinit var userProfile: UserProfile

    private var currentSettings = AppSettings()
    private var lastSyncTime: Date? = null

    fun initialize(context: Context) {
        notificationHelper = EnhancedNotificationHelper(context)
        settingsManager = SettingsManager(context)
        firestoreManager = FirestoreManager(context)
        currentSettings = settingsManager.getSettings()
        
        // Initialize user profile with default values
        userProfile = UserProfile(
            id = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            email = FirebaseAuth.getInstance().currentUser?.email ?: "",
            name = "",
            phone = "",
            designation = "",
            teacherId = "",
            gender = "",
            department = "",
            officeLocation = "",
            profilePictureUrl = ""
        )

        addSampleData()
    }

    fun applySettings(settings: AppSettings) {
        currentSettings = settings
    }

    private fun addSampleData() {
        if (classes.isEmpty()) {
            // Add sample class
            val sampleClass = Class(
                subject = "Sample Mathematics",
                department = "Math Department",
                roomNumber = "101",
                startDate = Calendar.getInstance().time,
                endDate = Calendar.getInstance().time,
                startTime = createTime(10, 14),
                endTime = createTime(11, 8),
                daysOfWeek = listOf(1, 3, 5), // Mon, Wed, Fri
                isRecurring = true,
                reminderMinutes = 15
            )
            addClass(sampleClass)
        }
    }

    private fun createTime(hour: Int, minute: Int): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, hour)
        cal.set(Calendar.MINUTE, minute)
        cal.set(Calendar.SECOND, 0)
        return cal.time
    }



    // Class management
    fun addClass(classItem: Class): Long {
        val newClass = classItem.copy(
            id = nextClassId++,
            reminderMinutes = if (classItem.reminderMinutes == 0) 15 else classItem.reminderMinutes
        )
        classes.add(newClass)

        // Schedule notifications if enabled
        if (currentSettings.classNotificationsEnabled && newClass.notificationsEnabled && ::notificationHelper.isInitialized) {
            notificationHelper.scheduleClassNotifications(newClass)
        }

        return newClass.id
    }

    fun getActiveClasses(): List<Class> {
        return classes.filter { it.isActive }
    }

    fun updateClass(classItem: Class) {
        val index = classes.indexOfFirst { it.id == classItem.id }
        if (index != -1) {
            // Cancel old notifications if helper is initialized
            if (::notificationHelper.isInitialized) {
                notificationHelper.cancelNotifications(classItem.id, EnhancedNotificationHelper.TYPE_CLASS)
            }

            // Update class
            classes[index] = classItem

            // Schedule new notifications if enabled and helper is initialized
            if (currentSettings.classNotificationsEnabled && classItem.notificationsEnabled && ::notificationHelper.isInitialized) {
                notificationHelper.scheduleClassNotifications(classItem)
            }
        }
    }

    fun deleteClass(classId: Long) {
        // Cancel notifications if helper is initialized
        if (::notificationHelper.isInitialized) {
            notificationHelper.cancelNotifications(classId, EnhancedNotificationHelper.TYPE_CLASS)
        }

        // Remove class
        classes.removeAll { it.id == classId }
    }

    // Meeting management
    fun addMeeting(meeting: Meeting): Long {
        val newMeeting = meeting.copy(
            id = nextMeetingId++,
            reminderMinutes = if (meeting.reminderMinutes == 0) 15 else meeting.reminderMinutes
        )
        meetings.add(newMeeting)

        // Schedule notifications if enabled and helper is initialized
        if (currentSettings.meetingNotificationsEnabled && newMeeting.notificationsEnabled && ::notificationHelper.isInitialized) {
            notificationHelper.scheduleMeetingNotifications(newMeeting)
        }

        return newMeeting.id
    }

    fun getActiveMeetings(): List<Meeting> {
        return meetings.filter { it.isActive }
    }

    fun updateMeeting(meeting: Meeting) {
        val index = meetings.indexOfFirst { it.id == meeting.id }
        if (index != -1) {
            // Cancel old notifications if helper is initialized
            if (::notificationHelper.isInitialized) {
                notificationHelper.cancelNotifications(meeting.id, EnhancedNotificationHelper.TYPE_MEETING)
            }

            // Update meeting
            meetings[index] = meeting

            // Schedule new notifications if enabled and helper is initialized
            if (currentSettings.meetingNotificationsEnabled && meeting.notificationsEnabled && ::notificationHelper.isInitialized) {
                notificationHelper.scheduleMeetingNotifications(meeting)
            }
        }
    }

    fun deleteMeeting(meetingId: Long) {
        // Cancel notifications if helper is initialized
        if (::notificationHelper.isInitialized) {
            notificationHelper.cancelNotifications(meetingId, EnhancedNotificationHelper.TYPE_MEETING)
        }

        // Remove meeting
        meetings.removeAll { it.id == meetingId }
    }

    // Getter methods
    fun getAllClasses(): List<Class> = getActiveClasses()
    fun getAllMeetings(): List<Meeting> = getActiveMeetings()
    fun getClassById(id: Long): Class? = classes.find { it.id == id }
    fun getMeetingById(id: Long): Meeting? = meetings.find { it.id == id }

    // User profile methods
    fun getUserProfile(): UserProfile = userProfile
    
    fun updateUserProfile(profile: UserProfile) {
        userProfile = profile
    }

    // Cloud sync methods
    suspend fun performCloudSync(): Boolean = withContext(Dispatchers.IO) {
        if (!firestoreManager.isUserLoggedIn()) {
            Log.d(TAG, "User not logged in, cannot sync")
            return@withContext false
        }

        try {
            Log.d(TAG, "Starting cloud sync")
            
            // Convert data to Firestore-compatible models
            val classItems = classes.map { it.toClassItem() }
            val meetingItems = meetings.map { it.toMeetingItem() }
            
            // Create settings map
            val settingsMap = mapOf(
                "classNotificationsEnabled" to currentSettings.classNotificationsEnabled,
                "meetingNotificationsEnabled" to currentSettings.meetingNotificationsEnabled,
                "reminderTime" to currentSettings.reminderTime,
                "autoSync" to currentSettings.autoSync,
                "notificationSound" to currentSettings.notificationSound,
                "notificationVibration" to currentSettings.notificationVibration,
                "advanceNotification" to currentSettings.advanceNotification,
                "theme" to currentSettings.theme,
                "currentSemesterId" to currentSettings.currentSemesterId,
                "lastSyncTimestamp" to currentSettings.lastSyncTimestamp
            )
            
            // Sync all data to Firestore
            val success = firestoreManager.syncAllData(
                profile = userProfile,
                classes = classItems,
                meetings = meetingItems,
                settings = settingsMap
            )
            
            if (success) {
                // Update last sync time
                lastSyncTime = Date()
                Log.d(TAG, "Cloud sync completed successfully")
            } else {
                Log.e(TAG, "Cloud sync failed")
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error during cloud sync", e)
            false
        }
    }

    suspend fun syncFromCloud(): Boolean = withContext(Dispatchers.IO) {
        if (!firestoreManager.isUserLoggedIn()) {
            Log.d(TAG, "User not logged in, cannot sync from cloud")
            return@withContext false
        }

        try {
            Log.d(TAG, "Starting sync from cloud")
            
            // Get all data from Firestore
            val result = firestoreManager.restoreAllData()
            
            // Process user profile
            (result["profile"] as? UserProfile)?.let {
                userProfile = it
                Log.d(TAG, "User profile synced from cloud")
            }
            
            // Process classes
            @Suppress("UNCHECKED_CAST")
            (result["classes"] as? List<ClassItem>)?.let { cloudClasses ->
                // Clear existing classes and add cloud classes
                classes.clear()
                cloudClasses.forEach { cloudClass ->
                    classes.add(cloudClass.toClass())
                }
                // Update next ID
                if (cloudClasses.isNotEmpty()) {
                    nextClassId = (cloudClasses.maxByOrNull { it.id }?.id ?: 0) + 1
                }
                Log.d(TAG, "Classes synced from cloud: ${cloudClasses.size}")
            }
            
            // Process meetings
            @Suppress("UNCHECKED_CAST")
            (result["meetings"] as? List<MeetingItem>)?.let { cloudMeetings ->
                // Clear existing meetings and add cloud meetings
                meetings.clear()
                cloudMeetings.forEach { cloudMeeting ->
                    meetings.add(cloudMeeting.toMeeting())
                }
                // Update next ID
                if (cloudMeetings.isNotEmpty()) {
                    nextMeetingId = (cloudMeetings.maxByOrNull { it.id }?.id ?: 0) + 1
                }
                Log.d(TAG, "Meetings synced from cloud: ${cloudMeetings.size}")
            }
            // Process settings
            @Suppress("UNCHECKED_CAST")
            (result["settings"] as? Map<String, Any>)?.let { cloudSettings ->
                // Update settings
                currentSettings = AppSettings(
                    classNotificationsEnabled = cloudSettings["classNotificationsEnabled"] as? Boolean ?: true,
                    meetingNotificationsEnabled = cloudSettings["meetingNotificationsEnabled"] as? Boolean ?: true,
                    reminderTime = cloudSettings["reminderTime"] as? Int ?: 15,
                    autoSync = cloudSettings["autoSync"] as? Boolean ?: false,
                    notificationSound = cloudSettings["notificationSound"] as? Boolean ?: true,
                    notificationVibration = cloudSettings["notificationVibration"] as? Boolean ?: true,
                    advanceNotification = cloudSettings["advanceNotification"] as? Boolean ?: true,
                    theme = cloudSettings["theme"] as? Int ?: 0,
                    currentSemesterId = cloudSettings["currentSemesterId"] as? Long ?: 0L
                )
                // Save settings to local storage
                settingsManager.saveSettings(currentSettings)
                Log.d(TAG, "Settings synced from cloud")
            }
            
            // Update last sync time
            lastSyncTime = Date()
            Log.d(TAG, "Sync from cloud completed successfully")
            
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync from cloud", e)
            false
        }
    }
    
    fun getLastSyncTimeFormatted(): String {
        return if (lastSyncTime != null) {
            SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(lastSyncTime!!)
        } else {
            "Never"
        }
    }
    
    // Extension functions to convert between model types
    private fun Class.toClassItem(): ClassItem {
        return ClassItem(
            id = this.id,
            subject = this.subject,
            department = this.department,
            roomNumber = this.roomNumber,
            startDate = this.startDate,
            endDate = this.endDate,
            startTime = this.startTime,
            endTime = this.endTime,
            daysOfWeek = this.daysOfWeek,
            isRecurring = this.isRecurring,
            notificationsEnabled = this.notificationsEnabled,
            reminderMinutes = this.reminderMinutes,
            description = this.description,
            semesterId = this.semesterId
        )
    }
    
    private fun ClassItem.toClass(): Class {
        return Class(
            id = this.id,
            subject = this.subject,
            department = this.department,
            roomNumber = this.roomNumber,
            startDate = this.startDate,
            endDate = this.endDate,
            startTime = this.startTime,
            endTime = this.endTime,
            daysOfWeek = this.daysOfWeek,
            isRecurring = this.isRecurring,
            notificationsEnabled = this.notificationsEnabled,
            reminderMinutes = this.reminderMinutes,
            description = this.description,
            semesterId = this.semesterId
        )
    }
    
    private fun Meeting.toMeetingItem(): MeetingItem {
        return MeetingItem(
            id = this.id,
            title = this.title,
            with = this.withWhom, // Map withWhom to with for Firestore
            location = this.location,
            date = this.startDate, // Map startDate to date for Firestore
            startTime = this.startTime,
            endTime = this.endTime,
            notificationsEnabled = this.notificationsEnabled,
            reminderMinutes = this.reminderMinutes,
            notes = this.notes,
            semesterId = this.semesterId
        )
    }
    
    private fun MeetingItem.toMeeting(): Meeting {
        return Meeting(
            id = this.id,
            title = this.title,
            withWhom = this.with, // Map with to withWhom for local model
            with = this.with, // Keep the with field in sync
            location = this.location,
            startDate = this.date, // Map date to startDate for local model
            endDate = this.date, // Use the same date for endDate
            date = this.date, // Keep the date field in sync
            startTime = this.startTime,
            endTime = this.endTime,
            notificationsEnabled = this.notificationsEnabled,
            reminderMinutes = this.reminderMinutes,
            notes = this.notes,
            semesterId = this.semesterId
        )
    }
}