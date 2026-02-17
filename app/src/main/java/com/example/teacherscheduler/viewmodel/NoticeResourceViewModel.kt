package com.example.teacherscheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.local.NoticeDao
import com.example.teacherscheduler.data.local.ResourceDao
import com.example.teacherscheduler.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val noticeDao: NoticeDao
) : ViewModel() {
    
    private val _departmentId = MutableStateFlow(1L)
    private val _userId = MutableStateFlow("user123")
    private val _isHOD = MutableStateFlow(false)
    
    val uiState: StateFlow<UiState<NoticeData>> = combine(
        _departmentId.flatMapLatest { deptId ->
            noticeDao.getNoticesByDepartment(deptId)
        },
        combine(_departmentId, _userId) { deptId, userId ->
            noticeDao.getUnseenCount(deptId, userId)
        }.flatMapLatest { it },
        _isHOD
    ) { notices, unseenCount, isHOD ->
        NoticeData(
            notices = notices,
            unseenCount = unseenCount,
            isHOD = isHOD
        )
    }.map<NoticeData, UiState<NoticeData>> { data ->
        UiState.Success(data)
    }.catch { e ->
        emit(UiState.Error(e.message ?: "Failed to load notices"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )
    
    fun markAsSeen(noticeId: Long) {
        viewModelScope.launch {
            noticeDao.markAsSeen(
                NoticeSeenStatus(
                    noticeId = noticeId,
                    userId = _userId.value
                )
            )
        }
    }
}

@HiltViewModel
class ResourceViewModel @Inject constructor(
    private val resourceDao: ResourceDao
) : ViewModel() {
    
    private val _departmentId = MutableStateFlow(1L)
    
    val uiState: StateFlow<UiState<ResourceData>> = combine(
        _departmentId.flatMapLatest { deptId ->
            resourceDao.getResourcesByDepartment(deptId)
        },
        resourceDao.getAllSubjects()
    ) { resources, subjects ->
        ResourceData(
            resources = resources,
            subjects = subjects
        )
    }.map<ResourceData, UiState<ResourceData>> { data ->
        UiState.Success(data)
    }.catch { e ->
        emit(UiState.Error(e.message ?: "Failed to load resources"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )
}
