package com.example.teacherscheduler.features.department.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.features.department.model.DepartmentNotice
import com.example.teacherscheduler.features.department.repository.DepartmentNoticeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DepartmentNoticeViewModel(
    private val repository: DepartmentNoticeRepository,
    private val departmentId: String,
    private val teacherId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<NoticeUiState>(NoticeUiState.Loading)
    val uiState: StateFlow<NoticeUiState> = _uiState.asStateFlow()

    val notices: StateFlow<List<DepartmentNotice>> = repository.observeNoticesRealtime(departmentId)
        .onStart { _uiState.value = NoticeUiState.Loading }
        .catch { e -> 
            _uiState.value = NoticeUiState.Error(e.message ?: "Failed to load notices")
            emit(emptyList())
        }
        .onEach { _uiState.value = NoticeUiState.Success }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markNoticeAsSeen(noticeId: String) {
        viewModelScope.launch {
            repository.markNoticeAsSeen(noticeId, teacherId)
        }
    }

    fun getSeenCount(noticeId: String): Flow<Int> = flow {
        emit(repository.getSeenCount(noticeId))
    }
}

sealed class NoticeUiState {
    object Loading : NoticeUiState()
    object Success : NoticeUiState()
    data class Error(val message: String) : NoticeUiState()
}
