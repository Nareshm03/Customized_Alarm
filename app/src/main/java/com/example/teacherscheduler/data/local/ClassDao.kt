package com.example.teacherscheduler.data.local

import androidx.room.*
import com.example.teacherscheduler.model.Class
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(classItem: Class): Long
    
    @Update
    suspend fun update(classItem: Class)
    
    @Delete
    suspend fun delete(classItem: Class)
    
    @Query("SELECT * FROM classes WHERE isActive = 1 ORDER BY startTime")
    fun getAllActiveClasses(): Flow<List<Class>>
    
    @Query("SELECT * FROM classes WHERE isActive = 1 ORDER BY startTime LIMIT :limit OFFSET :offset")
    suspend fun getActiveClassesPaged(limit: Int, offset: Int): List<Class>
    
    @Query("SELECT * FROM classes WHERE isActive = 1 ORDER BY startTime")
    suspend fun getAllActiveClassesSync(): List<Class>
    
    @Query("SELECT * FROM classes WHERE id = :id")
    suspend fun getClassById(id: Long): Class?
    
    @Query("SELECT * FROM classes WHERE strftime('%Y-%m-%d', startDate / 1000, 'unixepoch') = :dateString AND isActive = 1 ORDER BY startTime")
    fun getClassesForDay(dateString: String): Flow<List<Class>>
    
    @Query("SELECT * FROM classes WHERE lastSyncTimestamp < :timestamp")
    suspend fun getUnsyncedClasses(timestamp: Long): List<Class>
    
    @Query("SELECT * FROM classes WHERE isActive = 1 AND startDate >= :startTime AND endDate <= :endTime ORDER BY startTime")
    suspend fun getClassesForDateRange(startTime: Long, endTime: Long): List<Class>
}