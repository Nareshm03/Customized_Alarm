package com.example.teacherscheduler.util

import android.content.Context
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    fun exportClasses(context: Context, classes: List<Class>): File {
        val csv = StringBuilder()
        csv.append("Subject,Department,Room,Start Date,End Date,Start Time,End Time,Recurring,Active\n")
        
        classes.forEach { classItem ->
            csv.append("\"${classItem.subject}\",")
            csv.append("\"${classItem.department}\",")
            csv.append("\"${classItem.roomNumber}\",")
            csv.append("\"${dateFormat.format(classItem.startDate)}\",")
            csv.append("\"${dateFormat.format(classItem.endDate)}\",")
            csv.append("\"${dateFormat.format(classItem.startTime)}\",")
            csv.append("\"${dateFormat.format(classItem.endTime)}\",")
            csv.append("${classItem.isRecurring},")
            csv.append("${classItem.isActive}\n")
        }
        
        val fileName = "classes_export_${System.currentTimeMillis()}.csv"
        val file = File(context.getExternalFilesDir(null), fileName)
        file.writeText(csv.toString())
        
        return file
    }
    
    fun exportMeetings(context: Context, meetings: List<Meeting>): File {
        val csv = StringBuilder()
        csv.append("Title,With,Location,Notes,Start Date,End Date,Start Time,End Time,Active\n")
        
        meetings.forEach { meeting ->
            csv.append("\"${meeting.title}\",")
            csv.append("\"${meeting.withWhom}\",")
            csv.append("\"${meeting.location}\",")
            csv.append("\"${meeting.notes}\",")
            csv.append("\"${dateFormat.format(meeting.startDate)}\",")
            csv.append("\"${dateFormat.format(meeting.endDate)}\",")
            csv.append("\"${dateFormat.format(meeting.startTime)}\",")
            csv.append("\"${dateFormat.format(meeting.endTime)}\",")
            csv.append("${meeting.isActive}\n")
        }
        
        val fileName = "meetings_export_${System.currentTimeMillis()}.csv"
        val file = File(context.getExternalFilesDir(null), fileName)
        file.writeText(csv.toString())
        
        return file
    }
}
