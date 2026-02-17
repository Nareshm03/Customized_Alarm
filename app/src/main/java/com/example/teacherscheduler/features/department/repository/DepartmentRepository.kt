package com.example.teacherscheduler.features.department.repository

import android.content.Context
import android.util.Log
import com.example.teacherscheduler.data.FirestoreManager
import com.example.teacherscheduler.features.department.data.local.DepartmentTaskDao
import com.example.teacherscheduler.features.department.model.DepartmentTask
import com.example.teacherscheduler.features.department.model.TaskStatus
import com.example.teacherscheduler.notification.EnhancedNotificationHelper
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DepartmentRepository(
    private val taskDao: DepartmentTaskDao,
    private val firestoreManager: FirestoreManager,
    private val context: Context
) {
    private val TAG = "DepartmentRepository"
    private val notificationHelper = EnhancedNotificationHelper(context)
    
    suspend fun createTask(
        title: String,
        description: String,
        createdBy: String,
        departmentId: String,
        assignedTeacherIds: List<String>,
        deadline: Long,
        reminderMinutesBefore: Int,
        priority: com.example.teacherscheduler.features.department.model.TaskPriority
    ): Result<Long> {
        return try {
            val taskId = UUID.randomUUID().toString()
            val statusMap = assignedTeacherIds.associateWith { TaskStatus.ASSIGNED }
            
            val task = DepartmentTask(
                taskId = taskId,
                title = title,
                description = description,
                createdBy = createdBy,
                departmentId = departmentId,
                assignedTeacherIds = assignedTeacherIds,
                deadline = deadline,
                reminderMinutesBefore = reminderMinutesBefore,
                priority = priority,
                statusPerTeacher = statusMap
            )
            
            val id = taskDao.insert(task)
            
            // Schedule notification reminder
            notificationHelper.scheduleDepartmentTaskNotification(
                taskId = taskId,
                title = title,
                description = description,
                deadline = deadline,
                reminderMinutesBefore = reminderMinutesBefore
            )
            
            syncTaskToFirestore(task)
            Result.success(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating task", e)
            Result.failure(e)
        }
    }
    
    suspend fun assignTaskToTeachers(
        taskId: String,
        teacherIds: List<String>
    ): Result<Unit> {
        return try {
            val task = taskDao.getTaskByTaskId(taskId) ?: return Result.failure(Exception("Task not found"))
            val updatedTeacherIds = (task.assignedTeacherIds + teacherIds).distinct()
            val updatedStatusMap = task.statusPerTeacher.toMutableMap().apply {
                teacherIds.forEach { putIfAbsent(it, TaskStatus.ASSIGNED) }
            }
            
            val updatedTask = task.copy(
                assignedTeacherIds = updatedTeacherIds,
                statusPerTeacher = updatedStatusMap,
                updatedAt = System.currentTimeMillis()
            )
            
            taskDao.update(updatedTask)
            syncTaskToFirestore(updatedTask)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error assigning task", e)
            Result.failure(e)
        }
    }
    
    fun getTasksForHOD(hodId: String): Flow<List<DepartmentTask>> {
        return taskDao.getTasksByHOD(hodId)
    }
    
    fun getTasksForTeacher(teacherId: String): Flow<List<DepartmentTask>> {
        return taskDao.getTasksForTeacher(teacherId)
    }
    
    fun getTasksByDepartment(departmentId: String): Flow<List<DepartmentTask>> {
        return taskDao.getTasksByDepartment(departmentId)
    }
    
    suspend fun updateTeacherTaskStatus(
        taskId: String,
        teacherId: String,
        newStatus: TaskStatus
    ): Result<Unit> {
        return try {
            val task = taskDao.getTaskByTaskId(taskId) ?: return Result.failure(Exception("Task not found"))
            
            if (!task.isAssignedToTeacher(teacherId)) {
                return Result.failure(Exception("Task not assigned to teacher"))
            }
            
            val updatedStatusMap = task.statusPerTeacher.toMutableMap().apply {
                put(teacherId, newStatus)
            }
            
            val updatedTask = task.copy(
                statusPerTeacher = updatedStatusMap,
                updatedAt = System.currentTimeMillis()
            )
            
            taskDao.update(updatedTask)
            
            // Cancel notification if completed
            if (newStatus == TaskStatus.COMPLETED) {
                notificationHelper.cancelDepartmentTaskNotification(taskId)
            }
            
            syncTaskToFirestore(updatedTask)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating task status", e)
            Result.failure(e)
        }
    }
    
    suspend fun getTaskById(taskId: String): DepartmentTask? {
        return try {
            taskDao.getTaskByTaskId(taskId)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting task", e)
            null
        }
    }
    
    suspend fun deleteTask(taskId: String): Result<Unit> {
        return try {
            notificationHelper.cancelDepartmentTaskNotification(taskId)
            taskDao.deleteByTaskId(taskId)
            deleteTaskFromFirestore(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting task", e)
            Result.failure(e)
        }
    }
    
    private suspend fun syncTaskToFirestore(task: DepartmentTask) {
        try {
            if (!firestoreManager.isUserLoggedIn()) return
            
            val userId = firestoreManager.getCurrentUserId() ?: return
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            
            val taskData = hashMapOf(
                "taskId" to task.taskId,
                "title" to task.title,
                "description" to task.description,
                "createdBy" to task.createdBy,
                "departmentId" to task.departmentId,
                "assignedTeacherIds" to task.assignedTeacherIds,
                "deadline" to task.deadline,
                "reminderMinutesBefore" to task.reminderMinutesBefore,
                "priority" to task.priority.name,
                "statusPerTeacher" to task.statusPerTeacher.mapValues { it.value.name },
                "createdAt" to task.createdAt,
                "updatedAt" to task.updatedAt
            )
            
            db.collection("departments")
                .document(task.departmentId)
                .collection("tasks")
                .document(task.taskId)
                .set(taskData, SetOptions.merge())
                .await()
            
            Log.d(TAG, "Task synced to Firestore: ${task.taskId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing task to Firestore", e)
        }
    }
    
    private suspend fun deleteTaskFromFirestore(taskId: String) {
        try {
            if (!firestoreManager.isUserLoggedIn()) return
            
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val tasksQuery = db.collectionGroup("tasks")
                .whereEqualTo("taskId", taskId)
                .get()
                .await()
            
            for (document in tasksQuery.documents) {
                document.reference.delete().await()
            }
            
            Log.d(TAG, "Task deleted from Firestore: $taskId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting task from Firestore", e)
        }
    }
    
    suspend fun syncFromFirestore(departmentId: String): Result<Unit> {
        return try {
            if (!firestoreManager.isUserLoggedIn()) {
                return Result.failure(Exception("User not logged in"))
            }
            
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val tasksSnapshot = db.collection("departments")
                .document(departmentId)
                .collection("tasks")
                .get()
                .await()
            
            for (document in tasksSnapshot.documents) {
                val taskData = document.data ?: continue
                
                val task = DepartmentTask(
                    taskId = taskData["taskId"] as? String ?: continue,
                    title = taskData["title"] as? String ?: "",
                    description = taskData["description"] as? String ?: "",
                    createdBy = taskData["createdBy"] as? String ?: "",
                    departmentId = taskData["departmentId"] as? String ?: "",
                    assignedTeacherIds = (taskData["assignedTeacherIds"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    deadline = (taskData["deadline"] as? Number)?.toLong() ?: 0L,
                    reminderMinutesBefore = (taskData["reminderMinutesBefore"] as? Number)?.toInt() ?: 15,
                    priority = com.example.teacherscheduler.features.department.model.TaskPriority.valueOf(
                        taskData["priority"] as? String ?: "MEDIUM"
                    ),
                    statusPerTeacher = (taskData["statusPerTeacher"] as? Map<*, *>)?.mapNotNull { (k, v) ->
                        (k as? String)?.let { key ->
                            (v as? String)?.let { value ->
                                key to TaskStatus.valueOf(value)
                            }
                        }
                    }?.toMap() ?: emptyMap(),
                    createdAt = (taskData["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    updatedAt = (taskData["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                )
                
                taskDao.insert(task)
            }
            
            Log.d(TAG, "Synced ${tasksSnapshot.size()} tasks from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing from Firestore", e)
            Result.failure(e)
        }
    }
}
