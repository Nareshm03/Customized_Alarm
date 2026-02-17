package com.example.teacherscheduler.features.department.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.features.department.model.DepartmentResource
import com.example.teacherscheduler.features.department.repository.DepartmentResourceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DepartmentResourceViewModel(
    private val repository: DepartmentResourceRepository,
    private val departmentId: String,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<ResourceUiState>(ResourceUiState.Loading)
    val uiState: StateFlow<ResourceUiState> = _uiState.asStateFlow()

    val departmentResources: StateFlow<List<DepartmentResource>> = 
        repository.fetchDepartmentResources(departmentId, userId)
            .onStart { _uiState.value = ResourceUiState.Loading }
            .catch { e -> 
                _uiState.value = ResourceUiState.Error(e.message ?: "Failed to load resources")
                emit(emptyList())
            }
            .onEach { _uiState.value = ResourceUiState.Success }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val privateResources: StateFlow<List<DepartmentResource>> = 
        repository.fetchPrivateResources(userId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    fun retry() {
        viewModelScope.launch {
            _uiState.value = ResourceUiState.Loading
        }
    }
}

sealed class ResourceUiState {
    object Loading : ResourceUiState()
    object Success : ResourceUiState()
    data class Error(val message: String) : ResourceUiState()
}
