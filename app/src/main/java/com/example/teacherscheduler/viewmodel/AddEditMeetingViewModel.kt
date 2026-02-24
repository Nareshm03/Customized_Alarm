package com.example.teacherscheduler.viewmodel

import androidx.lifecycle.ViewModel
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.Meeting
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddEditMeetingViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    suspend fun saveMeeting(meeting: Meeting): Boolean {
        return try {
            if (meeting.id == 0L) {
                repository.insertMeeting(meeting)
            } else {
                repository.updateMeeting(meeting)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}

