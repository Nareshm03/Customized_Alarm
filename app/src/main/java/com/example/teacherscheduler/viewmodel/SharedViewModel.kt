package com.example.teacherscheduler.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel to share data between fragments
 */
class SharedViewModel : ViewModel() {
    
    // For FAB visibility and behavior
    private val _fabVisible = MutableLiveData<Boolean>()
    val fabVisible: LiveData<Boolean> = _fabVisible
    
    private val _fabAction = MutableLiveData<FabAction>()
    val fabAction: LiveData<FabAction> = _fabAction
    
    // For navigation events
    private val _navigateToClassDetail = MutableLiveData<Long?>()
    val navigateToClassDetail: LiveData<Long?> = _navigateToClassDetail
    
    private val _navigateToMeetingDetail = MutableLiveData<Long?>()
    val navigateToMeetingDetail: LiveData<Long?> = _navigateToMeetingDetail
    
    // Set FAB visibility
    fun setFabVisible(visible: Boolean) {
        _fabVisible.value = visible
    }
    
    // Set FAB action
    fun setFabAction(action: FabAction) {
        _fabAction.value = action
    }
    
    // Navigation methods
    fun navigateToClassDetail(classId: Long) {
        _navigateToClassDetail.value = classId
    }
    
    fun navigateToMeetingDetail(meetingId: Long) {
        _navigateToMeetingDetail.value = meetingId
    }
    
    fun onClassDetailNavigated() {
        _navigateToClassDetail.value = null
    }
    
    fun onMeetingDetailNavigated() {
        _navigateToMeetingDetail.value = null
    }
    
    // FAB actions enum
    enum class FabAction {
        ADD_CLASS,
        ADD_MEETING
    }
}