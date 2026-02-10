package com.example.teacherscheduler.util

import android.content.Context
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import java.util.*

/**
 * Smart form helper that provides templates, suggestions, and auto-fill
 */
class SmartFormHelper(private val context: Context, private val repository: Repository) {

    // Class templates
    data class ClassTemplate(
        val name: String,
        val subject: String,
        val department: String,
        val duration: Int, // minutes
        val icon: String
    )

    // Meeting templates
    data class MeetingTemplate(
        val name: String,
        val title: String,
        val duration: Int, // minutes
        val location: String,
        val icon: String
    )

    // Subject suggestions
    fun getSubjectSuggestions(): List<String> {
        return listOf(
            "Mathematics",
            "Physics",
            "Chemistry",
            "Biology",
            "English Literature",
            "History",
            "Geography",
            "Computer Science",
            "Art",
            "Music",
            "Physical Education",
            "Economics",
            "Psychology",
            "Sociology",
            "Philosophy",
            "Foreign Language"
        )
    }

    // Department suggestions
    fun getDepartmentSuggestions(): List<String> {
        return listOf(
            "Science Department",
            "Mathematics Department",
            "Arts Department",
            "Humanities Department",
            "Languages Department",
            "Physical Education",
            "Technology Department",
            "Business Studies"
        )
    }

    // Room number suggestions
    fun getRoomSuggestions(): List<String> {
        return listOf(
            "101", "102", "103", "104", "105",
            "201", "202", "203", "204", "205",
            "301", "302", "303", "Lab 1", "Lab 2",
            "Conference Room", "Auditorium", "Library"
        )
    }

    // Meeting location suggestions
    fun getMeetingLocationSuggestions(): List<String> {
        return listOf(
            "Conference Room A",
            "Conference Room B",
            "Principal's Office",
            "Staff Room",
            "Library",
            "Online (Zoom)",
            "Online (Google Meet)",
            "Auditorium",
            "Cafeteria"
        )
    }

    // Class templates
    fun getClassTemplates(): List<ClassTemplate> {
        return listOf(
            ClassTemplate(
                "Lecture",
                "Mathematics",
                "Science Department",
                60,
                "📚"
            ),
            ClassTemplate(
                "Lab Session",
                "Physics Lab",
                "Science Department",
                90,
                "🔬"
            ),
            ClassTemplate(
                "Tutorial",
                "English Tutorial",
                "Arts Department",
                45,
                "✍️"
            ),
            ClassTemplate(
                "Seminar",
                "Research Seminar",
                "Academic Department",
                120,
                "🎓"
            ),
            ClassTemplate(
                "Workshop",
                "Skills Workshop",
                "Professional Development",
                60,
                "🛠️"
            )
        )
    }

    // Meeting templates
    fun getMeetingTemplates(): List<MeetingTemplate> {
        return listOf(
            MeetingTemplate(
                "Parent Meeting",
                "Parent-Teacher Conference",
                30,
                "Conference Room",
                "👨‍👩‍👧"
            ),
            MeetingTemplate(
                "Team Meeting",
                "Staff Team Meeting",
                60,
                "Staff Room",
                "👥"
            ),
            MeetingTemplate(
                "Department Meeting",
                "Department Review",
                90,
                "Conference Room A",
                "📋"
            ),
            MeetingTemplate(
                "One-on-One",
                "Student Consultation",
                20,
                "Office",
                "💬"
            ),
            MeetingTemplate(
                "Online Meeting",
                "Virtual Meeting",
                45,
                "Online (Zoom)",
                "💻"
            )
        )
    }

    // Auto-fill based on history
    suspend fun getAutoFillSuggestions(fieldType: String): List<String> {
        return when (fieldType) {
            "subject" -> {
                val classes = repository.getAllActiveClassesSync()
                classes.map { it.subject }.distinct().take(5)
            }
            "department" -> {
                val classes = repository.getAllActiveClassesSync()
                classes.map { it.department }.distinct().take(5)
            }
            "room" -> {
                val classes = repository.getAllActiveClassesSync()
                classes.map { it.roomNumber }.distinct().take(5)
            }
            "meeting_location" -> {
                val meetings = repository.getAllActiveMeetingsSync()
                meetings.map { it.location }.distinct().take(5)
            }
            "meeting_person" -> {
                val meetings = repository.getAllActiveMeetingsSync()
                meetings.map { it.withWhom }.distinct().take(5)
            }
            else -> emptyList()
        }
    }

