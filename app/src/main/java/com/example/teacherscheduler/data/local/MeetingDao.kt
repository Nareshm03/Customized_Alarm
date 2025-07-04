package com.example.teacherscheduler.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.teacherscheduler.model.Meeting
import java.util.Date

@Dao
interface MeetingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meeting: Meeting): Long
    
    @Update
    suspend fun update(meeting: Meeting)
    
    @Delete
    suspend fun delete(meeting: Meeting)
    
    @Query("SELECT * FROM meetings WHERE isActive = 1 ORDER BY startTime")
    fun getAllActiveMeetings(): LiveData<List<Meeting>>
    
    @Query("SELECT * FROM meetings WHERE isActive = 1 ORDER BY startTime")
    suspend fun getAllActiveMeetingsSync(): List<Meeting>
    
    @Query("SELECT * FROM meetings WHERE id = :id")
    suspend fun getMeetingById(id: Long): Meeting?
    
    @Query("SELECT * FROM meetings WHERE strftime('%Y-%m-%d', startDate / 1000, 'unixepoch') = :dateString AND isActive = 1 ORDER BY startTime")
    fun getMeetingsForDay(dateString: String): LiveData<List<Meeting>>
    
    @Query("SELECT * FROM meetings WHERE lastSyncTimestamp < :timestamp")
    suspend fun getUnsyncedMeetings(timestamp: Long): List<Meeting>
    
    @Query("SELECT * FROM meetings WHERE isActive = 1 AND startDate >= :startTime AND endDate <= :endTime ORDER BY startTime")
    suspend fun getMeetingsForDateRange(startTime: Long, endTime: Long): List<Meeting>
}