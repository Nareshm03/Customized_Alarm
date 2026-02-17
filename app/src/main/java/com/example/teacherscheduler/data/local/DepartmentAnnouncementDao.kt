package com.example.teacherscheduler.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.teacherscheduler.model.DepartmentAnnouncement

/**
 * DAO Extensions for HOD Dashboard Features
 * Add these methods to existing DAOs
 */

// ==================== ADD TO ToDoDao.kt ====================

/**
 * Get department tasks synchronously (for analytics)
 */
// @Query("""
//     SELECT * FROM todos 
//     WHERE departmentId = :departmentId 
//     AND isDepartmentTask = 1 
//     AND isActive = 1
//     ORDER BY dueDate ASC
// """)
// suspend fun getDepartmentTasksSync(departmentId: Long): List<ToDo>

/**
 * Get task count by status
 */
// @Query("""
//     SELECT COUNT(*) FROM todos 
//     WHERE departmentId = :departmentId 
//     AND status = :status 
//     AND isActive = 1
// """)
// suspend fun getTaskCountByStatus(departmentId: Long, status: String): Int

/**
 * Get overdue task count
 */
// @Query("""
//     SELECT COUNT(*) FROM todos 
//     WHERE departmentId = :departmentId 
//     AND isDepartmentTask = 1 
//     AND isCompleted = 0 
//     AND dueDate < :currentTime 
//     AND isActive = 1
// """)
// suspend fun getOverdueTaskCount(departmentId: Long, currentTime: Long): Int


// ==================== DepartmentAnnouncementDao.kt ====================

@Dao
interface DepartmentAnnouncementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(announcement: DepartmentAnnouncement): Long

    @Update
    suspend fun update(announcement: DepartmentAnnouncement)

    @Delete
    suspend fun delete(announcement: DepartmentAnnouncement)

    /**
     * Get all active announcements for department
     */
    @Query("""
        SELECT * FROM department_announcements 
        WHERE departmentId = :departmentId 
        AND isActive = 1 
        AND (expiresAt IS NULL OR expiresAt > :currentTime)
        ORDER BY isPinned DESC, publishedAt DESC
    """)
    fun getActiveAnnouncements(
        departmentId: Long,
        currentTime: Long = System.currentTimeMillis()
    ): LiveData<List<DepartmentAnnouncement>>

    /**
     * Get announcement by ID
     */
    @Query("SELECT * FROM department_announcements WHERE id = :id")
    suspend fun getById(id: Long): DepartmentAnnouncement?

    /**
     * Get pinned announcements
     */
    @Query("""
        SELECT * FROM department_announcements 
        WHERE departmentId = :departmentId 
        AND isPinned = 1 
        AND isActive = 1
        ORDER BY publishedAt DESC
    """)
    fun getPinnedAnnouncements(departmentId: Long): LiveData<List<DepartmentAnnouncement>>

    /**
     * Mark announcement as read by user
     */
    @Query("""
        UPDATE department_announcements 
        SET totalReaders = totalReaders + 1 
        WHERE id = :announcementId
    """)
    suspend fun markAsRead(announcementId: Long)

    /**
     * Delete expired announcements
     */
    @Query("""
        DELETE FROM department_announcements 
        WHERE expiresAt IS NOT NULL 
        AND expiresAt < :currentTime
    """)
    suspend fun deleteExpired(currentTime: Long = System.currentTimeMillis())
}
