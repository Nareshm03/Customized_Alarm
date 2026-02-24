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

class FirestoreManager(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val TAG = "FirestoreManager"

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun syncAllData(
        profile: UserProfile,
        classes: List<ClassItem>,
        meetings: List<MeetingItem>,
        settings: Map<String, Any>
    ): Boolean {
        val userId = getCurrentUserId() ?: return false
        return try {
            val batch = db.batch()
            val userDocRef = db.collection("users").document(userId)

            batch.set(userDocRef, profile, SetOptions.merge())

            val classesCollection = userDocRef.collection("classes")
            classes.forEach { classItem ->
                val classDoc = classesCollection.document(classItem.id.toString())
                batch.set(classDoc, classItem, SetOptions.merge())
            }

            val meetingsCollection = userDocRef.collection("meetings")
            meetings.forEach { meetingItem ->
                val meetingDoc = meetingsCollection.document(meetingItem.id.toString())
                batch.set(meetingDoc, meetingItem, SetOptions.merge())
            }

            val settingsDoc = userDocRef.collection("settings").document("user_settings")
            batch.set(settingsDoc, settings, SetOptions.merge())

            batch.commit().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing all data", e)
            false
        }
    }

    suspend fun restoreAllData(): Map<String, Any> {
        val userId = getCurrentUserId() ?: return emptyMap()
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            val profile = userDoc.toObject(UserProfile::class.java)

            val classesSnapshot = db.collection("users").document(userId).collection("classes").get().await()
            val classes = classesSnapshot.toObjects(ClassItem::class.java)

            val meetingsSnapshot = db.collection("users").document(userId).collection("meetings").get().await()
            val meetings = meetingsSnapshot.toObjects(MeetingItem::class.java)

            val settingsDoc = db.collection("users").document(userId).collection("settings").document("user_settings").get().await()
            val settings = settingsDoc.data ?: emptyMap()

            val result = mutableMapOf<String, Any>()
            if (profile != null) {
                result["profile"] = profile
            }
            result["classes"] = classes
            result["meetings"] = meetings
            result["settings"] = settings
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring all data", e)
            emptyMap()
        }
    }

    suspend fun getUserProfile(): UserProfile? {
        val userId = getCurrentUserId() ?: return null
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            userDoc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile", e)
            null
        }
    }

    suspend fun syncUserProfile(profile: UserProfile): Boolean {
        val userId = getCurrentUserId() ?: return false
        return try {
            db.collection("users").document(userId)
                .set(profile, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing user profile", e)
            false
        }
    }
}
