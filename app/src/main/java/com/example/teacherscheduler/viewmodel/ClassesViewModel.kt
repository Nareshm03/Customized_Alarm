package com.example.teacherscheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.Class
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassesViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    
    val uiState: StateFlow<UiState<ClassesData>> = repository.getAllActiveClasses()
        .map { classes ->
            UiState.Success(ClassesData(classes = classes)) as UiState<ClassesData>
        }
        .catch { e ->
            emit(UiState.Error(e.message ?: "Failed to load classes"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )
    
    fun deleteClass(classItem: Class) {
        viewModelScope.launch {
            repository.deleteClass(classItem)
        }
    }
}
