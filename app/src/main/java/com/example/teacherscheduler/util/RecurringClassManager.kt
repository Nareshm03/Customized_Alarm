package com.example.teacherscheduler.util

import com.example.teacherscheduler.model.Class
import java.util.*

object RecurringClassManager {
    
    fun generateRecurringClasses(
        template: Class,
        daysOfWeek: List<Int>,
        numberOfWeeks: Int = 16
    ): List<Class> {
        val classes = mutableListOf<Class>()
        val startCal = Calendar.getInstance().apply {
            time = template.startDate
        }
        
        for (week in 0 until numberOfWeeks) {
            daysOfWeek.forEach { dayOfWeek ->
                val classCal = startCal.clone() as Calendar
                classCal.add(Calendar.WEEK_OF_YEAR, week)
                classCal.set(Calendar.DAY_OF_WEEK, dayOfWeek)
                
                val newClass = template.copy(
                    id = 0,
                    startDate = Date(classCal.timeInMillis),
                    endDate = Date(classCal.timeInMillis),
                    isRecurring = true,
                    daysOfWeek = daysOfWeek
                )
                
                classes.add(newClass)
            }
        }
        
        return classes
    }
    
    fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Sunday"
            Calendar.MONDAY -> "Monday"
            Calendar.TUESDAY -> "Tuesday"
            Calendar.WEDNESDAY -> "Wednesday"
            Calendar.THURSDAY -> "Thursday"
            Calendar.FRIDAY -> "Friday"
            Calendar.SATURDAY -> "Saturday"
            else -> "Unknown"
        }
    }
}
