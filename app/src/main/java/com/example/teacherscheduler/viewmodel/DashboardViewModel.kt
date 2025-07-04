package com.example.teacherscheduler.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.switchMap
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = Repository(application)
    
    private val _selectedDate = MutableLiveData<Date>().apply {
        value = Calendar.getInstance().time
    }
    
    val selectedDate: LiveData<Date> = _selectedDate
    
    // Using switchMap extension function to transform LiveData
    val classesForSelectedDate: LiveData<List<Class>> = _selectedDate.switchMap { date ->
        repository.getClassesForDay(date)
    }
    
    val meetingsForSelectedDate: LiveData<List<Meeting>> = _selectedDate.switchMap { date ->
        repository.getMeetingsForDay(date)
    }
    
    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
    }
    
    fun getTodayDate(): Date {
        return Calendar.getInstance().time
    }
    
    fun getTomorrowDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return calendar.time
    }
    
    fun deleteClass(classItem: Class) {
        // Implement class deletion through repository using coroutines
        viewModelScope.launch {
            repository.deleteClass(classItem)
        }
    }
    
    fun deleteMeeting(meeting: Meeting) {
        // Implement meeting deletion through repository using coroutines
        viewModelScope.launch {
            repository.deleteMeeting(meeting)
        }
    }
}