package com.example.teacherscheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    
    val classes: StateFlow<List<Class>> = repository.getAllActiveClasses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    
    val meetings: StateFlow<List<Meeting>> = repository.getAllActiveMeetings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