    // Smart time suggestions based on existing schedule
    suspend fun suggestTimeSlots(date: Calendar): List<Pair<Calendar, Calendar>> {
        val suggestions = mutableListOf<Pair<Calendar, Calendar>>()

        // Common time slots
        val commonSlots = listOf(
            8 to 9,   // 8:00 - 9:00
            9 to 10,  // 9:00 - 10:00
            10 to 11, // 10:00 - 11:00
            11 to 12, // 11:00 - 12:00
            13 to 14, // 1:00 - 2:00
            14 to 15, // 2:00 - 3:00
            15 to 16, // 3:00 - 4:00
            16 to 17  // 4:00 - 5:00
        )

        // Get existing classes and meetings for the day
        val existingClasses = repository.getAllActiveClassesSync()
        val existingMeetings = repository.getAllActiveMeetingsSync()

        // Filter available slots
        commonSlots.forEach { (startHour, endHour) ->
            val start = date.clone() as Calendar
            start.set(Calendar.HOUR_OF_DAY, startHour)
            start.set(Calendar.MINUTE, 0)

            val end = date.clone() as Calendar
            end.set(Calendar.HOUR_OF_DAY, endHour)
            end.set(Calendar.MINUTE, 0)

            // Check if slot is available
            val isAvailable = !hasConflict(start, end, existingClasses, existingMeetings)

            if (isAvailable) {
                suggestions.add(Pair(start, end))
            }
        }

        return suggestions.take(5) // Return top 5 available slots
    }

    private fun hasConflict(
        start: Calendar,
        end: Calendar,
        classes: List<Class>,
        meetings: List<Meeting>
    ): Boolean {
        val startTime = start.timeInMillis
        val endTime = end.timeInMillis

        // Check class conflicts
        classes.forEach { classItem ->
            val classStart = classItem.startTime.time
            val classEnd = classItem.endTime.time

            if (startTime < classEnd && endTime > classStart) {
                return true
            }
        }

        // Check meeting conflicts
        meetings.forEach { meeting ->
            val meetingStart = meeting.startTime.time
            val meetingEnd = meeting.endTime.time

            if (startTime < meetingEnd && endTime > meetingStart) {
                return true
            }
        }

        return false
    }

    // Detect patterns and suggest recurring schedules
    suspend fun detectRecurringPattern(): List<Int> {
        val classes = repository.getAllActiveClassesSync()
        val dayFrequency = mutableMapOf<Int, Int>()

        classes.filter { it.isRecurring }.forEach { classItem ->
            classItem.daysOfWeek.forEach { day ->
                dayFrequency[day] = (dayFrequency[day] ?: 0) + 1
            }
        }

        // Return most common days
        return dayFrequency.entries
            .sortedByDescending { it.value }
            .take(3)
            .map { it.key }
    }

    // Smart department assignment based on subject
    fun suggestDepartment(subject: String): String {
        return when {
            subject.contains("Math", ignoreCase = true) -> "Mathematics Department"
            subject.contains("Phys", ignoreCase = true) -> "Science Department"
            subject.contains("Chem", ignoreCase = true) -> "Science Department"
            subject.contains("Bio", ignoreCase = true) -> "Science Department"
            subject.contains("English", ignoreCase = true) -> "Arts Department"
            subject.contains("History", ignoreCase = true) -> "Humanities Department"
            subject.contains("Geo", ignoreCase = true) -> "Humanities Department"
            subject.contains("Computer", ignoreCase = true) -> "Technology Department"
            subject.contains("Art", ignoreCase = true) -> "Arts Department"
            subject.contains("Music", ignoreCase = true) -> "Arts Department"
            subject.contains("PE", ignoreCase = true) || subject.contains("Physical", ignoreCase = true) -> "Physical Education"
            else -> "General Department"
        }
    }

    // Generate class code automatically
    fun generateClassCode(subject: String, roomNumber: String): String {
        val subjectCode = subject.take(3).uppercase(Locale.getDefault())
        val roomCode = roomNumber.filter { it.isDigit() }.take(3)
        val random = (100..999).random()
        return "$subjectCode-$roomCode-$random"
    }

    // Suggest meeting agenda based on title
    fun suggestMeetingAgenda(title: String): String {
        return when {
            title.contains("Parent", ignoreCase = true) -> "1. Student progress discussion\n2. Academic performance\n3. Behavioral updates\n4. Questions and concerns"
            title.contains("Team", ignoreCase = true) -> "1. Project updates\n2. Upcoming deadlines\n3. Resource allocation\n4. Action items"
            title.contains("Department", ignoreCase = true) -> "1. Review performance metrics\n2. Budget discussion\n3. Policy updates\n4. Planning ahead"
            title.contains("Student", ignoreCase = true) -> "1. Academic concerns\n2. Goal setting\n3. Support options\n4. Follow-up plan"
            else -> "1. Opening remarks\n2. Main discussion\n3. Decisions and action items\n4. Closing notes"
        }
    }
}

