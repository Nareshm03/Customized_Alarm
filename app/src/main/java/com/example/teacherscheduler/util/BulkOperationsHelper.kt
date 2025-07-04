package com.example.teacherscheduler.util

import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.ClassTemplate
import com.example.teacherscheduler.model.Meeting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class BulkOperationsHelper(private val repository: Repository) {

    /**
     * Create multiple classes from a template for specified dates
     */
    suspend fun createClassesFromTemplate(
        template: ClassTemplate,
        dates: List<Long>, // List of start date times
        onProgress: ((Int, Int) -> Unit)? = null
    ): Result<List<Class>> = withContext(Dispatchers.IO) {
        try {
            val createdClasses = mutableListOf<Class>()
            
            dates.forEachIndexed { index, startDateTime ->
                val classItem = template.createClass(startDateTime)
                val classId = repository.insertClass(classItem)
                createdClasses.add(classItem.copy(id = classId))
                
                onProgress?.invoke(index + 1, dates.size)
            }
            
            Result.success(createdClasses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Create recurring classes for a semester
     */
    suspend fun createRecurringClasses(
        template: ClassTemplate,
        startDate: Calendar,
        endDate: Calendar,
        daysOfWeek: List<Int>, // Calendar.MONDAY, etc.
        timeOfDay: Pair<Int, Int>, // hour, minute
        onProgress: ((Int, Int) -> Unit)? = null
    ): Result<List<Class>> = withContext(Dispatchers.IO) {
        try {
            val dates = generateRecurringDates(startDate, endDate, daysOfWeek, timeOfDay)
            createClassesFromTemplate(template, dates, onProgress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Bulk delete classes by IDs
     */
    suspend fun deleteClasses(
        classIds: List<Long>,
        onProgress: ((Int, Int) -> Unit)? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var deletedCount = 0
            
            classIds.forEachIndexed { index, classId ->
                try {
                    val classItem = repository.getClassById(classId)
                    classItem?.let {
                        repository.deleteClass(it)
                        deletedCount++
                    }
                } catch (e: Exception) {
                    // Continue with other deletions even if one fails
                }
                
                onProgress?.invoke(index + 1, classIds.size)
            }
            
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Bulk update notification settings for classes
     */
    suspend fun updateClassNotifications(
        classIds: List<Long>,
        notificationsEnabled: Boolean,
        onProgress: ((Int, Int) -> Unit)? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var updatedCount = 0
            
            classIds.forEachIndexed { index, classId ->
                try {
                    val classItem = repository.getClassById(classId)
                    classItem?.let {
                        val updatedClass = it.copy(notificationsEnabled = notificationsEnabled)
                        repository.updateClass(updatedClass)
                        updatedCount++
                    }
                } catch (e: Exception) {
                    // Continue with other updates even if one fails
                }
                
                onProgress?.invoke(index + 1, classIds.size)
            }
            
            Result.success(updatedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Bulk reschedule classes by adding/subtracting time
     */
    suspend fun rescheduleClasses(
        classIds: List<Long>,
        timeOffsetMinutes: Int, // positive to move forward, negative to move backward
        onProgress: ((Int, Int) -> Unit)? = null
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            var rescheduledCount = 0
            val offsetMillis = timeOffsetMinutes * 60 * 1000L
            
            classIds.forEachIndexed { index, classId ->
                try {
                    val classItem = repository.getClassById(classId)
                    classItem?.let {
                        // For rescheduling, we need to update the Date objects
                        val newStartDate = Date(it.startDate.time + offsetMillis)
                        val newEndDate = Date(it.endDate.time + offsetMillis)
                        
                        val updatedClass = it.copy(
                            startDate = newStartDate,
                            endDate = newEndDate
                        )
                        repository.updateClass(updatedClass)
                        rescheduledCount++
                    }
                } catch (e: Exception) {
                    // Continue with other reschedules even if one fails
                }
                
                onProgress?.invoke(index + 1, classIds.size)
            }
            
            Result.success(rescheduledCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export classes to CSV format
     */
    suspend fun exportClassesToCsv(
        classes: List<Class>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val csvBuilder = StringBuilder()
            
            // Header
            csvBuilder.appendLine("Subject,Department,Room,Start Date,Start Time,End Date,End Time,Notifications Enabled")
            
            // Data rows
            classes.forEach { classItem ->
                val startDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(classItem.startDate)
                val startTimeStr = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(classItem.startTime)
                val endDateStr = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(classItem.endDate)
                val endTimeStr = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(classItem.endTime)
                
                csvBuilder.appendLine(
                    "${classItem.subject},${classItem.department},${classItem.roomNumber}," +
                    "$startDateStr,$startTimeStr,$endDateStr,$endTimeStr,${classItem.notificationsEnabled}"
                )
            }
            
            Result.success(csvBuilder.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateRecurringDates(
        startDate: Calendar,
        endDate: Calendar,
        daysOfWeek: List<Int>,
        timeOfDay: Pair<Int, Int>
    ): List<Long> {
        val dates = mutableListOf<Long>()
        val current = startDate.clone() as Calendar
        
        while (current.before(endDate) || current.equals(endDate)) {
            if (daysOfWeek.contains(current.get(Calendar.DAY_OF_WEEK))) {
                val classDateTime = current.clone() as Calendar
                classDateTime.set(Calendar.HOUR_OF_DAY, timeOfDay.first)
                classDateTime.set(Calendar.MINUTE, timeOfDay.second)
                classDateTime.set(Calendar.SECOND, 0)
                classDateTime.set(Calendar.MILLISECOND, 0)
                
                dates.add(classDateTime.timeInMillis)
            }
            current.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return dates
    }
}