package com.example.teacherscheduler.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.DepartmentManager
import com.example.teacherscheduler.data.ProfileManager
import com.example.teacherscheduler.model.DepartmentMember
import com.example.teacherscheduler.model.ToDo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

/**
 * ViewModel for Department-related operations, particularly HOD task assignment
 */
class DepartmentViewModel(application: Application) : AndroidViewModel(application) {
    private val departmentManager = DepartmentManager(application)
    private val profileManager = ProfileManager(application)

    // UI State
    private val _uiState = MutableStateFlow<AssignTaskUiState>(AssignTaskUiState.Initial)
    val uiState: StateFlow<AssignTaskUiState> = _uiState.asStateFlow()

    // Department members (teachers)
    private val _departmentMembers = MutableStateFlow<List<DepartmentMember>>(emptyList())
    val departmentMembers: StateFlow<List<DepartmentMember>> = _departmentMembers.asStateFlow()

    /**
     * Load department members for the current HOD
     */
    fun loadDepartmentMembers(departmentId: Long) {
        viewModelScope.launch {
            try {
                val members = departmentManager.getDepartmentMembers(departmentId).first()
                // Filter to show only teachers (exclude HOD if needed)
                _departmentMembers.value = members.filter { !it.isHOD() }
            } catch (e: Exception) {
                _uiState.value = AssignTaskUiState.Error("Failed to load teachers: ${e.message}")
            }
        }
    }

    /**
     * Assign task to selected teachers
     */
    fun assignTask(
        departmentId: Long,
        title: String,
        description: String,
        selectedTeachers: List<String>, // List of user IDs
        dueDate: Date?,
        reminderMinutes: Int,
        priority: ToDo.Priority
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = AssignTaskUiState.Loading

                val currentUser = profileManager.getUserProfile()
                val assignedBy = currentUser.id
                val assignedByName = currentUser.name

                if (title.isBlank()) {
                    _uiState.value = AssignTaskUiState.Error("Task title is required")
                    return@launch
                }

                if (selectedTeachers.isEmpty()) {
                    _uiState.value = AssignTaskUiState.Error("Please select at least one teacher")
                    return@launch
                }

                // Assign task to each selected teacher
                for (teacherUserId in selectedTeachers) {
                    val teacher = _departmentMembers.value.find { it.userId == teacherUserId }
                    val assignedToName = teacher?.userName ?: "Unknown"

                    departmentManager.assignTask(
                        departmentId = departmentId,
                        title = title,
                        description = description,
                        category = "General",
                        priority = priority,
                        assignedTo = teacherUserId,
                        assignedToName = assignedToName,
                        assignedBy = assignedBy,
                        assignedByName = assignedByName,
                        dueDate = dueDate
                    )
                }

                _uiState.value = AssignTaskUiState.Success(
                    "Task assigned to ${selectedTeachers.size} teacher(s) successfully"
                )
            } catch (e: Exception) {
                _uiState.value = AssignTaskUiState.Error("Failed to assign task: ${e.message}")
            }
        }
    }

    /**
     * Assign task to all teachers in department (bulk assignment)
     */
    fun assignTaskToAll(
        departmentId: Long,
        title: String,
        description: String,
        dueDate: Date?,
        reminderMinutes: Int,
        priority: ToDo.Priority
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = AssignTaskUiState.Loading

                val currentUser = profileManager.getUserProfile()
                val assignedBy = currentUser.id
                val assignedByName = currentUser.name

                if (title.isBlank()) {
                    _uiState.value = AssignTaskUiState.Error("Task title is required")
                    return@launch
                }

                departmentManager.bulkAssignTask(
                    departmentId = departmentId,
                    title = title,
                    description = description,
                    category = "General",
                    priority = priority,
                    assignedBy = assignedBy,
                    assignedByName = assignedByName,
                    dueDate = dueDate
                )

                _uiState.value = AssignTaskUiState.Success(
                    "Task assigned to all teachers successfully"
                )
            } catch (e: Exception) {
                _uiState.value = AssignTaskUiState.Error("Failed to assign task: ${e.message}")
            }
        }
    }

    /**
     * Reset UI state to initial
     */
    fun resetUiState() {
        _uiState.value = AssignTaskUiState.Initial
    }
}

/**
 * UI State for task assignment
 */
sealed class AssignTaskUiState {
    object Initial : AssignTaskUiState()
    object Loading : AssignTaskUiState()
    data class Success(val message: String) : AssignTaskUiState()
    data class Error(val message: String) : AssignTaskUiState()
}

