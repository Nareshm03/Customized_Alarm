package com.example.teacherscheduler.util

import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import java.util.*

object GoogleCalendarSync {
    
    fun syncClassToCalendar(context: Context, classItem: Class): Long? {
        val calendarId = getPrimaryCalendarId(context) ?: return null
        
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, "${classItem.title} - ${classItem.department}")
            put(CalendarContract.Events.DESCRIPTION, "Room: ${classItem.room}")
            put(CalendarContract.Events.EVENT_LOCATION, classItem.room)
            put(CalendarContract.Events.DTSTART, classItem.getStartDateTime())
            put(CalendarContract.Events.DTEND, classItem.getEndDateTime())
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            
            if (classItem.isRecurring) {
                put(CalendarContract.Events.RRULE, buildRecurrenceRule(classItem.daysOfWeek))
            }
        }
        
        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        return uri?.lastPathSegment?.toLongOrNull()
    }
    
    fun syncMeetingToCalendar(context: Context, meeting: Meeting): Long? {
        val calendarId = getPrimaryCalendarId(context) ?: return null
        
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calendarId)
            put(CalendarContract.Events.TITLE, meeting.title)
            put(CalendarContract.Events.DESCRIPTION, "With: ${meeting.withWhom}\n${meeting.notes}")
            put(CalendarContract.Events.EVENT_LOCATION, meeting.location)
            put(CalendarContract.Events.DTSTART, meeting.getStartDateTime())
            put(CalendarContract.Events.DTEND, meeting.getEndDateTime())
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
        }
        
        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        return uri?.lastPathSegment?.toLongOrNull()
    }
    
    private fun getPrimaryCalendarId(context: Context): Long? {
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val selection = "${CalendarContract.Calendars.IS_PRIMARY} = 1"
        
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getLong(0)
            }
        }
        return null
    }
    
    private fun buildRecurrenceRule(daysOfWeek: List<Int>): String {
        val days = daysOfWeek.map { day ->
            when (day) {
                Calendar.SUNDAY -> "SU"
                Calendar.MONDAY -> "MO"
                Calendar.TUESDAY -> "TU"
                Calendar.WEDNESDAY -> "WE"
                Calendar.THURSDAY -> "TH"
                Calendar.FRIDAY -> "FR"
                Calendar.SATURDAY -> "SA"
                else -> ""
            }
        }.joinToString(",")
        
        return "FREQ=WEEKLY;BYDAY=$days"
    }
}
