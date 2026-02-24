package com.example.teacherscheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.model.Notice
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class NoticeBoardViewModel @Inject constructor() : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    
    private val _notices = MutableStateFlow<List<NoticeWithSeenCount>>(emptyList())
    val notices: StateFlow<List<NoticeWithSeenCount>> = _notices.asStateFlow()
    
    private var listenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    fun loadNotices(department: String, isHOD: Boolean) {
        listenerRegistration?.remove()
        
        listenerRegistration = firestore.collection("department_notices")
            .whereEqualTo("department", department)
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _notices.value = emptyList()
                    return@addSnapshotListener
                }
                
                viewModelScope.launch {
                    val noticesWithCounts = snapshot?.documents?.mapNotNull { doc ->
                        val noticeId = doc.id
                        val seenCount = if (isHOD) getSeenCount(noticeId) else 0
                        
                        NoticeWithSeenCount(
                            id = noticeId,
                            title = doc.getString("title") ?: "",
                            message = doc.getString("message") ?: "",
                            department = doc.getString("department") ?: "",
                            createdBy = doc.getString("createdBy") ?: "",
                            createdByName = doc.getString("createdByName") ?: "",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                            seenCount = seenCount
                        )
                    } ?: emptyList()
                    
                    _notices.value = noticesWithCounts
                }
            }
    }

    private suspend fun getSeenCount(noticeId: String): Int {
        return try {
            firestore.collection("notice_seen")
                .whereEqualTo("noticeId", noticeId)
                .get()
                .await()
                .size()
        } catch (e: Exception) {
            0
        }
    }

    fun markAsSeen(noticeId: String, userId: String) {
        viewModelScope.launch {
            try {
                val existing = firestore.collection("notice_seen")
                    .whereEqualTo("noticeId", noticeId)
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                if (existing.isEmpty) {
                    firestore.collection("notice_seen").add(mapOf(
                        "noticeId" to noticeId,
                        "userId" to userId,
                        "seenAt" to System.currentTimeMillis()
                    )).await()
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}

data class NoticeWithSeenCount(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val department: String = "",
    val createdBy: String = "",
    val createdByName: String = "",
    val createdAt: Long = 0L,
    val seenCount: Int = 0
)
