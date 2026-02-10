package com.example.teacherscheduler.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import com.example.teacherscheduler.model.ToDo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DashboardUiState(
    val greeting: String = "",
    val todayClassesCount: Int = 0,
    val upcomingMeetingsCount: Int = 0,
    val activeToDosCount: Int = 0,
    val todayClasses: List<Class> = emptyList(),
    val upcomingMeetings: List<Meeting> = emptyList(),
    val urgentToDos: List<ToDo> = emptyList(),
    val isLoading: Boolean = false,
    // Enhanced statistics
    val todayHours: Double = 0.0,
    val weekClassesCount: Int = 0,
    val weekMeetingsCount: Int = 0,
    val weekHours: Double = 0.0,
    val completedTodayCount: Int = 0,
    val totalTodayCount: Int = 0,
    val nextEventTitle: String = "",
    val nextEventTime: String = "",
    val insights: List<String> = emptyList(),
    val productivityScore: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    
    private val _selectedDate = MutableStateFlow(Calendar.getInstance().time)
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()
    
    val dashboardState: StateFlow<DashboardUiState> = combine(
        _selectedDate,
        repository.getAllActiveClasses(),
        repository.getAllActiveMeetings(),
        repository.getAllActiveToDos()
    ) { date, classes, meetings, todos ->
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val greeting = when (hour) {
            in 5..11 -> "Good Morning!"
            in 12..16 -> "Good Afternoon!"
            in 17..20 -> "Good Evening!"
            else -> "Good Night!"
        }
        
        val todayClasses = filterTodayClasses(classes)
        val upcomingMeetings = filterUpcomingMeetings(meetings)
        val weekClasses = filterWeekClasses(classes)
        val weekMeetings = filterWeekMeetings(meetings)
        val urgentToDos = todos.filter { it.priority == ToDo.Priority.URGENT || it.priority == ToDo.Priority.HIGH }.take(3)
        val activeToDosCount = todos.size

        // Calculate hours
        val todayHours = calculateTotalHours(todayClasses, emptyList())
        val weekHours = calculateTotalHours(weekClasses, weekMeetings)

        // Calculate productivity
        val totalToday = todayClasses.size + upcomingMeetings.filter {
            isToday(it.startDate)
        }.size
        val productivityScore = if (totalToday > 0) {
            ((todayClasses.size.toFloat() / totalToday) * 100).toInt()
        } else 100

        // Get next event
        val allUpcoming = (todayClasses.map { "Class: ${it.subject}" to it.startTime } +
                          upcomingMeetings.map { "Meeting: ${it.title}" to it.startTime })
            .sortedBy { it.second }

        val nextEvent = allUpcoming.firstOrNull()

        // Generate insights
        val insights = generateInsights(todayClasses.size, upcomingMeetings.size, activeToDosCount, weekClasses.size, todayHours)

        DashboardUiState(
            greeting = greeting,
            todayClassesCount = todayClasses.size,
            upcomingMeetingsCount = upcomingMeetings.size,
            activeToDosCount = activeToDosCount,
            todayClasses = todayClasses,
            upcomingMeetings = upcomingMeetings,
            urgentToDos = urgentToDos,
            todayHours = todayHours,
            weekClassesCount = weekClasses.size,
            weekMeetingsCount = weekMeetings.size,
            weekHours = weekHours,
            completedTodayCount = 0, // TODO: Track completion
            totalTodayCount = totalToday,
            nextEventTitle = nextEvent?.first ?: "No upcoming events",
            nextEventTime = nextEvent?.let { formatTime(it.second) } ?: "",
            insights = insights,
            productivityScore = productivityScore
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )
    
    private fun filterWeekClasses(classes: List<Class>): List<Class> {
        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val endOfWeek = startOfWeek.clone() as Calendar
        endOfWeek.add(Calendar.DAY_OF_WEEK, 7)

        return classes.filter { classItem ->
            if (classItem.isRecurring) {
                true // Recurring classes are always in the week
            } else {
                val classTime = classItem.startDate.time
                classTime >= startOfWeek.timeInMillis && classTime < endOfWeek.timeInMillis
            }
        }
    }

    private fun filterWeekMeetings(meetings: List<Meeting>): List<Meeting> {
        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        val endOfWeek = startOfWeek.clone() as Calendar
        endOfWeek.add(Calendar.DAY_OF_WEEK, 7)

        return meetings.filter { meeting ->
            val meetingTime = meeting.startDate.time
            meetingTime >= startOfWeek.timeInMillis && meetingTime < endOfWeek.timeInMillis
        }
    }

    private fun calculateTotalHours(classes: List<Class>, meetings: List<Meeting>): Double {
        var totalMinutes = 0L

        classes.forEach { classItem ->
            val duration = classItem.endTime.time - classItem.startTime.time
            totalMinutes += duration / (1000 * 60)
        }

        meetings.forEach { meeting ->
            val duration = meeting.endTime.time - meeting.startTime.time
            totalMinutes += duration / (1000 * 60)
        }

        return totalMinutes / 60.0
    }

    private fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val checkDate = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == checkDate.get(Calendar.YEAR) &&
               today.get(Calendar.DAY_OF_YEAR) == checkDate.get(Calendar.DAY_OF_YEAR)
    }

    private fun formatTime(date: Date): String {
        val format = SimpleDateFormat("h:mm a", Locale.getDefault())
        return format.format(date)
    }

    private fun generateInsights(todayClasses: Int, upcomingMeetings: Int, activeToDos: Int, weekClasses: Int, todayHours: Double): List<String> {
        val insights = mutableListOf<String>()

        when {
            todayClasses == 0 && upcomingMeetings == 0 -> {
                insights.add("📅 You have a free day ahead!")
            }
            todayClasses >= 5 -> {
                insights.add("📚 Busy day with $todayClasses classes")
            }
            todayHours >= 6 -> {
                insights.add("⏰ ${String.format(Locale.getDefault(), "%.1f", todayHours)} hours of teaching today")
            }
        }

        if (weekClasses >= 20) {
            insights.add("💪 Productive week with $weekClasses classes planned")
        }

        if (upcomingMeetings > 0) {
            insights.add("🤝 You have $upcomingMeetings upcoming meeting${if (upcomingMeetings > 1) "s" else ""}")
        }
        
        if (activeToDos > 0) {
            insights.add("📝 You have $activeToDos active tasks to complete")
        }

        val dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        if (dayOfWeek == Calendar.FRIDAY) {
            insights.add("🎉 Weekend is almost here!")
        }

        if (insights.isEmpty()) {
            insights.add("✨ Great day for planning ahead")
        }

        return insights.take(3) // Return max 3 insights
    }

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
    
    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
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
