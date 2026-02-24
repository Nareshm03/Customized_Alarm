package com.example.teacherscheduler.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.DataManager
import com.example.teacherscheduler.model.ClassItem
import com.example.teacherscheduler.model.MeetingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

enum class EventType {
    CLASS, MEETING
}

data class TimetableEvent(
    val id: Long,
    val title: String,
    val subtitle: String,
    val startTime: Calendar,
    val endTime: Calendar,
    val type: EventType,
    val originalObject: Any
)

class TimetableViewModel(application: Application) : AndroidViewModel(application) {

    private val _events = MutableStateFlow<List<TimetableEvent>>(emptyList())
    val events: StateFlow<List<TimetableEvent>> = _events.asStateFlow()

    init {
        val currentWeekStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        loadEventsForWeek(currentWeekStart)
    }

    fun loadEventsForWeek(weekStart: Calendar) {
        viewModelScope.launch {
            val weekEnd = weekStart.clone() as Calendar
            weekEnd.add(Calendar.DAY_OF_YEAR, 7)

            val timetableEvents = mutableListOf<TimetableEvent>()

            // Load classes
            val classes = DataManager.getActiveClasses()
            classes.forEach { classItem ->
                if (isClassInWeek(classItem, weekStart, weekEnd)) {
                    val classEvents = createEventsForClass(classItem, weekStart)
                    timetableEvents.addAll(classEvents)
                }
            }

            // Load meetings
            val meetings = DataManager.getActiveMeetings()
            meetings.forEach { meeting ->
                if (isMeetingInWeek(meeting, weekStart, weekEnd)) {
                    timetableEvents.add(createEventForMeeting(meeting))
                }
            }

            _events.value = timetableEvents.sortedBy { it.startTime.timeInMillis }
        }
    }

    private fun isClassInWeek(classItem: com.example.teacherscheduler.model.Class, weekStart: Calendar, weekEnd: Calendar): Boolean {
        // Simple check: if class is active, it might be in the week
        // A more robust check would involve checking the start/end dates and recurrence
        return classItem.isActive
    }

    private fun isMeetingInWeek(meeting: com.example.teacherscheduler.model.Meeting, weekStart: Calendar, weekEnd: Calendar): Boolean {
        val meetingDate = Calendar.getInstance()
        meetingDate.time = meeting.startDate
        return meetingDate.after(weekStart) && meetingDate.before(weekEnd)
    }

    private fun createEventsForClass(classItem: com.example.teacherscheduler.model.Class, weekStart: Calendar): List<TimetableEvent> {
        val events = mutableListOf<TimetableEvent>()
        val classItemModel = classItem.toClassItem()

        classItem.daysOfWeek.forEach { day ->
            val eventDate = weekStart.clone() as Calendar
            // Adjust day (1 is Sunday in Calendar, but 1 is Monday in our app's logic)
            val calendarDay = when (day) {
                1 -> Calendar.MONDAY
                2 -> Calendar.TUESDAY
                3 -> Calendar.WEDNESDAY
                4 -> Calendar.THURSDAY
                5 -> Calendar.FRIDAY
                6 -> Calendar.SATURDAY
                7 -> Calendar.SUNDAY
                else -> Calendar.MONDAY
            }
            
            // Set the calendar to the correct day of the week
            while (eventDate.get(Calendar.DAY_OF_WEEK) != calendarDay) {
                eventDate.add(Calendar.DAY_OF_YEAR, 1)
            }

            val startTime = eventDate.clone() as Calendar
            val classStart = Calendar.getInstance()
            classStart.time = classItem.startTime
            startTime.set(Calendar.HOUR_OF_DAY, classStart.get(Calendar.HOUR_OF_DAY))
            startTime.set(Calendar.MINUTE, classStart.get(Calendar.MINUTE))

            val endTime = eventDate.clone() as Calendar
            val classEnd = Calendar.getInstance()
            classEnd.time = classItem.endTime
            endTime.set(Calendar.HOUR_OF_DAY, classEnd.get(Calendar.HOUR_OF_DAY))
            endTime.set(Calendar.MINUTE, classEnd.get(Calendar.MINUTE))

            events.add(
                TimetableEvent(
                    id = classItem.id,
                    title = classItem.subject,
                    subtitle = classItem.roomNumber,
                    startTime = startTime,
                    endTime = endTime,
                    type = EventType.CLASS,
                    originalObject = classItemModel
                )
            )
        }
        return events
    }

    private fun createEventForMeeting(meeting: com.example.teacherscheduler.model.Meeting): TimetableEvent {
        val startTime = Calendar.getInstance()
        startTime.time = meeting.startDate
        val meetingStart = Calendar.getInstance()
        meetingStart.time = meeting.startTime
        startTime.set(Calendar.HOUR_OF_DAY, meetingStart.get(Calendar.HOUR_OF_DAY))
        startTime.set(Calendar.MINUTE, meetingStart.get(Calendar.MINUTE))

        val endTime = Calendar.getInstance()
        endTime.time = meeting.startDate
        val meetingEnd = Calendar.getInstance()
        meetingEnd.time = meeting.endTime
        endTime.set(Calendar.HOUR_OF_DAY, meetingEnd.get(Calendar.HOUR_OF_DAY))
        endTime.set(Calendar.MINUTE, meetingEnd.get(Calendar.MINUTE))

        return TimetableEvent(
            id = meeting.id,
            title = meeting.title,
            subtitle = meeting.location,
            startTime = startTime,
            endTime = endTime,
            type = EventType.MEETING,
            originalObject = meeting.toMeetingItem()
        )
    }

    // Helper extensions
    private fun com.example.teacherscheduler.model.Class.toClassItem(): ClassItem {
        return ClassItem(
            id = this.id,
            subject = this.subject,
            department = this.department,
            roomNumber = this.roomNumber,
            startDate = this.startDate,
            endDate = this.endDate,
            startTime = this.startTime,
            endTime = this.endTime,
            daysOfWeek = this.daysOfWeek,
            isRecurring = this.isRecurring,
            notificationsEnabled = this.notificationsEnabled,
            reminderMinutes = this.reminderMinutes,
            description = this.description,
            semesterId = this.semesterId
        )
    }

    private fun com.example.teacherscheduler.model.Meeting.toMeetingItem(): MeetingItem {
        return MeetingItem(
            id = this.id,
            title = this.title,
            with = this.withWhom,
            location = this.location,
            date = this.startDate,
            startTime = this.startTime,
            endTime = this.endTime,
            notificationsEnabled = this.notificationsEnabled,
            reminderMinutes = this.reminderMinutes,
            notes = this.notes,
            semesterId = this.semesterId
        )
    }
}
