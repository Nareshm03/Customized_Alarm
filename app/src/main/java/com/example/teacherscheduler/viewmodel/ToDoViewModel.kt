package com.example.teacherscheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.ToDo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ToDoViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val uiState: StateFlow<UiState<ToDosData>> = repository.getAllActiveToDos()
        .map { todos ->
            val data = ToDosData(
                todos = todos,
                pendingCount = todos.count { !it.isCompleted },
                overdueCount = todos.count { it.isOverdue() }
            )
            UiState.Success(data) as UiState<ToDosData>
        }
        .catch { e ->
            emit(UiState.Error(e.message ?: "Failed to load tasks"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    fun toggleCompletion(todo: ToDo) {
        viewModelScope.launch {
            repository.toggleToDoCompletion(todo.id, !todo.isCompleted)
        }
    }

    fun insertToDo(todo: ToDo) {
        viewModelScope.launch {
            repository.insertToDo(todo)
        }
    }

    fun deleteToDo(todo: ToDo) {
        viewModelScope.launch {
            repository.deleteToDo(todo)
        }
    }
}

