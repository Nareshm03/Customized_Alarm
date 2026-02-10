package com.example.teacherscheduler.util

import com.example.teacherscheduler.model.Class
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class ConflictDetectorTest {
    
    @Test
    fun testNoConflict() {
        val class1 = createClass(9, 0, 10, 0)
        val class2 = createClass(11, 0, 12, 0)
        
        val conflicts = ConflictDetector.checkClassConflicts(class1, listOf(class2))
        assertTrue(conflicts.isEmpty())
    }
    
    @Test
    fun testOverlapConflict() {
        val class1 = createClass(9, 0, 11, 0)
        val class2 = createClass(10, 0, 12, 0)
        
        val conflicts = ConflictDetector.checkClassConflicts(class1, listOf(class2))
        assertEquals(1, conflicts.size)
    }
    
    private fun createClass(startHour: Int, startMin: Int, endHour: Int, endMin: Int): Class {
        val startTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, startMin)
        }.time
        
        val endTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, endMin)
        }.time
        
        return Class(
            subject = "Test",
            department = "CS",
            roomNumber = "101",
            startDate = Date(),
            endDate = Date(),
            startTime = startTime,
            endTime = endTime
        )
    }
}
