package com.example.teacherscheduler.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.ToDo
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ToDoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)

    val allToDos: StateFlow<List<ToDo>> = repository.getAllToDos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeToDos: StateFlow<List<ToDo>> = repository.getAllActiveToDos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedToDos: StateFlow<List<ToDo>> = repository.getCompletedToDos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overdueToDos: StateFlow<List<ToDo>> = repository.getOverdueToDos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeToDosCount: StateFlow<Int> = repository.getActiveToDosCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val overdueToDosCount: StateFlow<Int> = repository.getOverdueToDosCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val categories: StateFlow<List<String>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertToDo(todo: ToDo) {
        viewModelScope.launch {
            repository.insertToDo(todo)
        }
    }

    fun updateToDo(todo: ToDo) {
        viewModelScope.launch {
            repository.updateToDo(todo)
        }
    }

    fun deleteToDo(todo: ToDo) {
        viewModelScope.launch {
            repository.deleteToDo(todo)
        }
    }

    fun toggleCompletion(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleToDoCompletion(id, isCompleted)
        }
    }

    fun getToDosByCategory(category: String): StateFlow<List<ToDo>> {
        return repository.getToDosByCategory(category)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun getToDosByPriority(priority: ToDo.Priority): StateFlow<List<ToDo>> {
        return repository.getToDosByPriority(priority)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }
}

