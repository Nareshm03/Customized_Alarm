package com.example.teacherscheduler.data

import android.content.Context
import android.util.Log
import com.example.teacherscheduler.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * FIXED: Top-level collections for scalable architecture
 * Structure:
 * - users/{userId} - User profiles with role & departmentId
 * - departments/{deptId} - Department info
 * - classes/{classId} - All classes with teacherId field
 * - meetings/{meetingId} - All meetings with teacherId field
 * - tasks/{taskId} - All tasks with teacherId field
 */
class FirestoreManager(private val context: Context) {
    private val TAG = "FirestoreManager"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Top-level collection references
    private val usersCollection = db.collection("users")
    private val departmentsCollection = db.collection("departments")
    private val classesCollection = db.collection("classes")
    private val meetingsCollection = db.collection("meetings")
    private val tasksCollection = db.collection("tasks")
    
    fun isUserLoggedIn() = auth.currentUser != null
    fun getCurrentUserId() = auth.currentUser?.uid
    fun getCurrentUserEmail() = auth.currentUser?.email
    
    // ==================== USER PROFILE ====================
    
    suspend fun syncUserProfile(profile: UserProfile): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            usersCollection.document(userId).set(profile, SetOptions.merge()).await()
            Log.d(TAG, "User profile synced")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing profile", e)
            false
        }
    }
    
    suspend fun getUserProfile(): UserProfile? {
        return try {
            val userId = getCurrentUserId() ?: return null
            val doc = usersCollection.document(userId).get().await()
            doc.toObject(UserProfile::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting profile", e)
            null
        }
    }
    
    // ==================== CLASSES ====================
    
    suspend fun syncClass(classItem: ClassItem): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            val firestoreClass = classItem.copy(teacherId = userId)
            classesCollection.document(classItem.id.toString()).set(firestoreClass).await()
            Log.d(TAG, "Class synced: ${classItem.subject}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing class", e)
            false
        }
    }
    
    suspend fun getClassesForUser(userId: String? = null): List<ClassItem> {
        return try {
            val targetUserId = userId ?: getCurrentUserId() ?: return emptyList()
            val docs = classesCollection.whereEqualTo("teacherId", targetUserId).get().await()
            docs.mapNotNull { it.toObject(ClassItem::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting classes", e)
            emptyList()
        }
    }
    
    suspend fun getClassesForDepartment(departmentId: String): List<ClassItem> {
        return try {
            val docs = classesCollection.whereEqualTo("departmentId", departmentId).get().await()
            docs.mapNotNull { it.toObject(ClassItem::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting department classes", e)
            emptyList()
        }
    }
    
    // ==================== MEETINGS ====================
    
    suspend fun syncMeeting(meeting: MeetingItem): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            val firestoreMeeting = meeting.copy(teacherId = userId)
            meetingsCollection.document(meeting.id.toString()).set(firestoreMeeting).await()
            Log.d(TAG, "Meeting synced: ${meeting.title}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing meeting", e)
            false
        }
    }
    
    suspend fun getMeetingsForUser(userId: String? = null): List<MeetingItem> {
        return try {
            val targetUserId = userId ?: getCurrentUserId() ?: return emptyList()
            val docs = meetingsCollection.whereEqualTo("teacherId", targetUserId).get().await()
            docs.mapNotNull { it.toObject(MeetingItem::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting meetings", e)
            emptyList()
        }
    }
    


    // ==================== TASKS ====================
    
    suspend fun syncTask(task: ToDo): Boolean {
        return try {
            val userId = getCurrentUserId() ?: return false
            val firestoreTask = if (task.assignedTo.isEmpty()) {
                task.copy(assignedTo = userId)
            } else task
            tasksCollection.document(task.id.toString()).set(firestoreTask).await()
            Log.d(TAG, "Task synced: ${task.title}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing task", e)
            false
        }
    }
    
    suspend fun getTasksForUser(userId: String? = null): List<ToDo> {
        return try {
            val targetUserId = userId ?: getCurrentUserId() ?: return emptyList()
            val docs = tasksCollection.whereEqualTo("assignedTo", targetUserId).get().await()
            docs.mapNotNull { it.toObject(ToDo::class.java) }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting tasks", e)
            emptyList()
        }
    }
    
    // ==================== DEPARTMENTS ====================
    
    suspend fun syncDepartment(department: Department): Boolean {
        return try {
            departmentsCollection.document(department.id.toString()).set(department).await()
            Log.d(TAG, "Department synced: ${department.departmentName}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing department", e)
            false
        }
    }
    
    suspend fun getDepartment(departmentId: String): Department? {
        return try {
            val doc = departmentsCollection.document(departmentId).get().await()
            doc.toObject(Department::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting department", e)
            null
        }
    }
    
    // ==================== REAL-TIME LISTENERS ====================
    
    fun listenToUserClasses(userId: String, onUpdate: (List<ClassItem>) -> Unit) {
        classesCollection.whereEqualTo("teacherId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed", error)
                    return@addSnapshotListener
                }
                val classes = snapshot?.mapNotNull { it.toObject(ClassItem::class.java) } ?: emptyList()
                onUpdate(classes)
            }
    }
    
    fun listenToUserMeetings(userId: String, onUpdate: (List<MeetingItem>) -> Unit) {
        meetingsCollection.whereEqualTo("teacherId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed", error)
                    return@addSnapshotListener
                }
                val meetings = snapshot?.mapNotNull { it.toObject(MeetingItem::class.java) } ?: emptyList()
                onUpdate(meetings)
            }
    }
    
    fun listenToUserTasks(userId: String, onUpdate: (List<ToDo>) -> Unit) {
        tasksCollection.whereEqualTo("assignedTo", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed", error)
                    return@addSnapshotListener
                }
                val tasks = snapshot?.mapNotNull { it.toObject(ToDo::class.java) } ?: emptyList()
                onUpdate(tasks)
            }
    }
    
    fun listenToDepartmentClasses(departmentId: String, onUpdate: (List<ClassItem>) -> Unit) {
        classesCollection.whereEqualTo("departmentId", departmentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Listen failed", error)
                    return@addSnapshotListener
                }
                val classes = snapshot?.mapNotNull { it.toObject(ClassItem::class.java) } ?: emptyList()
                onUpdate(classes)
            }
    }
}