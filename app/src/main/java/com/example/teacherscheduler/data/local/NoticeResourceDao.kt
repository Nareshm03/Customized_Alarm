package com.example.teacherscheduler.data.local

import androidx.room.*
import com.example.teacherscheduler.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices WHERE departmentId = :departmentId AND isActive = 1 ORDER BY publishedAt DESC")
    fun getNoticesByDepartment(departmentId: Long): Flow<List<Notice>>
    
    @Query("SELECT COUNT(*) FROM notices n WHERE n.departmentId = :departmentId AND n.isActive = 1 AND n.id NOT IN (SELECT noticeId FROM notice_seen_status WHERE userId = :userId)")
    fun getUnseenCount(departmentId: Long, userId: String): Flow<Int>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notice: Notice): Long
    
    @Update
    suspend fun update(notice: Notice)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun markAsSeen(status: NoticeSeenStatus)
    
    @Query("SELECT * FROM notice_seen_status WHERE noticeId = :noticeId AND userId = :userId")
    suspend fun getSeenStatus(noticeId: Long, userId: String): NoticeSeenStatus?
}

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resources WHERE departmentId = :departmentId AND isActive = 1 ORDER BY uploadedAt DESC")
    fun getResourcesByDepartment(departmentId: Long): Flow<List<Resource>>
    
    @Query("SELECT * FROM resources WHERE subject = :subject AND isActive = 1 ORDER BY uploadedAt DESC")
    fun getResourcesBySubject(subject: String): Flow<List<Resource>>
    
    @Query("SELECT * FROM resources WHERE (title LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%') AND isActive = 1")
    fun searchResources(query: String): Flow<List<Resource>>
    
    @Query("SELECT DISTINCT subject FROM resources WHERE isActive = 1 ORDER BY subject")
    fun getAllSubjects(): Flow<List<String>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(resource: Resource): Long
    
    @Update
    suspend fun update(resource: Resource)
    
    @Delete
    suspend fun delete(resource: Resource)
}
