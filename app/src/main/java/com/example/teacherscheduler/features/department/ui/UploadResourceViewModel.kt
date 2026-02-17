package com.example.teacherscheduler.features.department.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.features.department.model.ResourceVisibility
import com.example.teacherscheduler.features.department.repository.DepartmentResourceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UploadResourceViewModel(
    private val repository: DepartmentResourceRepository,
    private val departmentId: String,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun uploadResource(
        fileUri: Uri,
        title: String,
        description: String,
        subjectName: String,
        visibility: ResourceVisibility,
        fileType: String
    ) {
        viewModelScope.launch {
            _uiState.value = UploadUiState.Loading
            
            val result = repository.uploadResource(
                fileUri = fileUri,
                title = title,
                description = description,
                subjectName = subjectName,
                uploadedBy = userId,
                departmentId = departmentId,
                visibility = visibility,
                fileType = fileType
            )
            
            _uiState.value = if (result.isSuccess) {
                UploadUiState.Success
            } else {
                UploadUiState.Error(result.exceptionOrNull()?.message ?: "Upload failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = UploadUiState.Idle
    }
}

sealed class UploadUiState {
    object Idle : UploadUiState()
    object Loading : UploadUiState()
    object Success : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}
