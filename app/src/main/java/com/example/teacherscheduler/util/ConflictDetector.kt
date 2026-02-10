package com.example.teacherscheduler.util

import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting

object ConflictDetector {
    
    data class Conflict(
        val type: String,
        val title: String,
        val time: String
    )
    
    fun checkClassConflicts(
        newClass: Class,
        existingClasses: List<Class>,
        excludeId: Long = -1
    ): List<Conflict> {
        val conflicts = mutableListOf<Conflict>()
        val newStart = newClass.getStartDateTime()
        val newEnd = newClass.getEndDateTime()
        
        existingClasses.filter { it.id != excludeId && it.isActive }.forEach { existing ->
            val existingStart = existing.getStartDateTime()
            val existingEnd = existing.getEndDateTime()
            
            if (hasTimeOverlap(newStart, newEnd, existingStart, existingEnd)) {
                conflicts.add(
                    Conflict(
                        type = "Class",
                        title = existing.subject,
                        time = existing.getFormattedTime()
                    )
                )
            }
        }
        
        return conflicts
    }
    
    fun checkMeetingConflicts(
        newMeeting: Meeting,
        existingMeetings: List<Meeting>,
        existingClasses: List<Class>,
        excludeId: Long = -1
    ): List<Conflict> {
        val conflicts = mutableListOf<Conflict>()
        val newStart = newMeeting.getStartDateTime()
        val newEnd = newMeeting.getEndDateTime()
        
        existingMeetings.filter { it.id != excludeId && it.isActive }.forEach { existing ->
            val existingStart = existing.getStartDateTime()
            val existingEnd = existing.getEndDateTime()
            
            if (hasTimeOverlap(newStart, newEnd, existingStart, existingEnd)) {
                conflicts.add(
                    Conflict(
                        type = "Meeting",
                        title = existing.title,
                        time = existing.getFormattedTime()
                    )
                )
            }
        }
        
        existingClasses.filter { it.isActive }.forEach { existing ->
            val existingStart = existing.getStartDateTime()
            val existingEnd = existing.getEndDateTime()
            
            if (hasTimeOverlap(newStart, newEnd, existingStart, existingEnd)) {
                conflicts.add(
                    Conflict(
                        type = "Class",
                        title = existing.subject,
                        time = existing.getFormattedTime()
                    )
                )
            }
        }
        
        return conflicts
    }
    
    private fun hasTimeOverlap(start1: Long, end1: Long, start2: Long, end2: Long): Boolean {
        return start1 < end2 && end1 > start2
    }
}
