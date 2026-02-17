package com.example.teacherscheduler.features.department.repository

import android.util.Log
import com.example.teacherscheduler.data.DataEventManager
import com.example.teacherscheduler.data.FirestoreManager
import com.example.teacherscheduler.features.department.data.local.DepartmentNoticeDao
import com.example.teacherscheduler.features.department.model.DepartmentNotice
import com.example.teacherscheduler.features.department.model.NoticePriority
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DepartmentNoticeRepository(
    private val noticeDao: DepartmentNoticeDao,
    private val firestoreManager: FirestoreManager
) {
    private val TAG = "DepartmentNoticeRepository"
    private val db = FirebaseFirestore.getInstance()
    
    suspend fun createNotice(
        departmentId: String,
        title: String,
        description: String,
        createdBy: String,
        priority: NoticePriority,
        isHOD: Boolean
    ): Result<String> {
        return try {
            if (!isHOD) {
                return Result.failure(Exception("Unauthorized: Only HOD can create notices"))
            }
            
            val noticeId = UUID.randomUUID().toString()
            val notice = DepartmentNotice(
                noticeId = noticeId,
                departmentId = departmentId,
                title = title,
                description = description,
                createdBy = createdBy,
                priority = priority,
                createdAt = System.currentTimeMillis()
            )
            
            noticeDao.insert(notice)
            syncNoticeToFirestore(notice)
            Result.success(noticeId)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notice", e)
            Result.failure(e)
        }
    }
    
    fun fetchNoticesByDepartment(departmentId: String): Flow<List<DepartmentNotice>> {
        return noticeDao.getNoticesByDepartment(departmentId)
    }
    
    fun observeNoticesRealtime(departmentId: String): Flow<List<DepartmentNotice>> = callbackFlow {
        if (!firestoreManager.isUserLoggedIn() || departmentId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val listener: ListenerRegistration = db.collection("departments")
            .document(departmentId)
            .collection("notices")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to notices", error)
                    return@addSnapshotListener
                }
                
                snapshot?.let { querySnapshot ->
                    val notices = querySnapshot.documents.mapNotNull { document ->
                        try {
                            val data = document.data ?: return@mapNotNull null
                            DepartmentNotice(
                                noticeId = data["noticeId"] as? String ?: return@mapNotNull null,
                                departmentId = data["departmentId"] as? String ?: "",
                                title = data["title"] as? String ?: "",
                                description = data["description"] as? String ?: "",
                                createdBy = data["createdBy"] as? String ?: "",
                                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                priority = NoticePriority.valueOf(data["priority"] as? String ?: "NORMAL"),
                                seenBy = (data["seenBy"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing notice", e)
                            null
                        }
                    }
                    trySend(notices)
                }
            }
        
        awaitClose { 
            listener.remove()
            Log.d(TAG, "Firestore listener removed for department: $departmentId")
        }
    }
    
    suspend fun markNoticeAsSeen(noticeId: String, teacherId: String): Result<Unit> {
        return try {
            val notice = noticeDao.getNoticeByNoticeId(noticeId)
                ?: return Result.failure(Exception("Notice not found"))
            
            if (notice.seenBy.contains(teacherId)) {
                return Result.success(Unit)
            }
            
            updateSeenByInFirestore(noticeId, notice.departmentId, teacherId)
            val updatedSeenBy = notice.seenBy + teacherId
            val updatedNotice = notice.copy(seenBy = updatedSeenBy)
            noticeDao.update(updatedNotice)
            DataEventManager.emit(DataEventManager.DataEvent.NoticeRead)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notice as seen", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteNotice(
        noticeId: String,
        userId: String,
        isHOD: Boolean
    ): Result<Unit> {
        return try {
            if (!isHOD) {
                return Result.failure(Exception("Unauthorized: Only HOD can delete notices"))
            }
            
            val notice = noticeDao.getNoticeByNoticeId(noticeId)
                ?: return Result.failure(Exception("Notice not found"))
            
            if (notice.createdBy != userId) {
                return Result.failure(Exception("Unauthorized: Only creator can delete this notice"))
            }
            
            noticeDao.deleteByNoticeId(noticeId)
            deleteNoticeFromFirestore(noticeId, notice.departmentId)
            
            Log.d(TAG, "Notice deleted successfully: $noticeId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notice", e)
            Result.failure(e)
        }
    }
    
    suspend fun onNoticeOpened(noticeId: String, teacherId: String): Result<Unit> {
        return markNoticeAsSeen(noticeId, teacherId)
    }
    
    suspend fun getSeenCount(noticeId: String): Int {
        return try {
            val notice = noticeDao.getNoticeByNoticeId(noticeId)
            notice?.seenBy?.size ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting seen count", e)
            0
        }
    }
    
    private suspend fun syncNoticeToFirestore(notice: DepartmentNotice) {
        try {
            if (!firestoreManager.isUserLoggedIn()) return
            
            val noticeData = hashMapOf(
                "noticeId" to notice.noticeId,
                "departmentId" to notice.departmentId,
                "title" to notice.title,
                "description" to notice.description,
                "createdBy" to notice.createdBy,
                "createdAt" to notice.createdAt,
                "priority" to notice.priority.name,
                "seenBy" to notice.seenBy
            )
            
            db.collection("departments")
                .document(notice.departmentId)
                .collection("notices")
                .document(notice.noticeId)
                .set(noticeData, SetOptions.merge())
                .await()
            
            Log.d(TAG, "Notice synced to Firestore: ${notice.noticeId}")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing notice to Firestore", e)
        }
    }
    
    private suspend fun updateSeenByInFirestore(
        noticeId: String,
        departmentId: String,
        teacherId: String
    ) {
        try {
            if (!firestoreManager.isUserLoggedIn()) return
            
            db.collection("departments")
                .document(departmentId)
                .collection("notices")
                .document(noticeId)
                .update("seenBy", FieldValue.arrayUnion(teacherId))
                .await()
            
            Log.d(TAG, "Notice seenBy updated in Firestore: $noticeId")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating seenBy in Firestore", e)
        }
    }
    
    suspend fun syncFromFirestore(departmentId: String): Result<Unit> {
        return try {
            if (!firestoreManager.isUserLoggedIn()) {
                return Result.failure(Exception("User not logged in"))
            }
            
            val noticesSnapshot = db.collection("departments")
                .document(departmentId)
                .collection("notices")
                .get()
                .await()
            
            for (document in noticesSnapshot.documents) {
                val data = document.data ?: continue
                
                val notice = DepartmentNotice(
                    noticeId = data["noticeId"] as? String ?: continue,
                    departmentId = data["departmentId"] as? String ?: "",
                    title = data["title"] as? String ?: "",
                    description = data["description"] as? String ?: "",
                    createdBy = data["createdBy"] as? String ?: "",
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    priority = NoticePriority.valueOf(data["priority"] as? String ?: "NORMAL"),
                    seenBy = (data["seenBy"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                )
                
                noticeDao.insert(notice)
            }
            
            Log.d(TAG, "Synced ${noticesSnapshot.size()} notices from Firestore")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing from Firestore", e)
            Result.failure(e)
        }
    }
    
    private suspend fun deleteNoticeFromFirestore(noticeId: String, departmentId: String) {
        try {
            if (!firestoreManager.isUserLoggedIn()) return
            
            db.collection("departments")
                .document(departmentId)
                .collection("notices")
                .document(noticeId)
                .delete()
                .await()
            
            Log.d(TAG, "Notice deleted from Firestore: $noticeId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting notice from Firestore", e)
        }
    }
}
