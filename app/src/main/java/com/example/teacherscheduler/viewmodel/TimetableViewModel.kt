package com.example.teacherscheduler.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.FirestoreManager
import com.example.teacherscheduler.model.ClassItem
import com.example.teacherscheduler.model.MeetingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

data class TimetableEvent(
    val id: Long,
    val title: String,
    val subtitle: String,
    val startTime: Calendar,
    val endTime: Calendar,
    val color: Int,
    val type: EventType,
    val originalObject: Any
)

enum class EventType {
    CLASS, MEETING
}

class TimetableViewModel(application: Application) : AndroidViewModel(application) {
    private val firestoreManager = FirestoreManager(application)
    
    private val _events = MutableStateFlow<List<TimetableEvent>>(emptyList())
    val events: StateFlow<List<TimetableEvent>> = _events
    
    private var currentClasses = listOf<ClassItem>()
    private var currentMeetings = listOf<MeetingItem>()

    private var currentWeekStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    init {
        setupRealtimeListeners()
    }
    
    private fun setupRealtimeListeners() {
        val userId = firestoreManager.getCurrentUserId() ?: return
        
        // Listen to classes in real-time
        firestoreManager.listenToUserClasses(userId) { classes ->
            currentClasses = classes
            regenerateEvents()
        }
        
        // Listen to meetings in real-time
        firestoreManager.listenToUserMeetings(userId) { meetings ->
            currentMeetings = meetings
            regenerateEvents()
        }
    }
    
    private fun regenerateEvents() {
        viewModelScope.launch {
            val weekEnd = currentWeekStart.clone() as Calendar
            weekEnd.add(Calendar.DAY_OF_WEEK, 7)
            weekEnd.add(Calendar.MILLISECOND, -1)
            
            val timetableEvents = mutableListOf<TimetableEvent>()
            
            // Process classes
            currentClasses.forEach { classItem ->
                val classEvents = generateClassEvents(classItem, currentWeekStart, weekEnd)
                timetableEvents.addAll(classEvents)
            }
            
            // Process meetings
            currentMeetings.forEach { meeting ->
                val meetingEvent = generateMeetingEvent(meeting, currentWeekStart, weekEnd)
                meetingEvent?.let { timetableEvents.add(it) }
            }
            
            // Sort by day and time
            _events.value = timetableEvents.sortedWith(
                compareBy(
                    { it.startTime.get(Calendar.DAY_OF_WEEK) },
                    { it.startTime.get(Calendar.HOUR_OF_DAY) },
                    { it.startTime.get(Calendar.MINUTE) }
                )
            )
        }
    }

    fun loadEventsForWeek(weekStart: Calendar) {
        currentWeekStart = weekStart.clone() as Calendar
        regenerateEvents()
    }

    private fun generateClassEvents(classItem: ClassItem, weekStart: Calendar, weekEnd: Calendar): List<TimetableEvent> {
        val events = mutableListOf<TimetableEvent>()

        if (classItem.isRecurring && classItem.daysOfWeek.isNotEmpty()) {
            for (i in 0..6) {
                val currentDay = weekStart.clone() as Calendar
                currentDay.add(Calendar.DAY_OF_WEEK, i)
                val currentDayOfWeek = currentDay.get(Calendar.DAY_OF_WEEK)

                if (classItem.daysOfWeek.contains(currentDayOfWeek)) {
                    if (currentDay.timeInMillis >= classItem.startDate.time &&
                        currentDay.timeInMillis <= classItem.endDate.time) {
                        events.add(createEventFromClass(classItem, currentDayOfWeek))
                    }
                }
            }
        } else {
            val classDate = Calendar.getInstance()
            classDate.time = classItem.startDate
            if (classDate.timeInMillis >= weekStart.timeInMillis &&
                classDate.timeInMillis <= weekEnd.timeInMillis) {
                events.add(createEventFromClass(classItem, classDate.get(Calendar.DAY_OF_WEEK)))
            }
        }
        return events
    }

    private fun generateMeetingEvent(meeting: MeetingItem, weekStart: Calendar, weekEnd: Calendar): TimetableEvent? {
        val meetingDate = Calendar.getInstance()
        meetingDate.time = meeting.date
        return if (meetingDate.timeInMillis >= weekStart.timeInMillis &&
                   meetingDate.timeInMillis <= weekEnd.timeInMillis) {
            createEventFromMeeting(meeting, meetingDate.get(Calendar.DAY_OF_WEEK))
        } else null
    }

    private fun createEventFromClass(classItem: ClassItem, dayOfWeek: Int): TimetableEvent {
        val startCal = Calendar.getInstance().apply {
            time = classItem.startTime
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
        }
        val endCal = Calendar.getInstance().apply {
            time = classItem.endTime
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
        }
        return TimetableEvent(
            id = classItem.id,
            title = classItem.subject,
            subtitle = "Room ${classItem.roomNumber}",
            startTime = startCal,
            endTime = endCal,
            color = EventType.CLASS.ordinal,
            type = EventType.CLASS,
            originalObject = classItem
        )
    }

    private fun createEventFromMeeting(meeting: MeetingItem, dayOfWeek: Int): TimetableEvent {
        val startCal = Calendar.getInstance().apply {
            time = meeting.startTime
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
        }
        val endCal = Calendar.getInstance().apply {
            time = meeting.endTime
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
        }
        return TimetableEvent(
            id = meeting.id,
            title = meeting.title,
            subtitle = meeting.location,
            startTime = startCal,
            endTime = endCal,
            color = EventType.MEETING.ordinal,
            type = EventType.MEETING,
            originalObject = meeting
        )
    }
}
