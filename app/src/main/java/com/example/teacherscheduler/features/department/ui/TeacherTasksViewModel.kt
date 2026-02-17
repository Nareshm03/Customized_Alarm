package com.example.teacherscheduler.features.department.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.features.department.model.DepartmentTask
import com.example.teacherscheduler.features.department.model.TaskStatus
import com.example.teacherscheduler.features.department.repository.DepartmentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TeacherTasksViewModel(
    private val repository: DepartmentRepository,
    private val teacherId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<TasksUiState>(TasksUiState.Loading)
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    val tasks: StateFlow<List<DepartmentTask>> = repository.getTasksForTeacher(teacherId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun markInProgress(taskId: String) {
        viewModelScope.launch {
            _uiState.value = TasksUiState.Loading
            val result = repository.updateTeacherTaskStatus(taskId, teacherId, TaskStatus.IN_PROGRESS)
            _uiState.value = if (result.isSuccess) {
                TasksUiState.Success
            } else {
                TasksUiState.Error(result.exceptionOrNull()?.message ?: "Failed to update status")
            }
        }
    }

    fun markCompleted(taskId: String) {
        viewModelScope.launch {
            _uiState.value = TasksUiState.Loading
            val result = repository.updateTeacherTaskStatus(taskId, teacherId, TaskStatus.COMPLETED)
            _uiState.value = if (result.isSuccess) {
                TasksUiState.Success
            } else {
                TasksUiState.Error(result.exceptionOrNull()?.message ?: "Failed to complete task")
            }
        }
    }

    fun resetUiState() {
        _uiState.value = TasksUiState.Idle
    }
}

sealed class TasksUiState {
    object Idle : TasksUiState()
    object Loading : TasksUiState()
    object Success : TasksUiState()
    data class Error(val message: String) : TasksUiState()
}
