package com.example.teacherscheduler.firebase

import android.util.Log
import com.example.teacherscheduler.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseService @Inject constructor() {
    companion object {
        private const val TAG = "FirebaseService"
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_CLASSES = "classes"
        private const val COLLECTION_MEETINGS = "meetings"
        private const val COLLECTION_TASKS = "tasks"
        private const val COLLECTION_NOTICES = "notices"
    }

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val userId: String?
        get() = auth.currentUser?.uid

    fun isSignedIn(): Boolean = auth.currentUser != null

    // Authentication
    suspend fun signIn(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed: ${e.message}")
            false
        }
    }
    
    suspend fun signUp(email: String, password: String): Boolean {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Sign up failed: ${e.message}")
            false
        }
    }

    fun signOut() {
        auth.signOut()
    }

    // User Profile
    suspend fun saveUserProfile(name: String, email: String, role: String, department: String): Boolean {
        val uid = userId ?: return false
        return try {
            firestore.collection(COLLECTION_USERS).document(uid).set(mapOf(
                "name" to name,
                "email" to email,
                "role" to role,
                "department" to department,
                "teacherId" to uid
            )).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user profile: ${e.message}")
            false
        }
    }

    suspend fun getUserProfile(): Map<String, Any>? {
        val uid = userId ?: return null
        return try {
            firestore.collection(COLLECTION_USERS).document(uid).get().await().data
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user profile: ${e.message}")
            null
        }
    }

    // Classes
    suspend fun syncClass(classItem: Class): Boolean {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return false
        return try {
            val docRef = if (classItem.id == 0L) {
                firestore.collection(COLLECTION_CLASSES).document()
            } else {
                firestore.collection(COLLECTION_CLASSES).document(classItem.id.toString())
            }

            docRef.set(mapOf(
                "subject" to classItem.subject,
                "department" to classItem.department,
                "room" to classItem.roomNumber,
                "date" to Timestamp(classItem.startDate),
                "startTime" to Timestamp(classItem.startTime),
                "endTime" to Timestamp(classItem.endTime),
                "teacherId" to uid,
                "createdAt" to Timestamp.now()
            )).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync class: ${e.message}")
            false
        }
    }

    suspend fun getClasses(): List<Class> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = firestore.collection(COLLECTION_CLASSES)
                .whereEqualTo("teacherId", uid)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                mapDocumentToClass(doc.id, doc.data)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch classes: ${e.message}")
            emptyList()
        }
    }

    fun getClassesFlow(): Flow<List<Class>> = callbackFlow {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_CLASSES)
            .whereEqualTo("teacherId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Classes listener error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val classes = snapshot?.documents?.mapNotNull { doc ->
                    mapDocumentToClass(doc.id, doc.data)
                } ?: emptyList()

                trySend(classes)
            }

        awaitClose { listener.remove() }
    }

    private fun mapDocumentToClass(id: String, data: Map<String, Any>?): Class? {
        if (data == null) return null
        return try {
            Class(
                id = id.toLongOrNull() ?: System.currentTimeMillis(),
                subject = data["subject"] as? String ?: "",
                department = data["department"] as? String ?: "",
                roomNumber = data["room"] as? String ?: "",
                startDate = (data["date"] as? Timestamp)?.toDate() ?: Date(),
                endDate = (data["date"] as? Timestamp)?.toDate() ?: Date(),
                startTime = (data["startTime"] as? Timestamp)?.toDate() ?: Date(),
                endTime = (data["endTime"] as? Timestamp)?.toDate() ?: Date(),
                isRecurring = false,
                isActive = true,
                notificationsEnabled = true,
                reminderMinutes = 15,
                daysOfWeek = emptyList(),
                description = "",
                semesterId = 0L,
                createdAt = (data["createdAt"] as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
                lastSyncTimestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping class: ${e.message}")
            null
        }
    }

    suspend fun deleteClass(classId: Long): Boolean {
        return try {
            firestore.collection(COLLECTION_CLASSES).document(classId.toString()).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete class: ${e.message}")
            false
        }
    }

    // Meetings
    suspend fun syncMeeting(meeting: Meeting): Boolean {
        val uid = userId ?: return false
        return try {
            val docRef = if (meeting.id == 0L) {
                firestore.collection(COLLECTION_MEETINGS).document()
            } else {
                firestore.collection(COLLECTION_MEETINGS).document(meeting.id.toString())
            }

            docRef.set(mapOf(
                "id" to meeting.id,
                "title" to meeting.title,
                "withWhom" to meeting.withWhom,
                "location" to meeting.location,
                "notes" to meeting.notes,
                "startDate" to Timestamp(meeting.startDate),
                "endDate" to Timestamp(meeting.endDate),
                "startTime" to Timestamp(meeting.startTime),
                "endTime" to Timestamp(meeting.endTime),
                "isActive" to meeting.isActive,
                "notificationsEnabled" to meeting.notificationsEnabled,
                "reminderMinutes" to meeting.reminderMinutes,
                "semesterId" to meeting.semesterId,
                "teacherId" to uid,
                "createdAt" to Timestamp(Date(meeting.createdAt)),
                "lastSyncTimestamp" to Timestamp(Date())
            )).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync meeting: ${e.message}")
            false
        }
    }

    suspend fun getMeetings(): List<Meeting> {
        val uid = userId ?: return emptyList()
        return try {
            val snapshot = firestore.collection(COLLECTION_MEETINGS)
                .whereEqualTo("teacherId", uid)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                mapDocumentToMeeting(doc.id, doc.data)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch meetings: ${e.message}")
            emptyList()
        }
    }

    fun getMeetingsFlow(): Flow<List<Meeting>> = callbackFlow {
        val uid = userId
        if (uid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_MEETINGS)
            .whereEqualTo("teacherId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Meetings listener error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val meetings = snapshot?.documents?.mapNotNull { doc ->
                    mapDocumentToMeeting(doc.id, doc.data)
                } ?: emptyList()

                trySend(meetings)
            }

        awaitClose { listener.remove() }
    }

    private fun mapDocumentToMeeting(id: String, data: Map<String, Any>?): Meeting? {
        if (data == null) return null
        return try {
            Meeting(
                id = (data["id"] as? Long) ?: id.toLongOrNull() ?: 0L,
                title = data["title"] as? String ?: "",
                withWhom = data["withWhom"] as? String ?: "",
                location = data["location"] as? String ?: "",
                notes = data["notes"] as? String ?: "",
                startDate = (data["startDate"] as? Timestamp)?.toDate() ?: Date(),
                endDate = (data["endDate"] as? Timestamp)?.toDate() ?: Date(),
                startTime = (data["startTime"] as? Timestamp)?.toDate() ?: Date(),
                endTime = (data["endTime"] as? Timestamp)?.toDate() ?: Date(),
                isActive = data["isActive"] as? Boolean ?: true,
                notificationsEnabled = data["notificationsEnabled"] as? Boolean ?: true,
                reminderMinutes = (data["reminderMinutes"] as? Long)?.toInt() ?: 15,
                semesterId = (data["semesterId"] as? Long) ?: 0L,
                createdAt = (data["createdAt"] as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
                lastSyncTimestamp = (data["lastSyncTimestamp"] as? Timestamp)?.toDate()?.time ?: 0L
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping meeting: ${e.message}")
            null
        }
    }

    suspend fun deleteMeeting(meetingId: Long): Boolean {
        return try {
            firestore.collection(COLLECTION_MEETINGS).document(meetingId.toString()).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete meeting: ${e.message}")
            false
        }
    }

    // Tasks
    suspend fun syncTask(task: ToDo): Boolean {
        val uid = userId ?: return false
        return try {
            val docRef = if (task.id == 0L) {
                firestore.collection(COLLECTION_TASKS).document()
            } else {
                firestore.collection(COLLECTION_TASKS).document(task.id.toString())
            }

            docRef.set(mapOf(
                "id" to task.id,
                "title" to task.title,
                "description" to task.description,
                "category" to task.category,
                "priority" to task.priority.name,
                "dueDate" to (task.dueDate?.let { Timestamp(it) }),
                "reminderTime" to (task.reminderTime?.let { Timestamp(it) }),
                "isCompleted" to task.isCompleted,
                "completedAt" to task.completedAt,
                "notificationsEnabled" to task.notificationsEnabled,
                "reminderMinutes" to task.reminderMinutes,
                "tags" to task.tags,
                "isActive" to task.isActive,
                "semesterId" to task.semesterId,
                "createdAt" to Timestamp(Date(task.createdAt)),
                "lastSyncTimestamp" to Timestamp(Date()),
                "taskType" to task.taskType.name,
                "isDepartmentTask" to task.isDepartmentTask,
                "assignedBy" to task.assignedBy,
                "assignedByName" to task.assignedByName,
                "assignedTo" to task.assignedTo,
                "assignedToName" to task.assignedToName,
                "isBulkTask" to task.isBulkTask,
                "departmentId" to task.departmentId,
                "completionNotes" to task.completionNotes,
                "status" to task.status.name,
                "startedAt" to task.startedAt,
                "overdueNotificationSent" to task.overdueNotificationSent,
                "lastStatusChange" to task.lastStatusChange,
                "teacherId" to uid // Keep for security rules
            )).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync task: ${e.message}")
            false
        }
    }

    fun getTasksFlow(): Flow<List<ToDo>> = callbackFlow {
        val uid = userId
        if (uid == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        val listener = firestore.collection(COLLECTION_TASKS)
            .whereEqualTo("teacherId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Tasks listener error: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents?.mapNotNull { doc ->
                    mapDocumentToTask(doc.id, doc.data)
                } ?: emptyList()

                trySend(tasks)
            }

        awaitClose { listener.remove() }
    }

    private fun mapDocumentToTask(id: String, data: Map<String, Any>?): ToDo? {
        if (data == null) return null
        return try {
            ToDo(
                id = (data["id"] as? Long) ?: id.toLongOrNull() ?: 0L,
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                category = data["category"] as? String ?: "",
                priority = ToDo.Priority.valueOf(data["priority"] as? String ?: "MEDIUM"),
                dueDate = (data["dueDate"] as? Timestamp)?.toDate(),
                reminderTime = (data["reminderTime"] as? Timestamp)?.toDate(),
                isCompleted = data["isCompleted"] as? Boolean ?: false,
                completedAt = data["completedAt"] as? Long,
                notificationsEnabled = data["notificationsEnabled"] as? Boolean ?: true,
                reminderMinutes = (data["reminderMinutes"] as? Long)?.toInt() ?: 15,
                tags = (data["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                isActive = data["isActive"] as? Boolean ?: true,
                semesterId = (data["semesterId"] as? Long) ?: 0L,
                createdAt = (data["createdAt"] as? Timestamp)?.toDate()?.time ?: System.currentTimeMillis(),
                lastSyncTimestamp = (data["lastSyncTimestamp"] as? Timestamp)?.toDate()?.time ?: 0L,
                taskType = TaskType.valueOf(data["taskType"] as? String ?: "PERSONAL"),
                isDepartmentTask = data["isDepartmentTask"] as? Boolean ?: false,
                assignedBy = data["assignedBy"] as? String ?: "",
                assignedByName = data["assignedByName"] as? String ?: "",
                assignedTo = data["assignedTo"] as? String ?: "",
                assignedToName = data["assignedToName"] as? String ?: "",
                isBulkTask = data["isBulkTask"] as? Boolean ?: false,
                departmentId = (data["departmentId"] as? Long) ?: 0L,
                completionNotes = data["completionNotes"] as? String ?: "",
                status = TaskStatus.valueOf(data["status"] as? String ?: "ASSIGNED"),
                startedAt = data["startedAt"] as? Long,
                overdueNotificationSent = data["overdueNotificationSent"] as? Boolean ?: false,
                lastStatusChange = data["lastStatusChange"] as? Long ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping task: ${e.message}")
            null
        }
    }

    suspend fun deleteTask(taskId: Long): Boolean {
        return try {
            firestore.collection(COLLECTION_TASKS).document(taskId.toString()).delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete task: ${e.message}")
            false
        }
    }

    // Notices
    suspend fun syncNotice(notice: Notice): Boolean {
        return try {
            val docRef = if (notice.id == 0L) {
                firestore.collection(COLLECTION_NOTICES).document()
            } else {
                firestore.collection(COLLECTION_NOTICES).document(notice.id.toString())
            }

            docRef.set(mapOf(
                "id" to notice.id,
                "title" to notice.title,
                "message" to notice.message,
                "priority" to notice.priority.name,
                "createdBy" to notice.createdBy,
                "createdByName" to notice.createdByName,
                "departmentId" to notice.departmentId,
                "publishedAt" to Timestamp(Date(notice.publishedAt)),
                "isActive" to notice.isActive
            )).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync notice: ${e.message}")
            false
        }
    }
}
