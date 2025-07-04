package com.example.teacherscheduler.data.remote

import android.util.Log
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.*

class FirebaseService {
    companion object {
        private const val TAG = "FirebaseService"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_CLASSES = "classes"
        private const val COLLECTION_MEETINGS = "meetings"
    }
    
    private val auth: FirebaseAuth by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize FirebaseAuth: ${e.message}")
            throw e
        }
    }
    
    private val firestore: FirebaseFirestore by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize Firestore: ${e.message}")
            throw e
        }
    }

    private val userId: String?
        get() = try { 
            auth.currentUser?.uid 
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get current user: ${e.message}")
            null
        }

    fun isSignedIn(): Boolean = try {
        auth.currentUser != null
    } catch (e: Exception) {
        Log.w(TAG, "Failed to check sign-in status: ${e.message}")
        false
    }

    // Authentication methods
    suspend fun signIn(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Sign in successful")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed: ${e.message}")
            false
        }
    }

    suspend fun signUp(email: String, password: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            Log.d(TAG, "Sign up successful")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Sign up failed: ${e.message}")
            false
        }
    }

    fun signOut() {
        auth.signOut()
        Log.d(TAG, "User signed out")
    }



    // Class cloud operations
    suspend fun syncClass(classItem: Class): Boolean {
        val userId = userId ?: return false

        return try {
            val classData = hashMapOf(

                "subject" to classItem.subject,
                "department" to classItem.department,
                "roomNumber" to classItem.roomNumber,
                "startDate" to classItem.startDate,
                "endDate" to classItem.endDate,
                "startTime" to classItem.startTime,
                "endTime" to classItem.endTime,
                "daysOfWeek" to classItem.daysOfWeek,
                "isRecurring" to classItem.isRecurring,
                "isActive" to classItem.isActive,
                "notificationEnabled" to classItem.notificationsEnabled,
                "reminderMinutes" to classItem.reminderMinutes,
                "createdAt" to classItem.createdAt,
                "lastSyncTimestamp" to System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_CLASSES)
                .document(classItem.id.toString())
                .set(classData, SetOptions.merge())
                .await()

            Log.d(TAG, "Class synced: ${classItem.subject}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync class: ${e.message}")
            false
        }
    }

    suspend fun getClasses(): List<Class> {
        val userId = userId ?: return emptyList()

        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_CLASSES)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    Class(
                        id = doc.id.toLongOrNull() ?: 0,
                        subject = doc.getString("subject") ?: "",
                        department = doc.getString("department") ?: "",
                        roomNumber = doc.getString("roomNumber") ?: "",
                        startDate = doc.getDate("startDate") ?: Date(),
                        endDate = doc.getDate("endDate") ?: Date(),
                        startTime = doc.getDate("startTime") ?: Date(),
                        endTime = doc.getDate("endTime") ?: Date(),
                        daysOfWeek = try {
                            @Suppress("UNCHECKED_CAST")
                            (doc.get("daysOfWeek") as? List<Long>)?.map { it.toInt() } ?: listOf()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing daysOfWeek", e)
                            listOf()
                        },
                        isRecurring = doc.getBoolean("isRecurring") == true,
                        isActive = doc.getBoolean("isActive") == true,
                        notificationsEnabled = doc.getBoolean("notificationEnabled") == true,
                        reminderMinutes = doc.getLong("reminderMinutes")?.toInt() ?: 15,
                        createdAt = doc.getLong("createdAt") ?: 0,
                        lastSyncTimestamp = doc.getLong("lastSyncTimestamp") ?: 0
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing class: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get classes: ${e.message}")
            emptyList()
        }
    }

    // Meeting cloud operations
    suspend fun syncMeeting(meeting: Meeting): Boolean {
        val userId = userId ?: return false

        return try {
            val meetingData = hashMapOf(

                "title" to meeting.title,
                "withWhom" to meeting.withWhom,
                "location" to meeting.location,
                "notes" to meeting.notes,
                "startDate" to meeting.startDate,
                "endDate" to meeting.endDate,
                "startTime" to meeting.startTime,
                "endTime" to meeting.endTime,
                "isActive" to meeting.isActive,
                "notificationEnabled" to meeting.notificationsEnabled,
                "reminderMinutes" to meeting.reminderMinutes,
                "createdAt" to meeting.createdAt,
                "lastSyncTimestamp" to System.currentTimeMillis()
            )

            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_MEETINGS)
                .document(meeting.id.toString())
                .set(meetingData, SetOptions.merge())
                .await()

            Log.d(TAG, "Meeting synced: ${meeting.title}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync meeting: ${e.message}")
            false
        }
    }

    suspend fun getMeetings(): List<Meeting> {
        val userId = userId ?: return emptyList()

        return try {
            val snapshot = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_MEETINGS)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    Meeting(
                        id = doc.id.toLongOrNull() ?: 0,
                        title = doc.getString("title") ?: "",
                        withWhom = doc.getString("withWhom") ?: "",
                        location = doc.getString("location") ?: "",
                        notes = doc.getString("notes") ?: "",
                        startDate = doc.getDate("startDate") ?: Date(),
                        endDate = doc.getDate("endDate") ?: Date(),
                        startTime = doc.getDate("startTime") ?: Date(),
                        endTime = doc.getDate("endTime") ?: Date(),
                        isActive = doc.getBoolean("isActive") == true,
                        notificationsEnabled = doc.getBoolean("notificationEnabled") == true,
                        reminderMinutes = doc.getLong("reminderMinutes")?.toInt() ?: 15,
                        createdAt = doc.getLong("createdAt") ?: 0,
                        lastSyncTimestamp = doc.getLong("lastSyncTimestamp") ?: 0
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing meeting: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get meetings: ${e.message}")
            emptyList()
        }
    }

    // Delete operations
    suspend fun deleteClass(classId: Long): Boolean {
        val userId = userId ?: return false

        return try {
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_CLASSES)
                .document(classId.toString())
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete class: ${e.message}")
            false
        }
    }

    suspend fun deleteMeeting(meetingId: Long): Boolean {
        val userId = userId ?: return false

        return try {
            firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_MEETINGS)
                .document(meetingId.toString())
                .delete()
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete meeting: ${e.message}")
            false
        }
    }


}