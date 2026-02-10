package com.example.teacherscheduler.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
    private val repository = Repository(application)
    
    private val _events = MutableStateFlow<List<TimetableEvent>>(emptyList())
    val events: StateFlow<List<TimetableEvent>> = _events

    private var currentWeekStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    init {
        loadEventsForWeek(currentWeekStart)
    }

    fun loadEventsForWeek(weekStart: Calendar) {
        currentWeekStart = weekStart.clone() as Calendar

        val weekEnd = weekStart.clone() as Calendar
        weekEnd.add(Calendar.DAY_OF_WEEK, 7)
        weekEnd.add(Calendar.MILLISECOND, -1)

        viewModelScope.launch {
            combine(
                repository.getAllActiveClasses(),
                repository.getAllActiveMeetings()
            ) { classes, meetings ->
                val timetableEvents = mutableListOf<TimetableEvent>()
                
                // Process classes
                classes.forEach { classItem ->
                    val classEvents = generateClassEvents(classItem, weekStart, weekEnd)
                    timetableEvents.addAll(classEvents)
                }
                
                // Process meetings
                meetings.forEach { meeting ->
                    val meetingEvent = generateMeetingEvent(meeting, weekStart, weekEnd)
                    meetingEvent?.let { timetableEvents.add(it) }
                }
                
                // Sort by start time
                timetableEvents.sortedWith(compareBy({ it.startTime.get(Calendar.DAY_OF_WEEK) }, { it.startTime.get(Calendar.HOUR_OF_DAY) }, { it.startTime.get(Calendar.MINUTE) }))
            }.collect {
                _events.value = it
            }
        }
    }

    private fun generateClassEvents(classItem: Class, weekStart: Calendar, weekEnd: Calendar): List<TimetableEvent> {
        val events = mutableListOf<TimetableEvent>()

        if (classItem.isRecurring && classItem.daysOfWeek.isNotEmpty()) {
            // Generate events for each recurring day within the week
            val weekStartCopy = weekStart.clone() as Calendar

            for (i in 0..6) {
                val currentDay = weekStartCopy.clone() as Calendar
                currentDay.add(Calendar.DAY_OF_WEEK, i)

                val currentDayOfWeek = currentDay.get(Calendar.DAY_OF_WEEK)

                // Check if this class occurs on this day
                if (classItem.daysOfWeek.contains(currentDayOfWeek)) {
                    // Check if the date is within the class's date range
                    if (currentDay.timeInMillis >= classItem.startDate.time &&
                        currentDay.timeInMillis <= classItem.endDate.time) {

                        val event = createEventFromClass(classItem, currentDayOfWeek)
                        events.add(event)
                    }
                }
            }
        } else {
            // Non-recurring class
            val classDate = Calendar.getInstance()
            classDate.time = classItem.startDate

            // Check if class date is within the current week
            if (classDate.timeInMillis >= weekStart.timeInMillis &&
                classDate.timeInMillis <= weekEnd.timeInMillis) {
                val event = createEventFromClass(classItem, classDate.get(Calendar.DAY_OF_WEEK))
                events.add(event)
            }
        }

        return events
    }

    private fun generateMeetingEvent(meeting: Meeting, weekStart: Calendar, weekEnd: Calendar): TimetableEvent? {
        val meetingDate = Calendar.getInstance()
        meetingDate.time = meeting.startDate

        // Check if meeting date is within the current week
        return if (meetingDate.timeInMillis >= weekStart.timeInMillis &&
                   meetingDate.timeInMillis <= weekEnd.timeInMillis) {
            createEventFromMeeting(meeting, meetingDate.get(Calendar.DAY_OF_WEEK))
        } else {
            null
        }
    }

    private fun createEventFromClass(classItem: Class, dayOfWeek: Int): TimetableEvent {
        val startCal = Calendar.getInstance()
        startCal.time = classItem.startTime
        startCal.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        
        val endCal = Calendar.getInstance()
        endCal.time = classItem.endTime
        endCal.set(Calendar.DAY_OF_WEEK, dayOfWeek)

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

    private fun createEventFromMeeting(meeting: Meeting, dayOfWeek: Int): TimetableEvent {
        val startCal = Calendar.getInstance()
        startCal.time = meeting.startTime
        startCal.set(Calendar.DAY_OF_WEEK, dayOfWeek)
        
        val endCal = Calendar.getInstance()
        endCal.time = meeting.endTime
        endCal.set(Calendar.DAY_OF_WEEK, dayOfWeek)

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
