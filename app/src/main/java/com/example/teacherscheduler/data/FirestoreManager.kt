package com.example.teacherscheduler.data

import android.content.Context
import android.util.Log
import com.example.teacherscheduler.model.ClassItem
import com.example.teacherscheduler.model.MeetingItem
import com.example.teacherscheduler.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * Manager class for handling all Firestore operations
 */
class FirestoreManager(private val context: Context) {
    private val TAG = "FirestoreManager"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Collection references
    private val usersCollection = db.collection("users")
    
    /**
     * Get the current user's document reference
     */
    private fun getUserDocRef() = auth.currentUser?.uid?.let { usersCollection.document(it) }
    
    /**
     * Check if the user is logged in
     */
    fun isUserLoggedIn() = auth.currentUser != null
    
    /**
     * Get the current user's ID
     */
    fun getCurrentUserId() = auth.currentUser?.uid
    
    /**
     * Get the current user's email
     */
    fun getCurrentUserEmail() = auth.currentUser?.email
    
    /**
     * Sync user profile to Firestore
     */
    suspend fun syncUserProfile(profile: UserProfile): Boolean {
        return try {
            val userDocRef = getUserDocRef() ?: return false
            userDocRef.set(profile, SetOptions.merge()).await()
            Log.d(TAG, "User profile synced successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user profile", e)
            false
        }
    }
    
    /**
     * Get user profile from Firestore
     */
    suspend fun getUserProfile(): UserProfile? {
        return try {
            val userDocRef = getUserDocRef() ?: return null
            val document = userDocRef.get().await()
            if (document.exists()) {
                val profile = document.toObject(UserProfile::class.java)
                Log.d(TAG, "User profile retrieved successfully")
                profile
            } else {
                Log.d(TAG, "User profile does not exist")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile", e)
            null
        }
    }
    
    /**
     * Sync classes to Firestore
     */
    suspend fun syncClasses(classes: List<ClassItem>): Boolean {
        return try {
            val userDocRef = getUserDocRef() ?: return false
            val classesCollection = userDocRef.collection("classes")
            
            // First, delete all existing classes
            val existingClasses = classesCollection.get().await()
            for (document in existingClasses) {
                document.reference.delete().await()
            }
            
            // Then, add all current classes
            for (classItem in classes) {
                classesCollection.document(classItem.id.toString()).set(classItem).await()
            }
            
            Log.d(TAG, "Classes synced successfully: ${classes.size} classes")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing classes", e)
            false
        }
    }
    
    /**
     * Get classes from Firestore
     */
    suspend fun getClasses(): List<ClassItem> {
        return try {
            val userDocRef = getUserDocRef() ?: return emptyList()
            val classesCollection = userDocRef.collection("classes")
            val documents = classesCollection.get().await()
            
            val classes = documents.mapNotNull { it.toObject(ClassItem::class.java) }
            Log.d(TAG, "Retrieved ${classes.size} classes from Firestore")
            classes
        } catch (e: Exception) {
            Log.e(TAG, "Error getting classes", e)
            emptyList()
        }
    }
    
    /**
     * Sync meetings to Firestore
     */
    suspend fun syncMeetings(meetings: List<MeetingItem>): Boolean {
        return try {
            val userDocRef = getUserDocRef() ?: return false
            val meetingsCollection = userDocRef.collection("meetings")
            
            // First, delete all existing meetings
            val existingMeetings = meetingsCollection.get().await()
            for (document in existingMeetings) {
                document.reference.delete().await()
            }
            
            // Then, add all current meetings
            for (meeting in meetings) {
                meetingsCollection.document(meeting.id.toString()).set(meeting).await()
            }
            
            Log.d(TAG, "Meetings synced successfully: ${meetings.size} meetings")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing meetings", e)
            false
        }
    }
    
    /**
     * Get meetings from Firestore
     */
    suspend fun getMeetings(): List<MeetingItem> {
        return try {
            val userDocRef = getUserDocRef() ?: return emptyList()
            val meetingsCollection = userDocRef.collection("meetings")
            val documents = meetingsCollection.get().await()
            
            val meetings = documents.mapNotNull { it.toObject(MeetingItem::class.java) }
            Log.d(TAG, "Retrieved ${meetings.size} meetings from Firestore")
            meetings
        } catch (e: Exception) {
            Log.e(TAG, "Error getting meetings", e)
            emptyList()
        }
    }
    


    /**
     * Sync settings to Firestore
     */
    suspend fun syncSettings(settings: Map<String, Any>): Boolean {
        return try {
            val userDocRef = getUserDocRef() ?: return false
            userDocRef.collection("settings").document("app_settings")
                .set(settings, SetOptions.merge()).await()
            
            Log.d(TAG, "Settings synced successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing settings", e)
            false
        }
    }
    
    /**
     * Get settings from Firestore
     */
    suspend fun getSettings(): Map<String, Any>? {
        return try {
            val userDocRef = getUserDocRef() ?: return null
            val document = userDocRef.collection("settings").document("app_settings").get().await()
            
            if (document.exists()) {
                val settings = document.data
                Log.d(TAG, "Settings retrieved successfully")
                settings
            } else {
                Log.d(TAG, "Settings do not exist")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting settings", e)
            null
        }
    }
    
    /**
     * Update last sync time
     */
    suspend fun updateLastSyncTime(): Boolean {
        return try {
            val userDocRef = getUserDocRef() ?: return false
            val lastSyncData = hashMapOf(
                "lastSyncTime" to Date(),
                "deviceId" to android.os.Build.MODEL
            )
            
            userDocRef.collection("sync_info").document("last_sync")
                .set(lastSyncData, SetOptions.merge()).await()
            
            Log.d(TAG, "Last sync time updated successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating last sync time", e)
            false
        }
    }
    
    /**
     * Get last sync time
     */
    suspend fun getLastSyncTime(): Date? {
        return try {
            val userDocRef = getUserDocRef() ?: return null
            val document = userDocRef.collection("sync_info").document("last_sync").get().await()
            
            if (document.exists() && document.contains("lastSyncTime")) {
                val timestamp = document.getTimestamp("lastSyncTime")
                Log.d(TAG, "Last sync time retrieved successfully")
                timestamp?.toDate()
            } else {
                Log.d(TAG, "Last sync time does not exist")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last sync time", e)
            null
        }
    }
    
    /**
     * Sync all data to Firestore
     */
    suspend fun syncAllData(
        profile: UserProfile,
        classes: List<ClassItem>,
        meetings: List<MeetingItem>,
        settings: Map<String, Any>
    ): Boolean {
        var success = true
        
        if (!syncUserProfile(profile)) success = false
        if (!syncClasses(classes)) success = false
        if (!syncMeetings(meetings)) success = false
        if (!syncSettings(settings)) success = false
        if (success) updateLastSyncTime()
        
        return success
    }
    
    /**
     * Restore all data from Firestore
     */
    suspend fun restoreAllData(): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        
        result["profile"] = getUserProfile()
        result["classes"] = getClasses()
        result["meetings"] = getMeetings()
        result["settings"] = getSettings()
        
        return result
    }
}