package com.example.teacherscheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.Meeting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeetingViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    
    val uiState: StateFlow<UiState<MeetingsData>> = repository.getAllActiveMeetings()
        .map<List<Meeting>, UiState<MeetingsData>> { meetings ->
            UiState.Success(MeetingsData(meetings = meetings))
        }
        .catch { e ->
            emit(UiState.Error(e.message ?: "Failed to load meetings"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )
    
    fun deleteMeeting(meeting: Meeting) {
        viewModelScope.launch {
            repository.deleteMeeting(meeting)
        }
    }
}