package com.example.teacherscheduler.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*
import com.example.teacherscheduler.viewmodel.ClassesViewModel
import com.example.teacherscheduler.viewmodel.MeetingViewModel
import com.example.teacherscheduler.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    classesViewModel: ClassesViewModel = hiltViewModel(),
    meetingsViewModel: MeetingViewModel = hiltViewModel(),
    onAddClass: () -> Unit = {},
    onAddMeeting: () -> Unit = {}
) {
    val classesState by classesViewModel.uiState.collectAsStateWithLifecycle()
    val meetingsState by meetingsViewModel.uiState.collectAsStateWithLifecycle()

    val isLoading = classesState is UiState.Loading || meetingsState is UiState.Loading

    val classes = when (classesState) {
        is UiState.Success -> (classesState as UiState.Success).data.classes
        else -> emptyList()
    }

    val meetings = when (meetingsState) {
        is UiState.Success -> (meetingsState as UiState.Success).data.meetings
        else -> emptyList()
    }

    val weekDays = getWeekDays()
    val groupedData = groupByDay(classes, meetings, weekDays)

    Scaffold(
        containerColor = BackgroundPrimary,
        topBar = {
            SoftTopAppBar(
                title = "Schedule",
                actions = {
                    IconButton(onClick = onAddClass) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Add Class",
                            tint = TextPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Primary,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            groupedData.all { it.value.isEmpty() } -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = OutlineLight
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Nothing scheduled this week",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add a class or meeting to get started",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    PrimaryButton(
                        text = "Add Class",
                        onClick = onAddClass,
                        icon = Icons.Outlined.Add,
                        modifier = Modifier.fillMaxWidth(0.6f)
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(
                        start = AppSpacing.screenHorizontal,
                        end = AppSpacing.screenHorizontal,
                        top = AppSpacing.largeSpacing,
                        bottom = 100.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sectionSpacing)
                ) {
                    item {
                        Text(
                            text = "This Week",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextPrimary
                        )
                    }

                    items(weekDays) { day ->
                        val dayData = groupedData[day] ?: emptyList()
                        if (dayData.isNotEmpty()) {
                            DaySection(day = day, items = dayData)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DaySection(day: String, items: List<TimetableItem>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = TextPrimary
        )

        items.sortedBy { it.startTime }.forEach { item ->
            when (item) {
                is TimetableItem.ClassItem -> ClassCard(item.classItem)
                is TimetableItem.MeetingItem -> MeetingCard(item.meeting)
            }
        }
    }
}

@Composable
private fun ClassCard(classItem: Class) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.card),
        color = PrimaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(Primary, RoundedCornerShape(2.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = classItem.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "Room ${classItem.room}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Text(
                text = classItem.getFormattedTime(),
                style = MaterialTheme.typography.labelMedium,
                color = Primary
            )
        }
    }
}

@Composable
private fun MeetingCard(meeting: Meeting) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AppRadius.card),
        color = TextSecondary.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(TextSecondary, RoundedCornerShape(2.dp))
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = meeting.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextPrimary
                )
                Text(
                    text = meeting.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Text(
                text = meeting.getFormattedTime(),
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
    }
}

private fun getWeekDays(): List<String> {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
    
    return (0..6).map {
        val day = dateFormat.format(calendar.time)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        day
    }
}

private fun groupByDay(
    classes: List<Class>,
    meetings: List<Meeting>,
    weekDays: List<String>
): Map<String, List<TimetableItem>> {
    val calendar = Calendar.getInstance()
    // Set to start of week (Monday)
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val startOfWeek = calendar.timeInMillis

    // End of week (Sunday end)
    val endCal = calendar.clone() as Calendar
    endCal.add(Calendar.DAY_OF_WEEK, 7)
    val endOfWeek = endCal.timeInMillis

    val dateFormat = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault())
    val result = mutableMapOf<String, MutableList<TimetableItem>>()

    // Build a map of day-of-week to formatted day string for the current week
    val dayOfWeekToFormattedDay = mutableMapOf<Int, String>()
    val tempCal = Calendar.getInstance()
    tempCal.timeInMillis = startOfWeek
    for (i in 0..6) {
        dayOfWeekToFormattedDay[tempCal.get(Calendar.DAY_OF_WEEK)] = dateFormat.format(tempCal.time)
        tempCal.add(Calendar.DAY_OF_YEAR, 1)
    }

    classes.forEach { classItem ->
        if (classItem.isRecurring && classItem.daysOfWeek.isNotEmpty()) {
            // For recurring classes, add to each matching day of the week
            classItem.daysOfWeek.forEach { dayOfWeek ->
                val dayStr = dayOfWeekToFormattedDay[dayOfWeek]
                if (dayStr != null) {
                    result.getOrPut(dayStr) { mutableListOf() }.add(TimetableItem.ClassItem(classItem))
                }
            }
        } else {
            // For non-recurring classes, check if the date falls in this week
            val classTime = classItem.startDate.time
            if (classTime in startOfWeek until endOfWeek) {
                val day = dateFormat.format(classItem.startDate)
                result.getOrPut(day) { mutableListOf() }.add(TimetableItem.ClassItem(classItem))
            }
        }
    }

    meetings.forEach { meeting ->
        val meetingTime = meeting.startDate.time
        if (meetingTime in startOfWeek until endOfWeek) {
            val day = dateFormat.format(meeting.startDate)
            result.getOrPut(day) { mutableListOf() }.add(TimetableItem.MeetingItem(meeting))
        }
    }

    return result
}

private sealed class TimetableItem {
    abstract val startTime: Long
    
    data class ClassItem(val classItem: Class) : TimetableItem() {
        override val startTime: Long = classItem.startDate.time
    }
    
    data class MeetingItem(val meeting: Meeting) : TimetableItem() {
        override val startTime: Long = meeting.startDate.time
    }
}
