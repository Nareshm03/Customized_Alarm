package com.example.teacherscheduler.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.data.local.NoticeDao
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModelRefactored @Inject constructor(
    private val repository: Repository,
    private val noticeDao: NoticeDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _userId = MutableStateFlow("user123")
    private val _departmentId = MutableStateFlow(1L)

    val uiState: StateFlow<UiState<DashboardData>> = combine(
        repository.getAllActiveClasses(),
        repository.getAllActiveMeetings(),
        repository.getActiveToDosCount(),
        repository.getOverdueToDosCount(),
        combine(_departmentId, _userId) { deptId, userId ->
            noticeDao.getUnseenCount(deptId, userId)
        }.flatMapLatest { it }
    ) { classes, meetings, pendingCount, overdueCount, unseenNoticeCount ->
        
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val greeting = when (hour) {
            in 5..11 -> "Good Morning!"
            in 12..16 -> "Good Afternoon!"
            in 17..20 -> "Good Evening!"
            else -> "Good Night!"
        }
        
        val todayClasses = filterTodayClasses(classes)
        val todayMeetings = filterTodayMeetings(meetings)
        val upcomingMeetings = filterUpcomingMeetings(meetings)
        
        val allUpcoming = (todayClasses.map { "Class: ${it.title}" to it.startTime } +
                          todayMeetings.map { "Meeting: ${it.title}" to it.startTime })
            .sortedBy { it.second }

        val nextEvent = allUpcoming.firstOrNull()

        DashboardData(
            greeting = greeting,
            todayClassesCount = todayClasses.size,
            upcomingMeetingsCount = upcomingMeetings.size,
            activeToDosCount = pendingCount + overdueCount,
            unseenNoticesCount = unseenNoticeCount,
            todayClasses = todayClasses,
            upcomingMeetings = upcomingMeetings,
            nextEventTitle = nextEvent?.first ?: "No upcoming events",
            nextEventTime = nextEvent?.let { formatTime(it.second) } ?: ""
        )
    }.map<DashboardData, UiState<DashboardData>> { data ->
        UiState.Success(data)
    }.catch { e ->
        emit(UiState.Error(e.message ?: "Unknown error"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UiState.Loading
    )

    private fun filterTodayClasses(classes: List<Class>): List<Class> {
        val today = Calendar.getInstance()
        return classes.filter { classItem ->
            if (classItem.isRecurring) {
                val todayDayOfWeek = today.get(Calendar.DAY_OF_WEEK)
                classItem.daysOfWeek.contains(todayDayOfWeek)
            } else {
                val classDate = Calendar.getInstance().apply { timeInMillis = classItem.startDate.time }
                classDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                classDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
            }
        }
    }
    
    private fun filterTodayMeetings(meetings: List<Meeting>): List<Meeting> {
        val today = Calendar.getInstance()
        return meetings.filter { meeting ->
            val meetingDate = Calendar.getInstance().apply { timeInMillis = meeting.startDate.time }
            meetingDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            meetingDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        }
    }
    
    private fun filterUpcomingMeetings(meetings: List<Meeting>): List<Meeting> {
        val now = System.currentTimeMillis()
        val nextWeek = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7)
        }.timeInMillis
        
        return meetings.filter { meeting ->
            val startTime = meeting.getStartDateTime()
            startTime > now && startTime <= nextWeek
        }
    }

    private fun formatTime(date: Date): String {
        val format = SimpleDateFormat("h:mm a", Locale.getDefault())
        return format.format(date)
    }
    
    fun deleteClass(classItem: Class) {
        viewModelScope.launch {
            repository.deleteClass(classItem)
        }
    }
    
    fun deleteMeeting(meeting: Meeting) {
        viewModelScope.launch {
            repository.deleteMeeting(meeting)
        }
    }
}