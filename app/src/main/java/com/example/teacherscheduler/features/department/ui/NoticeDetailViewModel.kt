package com.example.teacherscheduler.features.department.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.features.department.model.DepartmentNotice
import com.example.teacherscheduler.features.department.repository.DepartmentNoticeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoticeDetailViewModel(
    private val repository: DepartmentNoticeRepository,
    private val noticeId: String,
    private val teacherId: String
) : ViewModel() {

    private val _notice = MutableStateFlow<DepartmentNotice?>(null)
    val notice: StateFlow<DepartmentNotice?> = _notice.asStateFlow()

    init {
        loadNotice()
    }

    private fun loadNotice() {
        viewModelScope.launch {
            repository.fetchNoticesByDepartment("")
                .map { notices -> notices.find { it.noticeId == noticeId } }
                .collect { _notice.value = it }
        }
    }

    fun markAsSeen() {
        viewModelScope.launch {
            repository.markNoticeAsSeen(noticeId, teacherId)
        }
    }
}
