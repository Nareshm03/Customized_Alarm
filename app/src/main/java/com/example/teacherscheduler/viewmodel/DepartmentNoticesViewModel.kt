package com.example.teacherscheduler.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.DepartmentManager
import com.example.teacherscheduler.data.ProfileManager
import com.example.teacherscheduler.model.DepartmentAnnouncement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for Department Notices/Announcements
 *
 * Manages:
 * - Loading department announcements
 * - Marking announcements as seen/read
 * - UI state management
 */
class DepartmentNoticesViewModel(application: Application) : AndroidViewModel(application) {

    private val departmentManager = DepartmentManager(application)
    private val profileManager = ProfileManager(application)

    // UI State
    private val _uiState = MutableStateFlow<NoticesUiState>(NoticesUiState.Loading)
    val uiState: StateFlow<NoticesUiState> = _uiState.asStateFlow()

    // Notices list
    private val _notices = MutableStateFlow<List<DepartmentAnnouncement>>(emptyList())
    val notices: StateFlow<List<DepartmentAnnouncement>> = _notices.asStateFlow()

    /**
     * Load notices for the user's department
     */
    fun loadNotices() {
        viewModelScope.launch {
            try {
                _uiState.value = NoticesUiState.Loading

                val userProfile = profileManager.getUserProfile()
                val departmentId = userProfile.primaryDepartmentId

                if (departmentId <= 0) {
                    _uiState.value = NoticesUiState.Error("No department assigned")
                    return@launch
                }

                // Collect announcements
                departmentManager.getDepartmentAnnouncements(departmentId).collect { announcements ->
                    _notices.value = announcements
                    _uiState.value = if (announcements.isEmpty()) {
                        NoticesUiState.Empty
                    } else {
                        NoticesUiState.Success
                    }
                }
            } catch (e: Exception) {
                _uiState.value = NoticesUiState.Error(e.message ?: "Failed to load notices")
            }
        }
    }

    /**
     * Mark notice as seen by current user
     */
    fun markNoticeAsSeen(noticeId: Long) {
        viewModelScope.launch {
            try {
                val announcement = departmentManager.getAnnouncementById(noticeId)
                if (announcement != null) {
                    // Increment total readers count
                    val updated = announcement.copy(
                        totalReaders = announcement.totalReaders + 1,
                        updatedAt = System.currentTimeMillis()
                    )
                    departmentManager.updateAnnouncement(updated)
                }
            } catch (e: Exception) {
                // Silent fail - marking as seen is not critical
                e.printStackTrace()
            }
        }
    }

    /**
     * Refresh notices
     */
    fun refresh() {
        loadNotices()
    }
}

/**
 * UI State for Notices screen
 */
sealed class NoticesUiState {
    object Loading : NoticesUiState()
    object Success : NoticesUiState()
    object Empty : NoticesUiState()
    data class Error(val message: String) : NoticesUiState()
}

