package com.example.teacherscheduler.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notices")
data class Notice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val priority: NoticePriority = NoticePriority.NORMAL,
    val createdBy: String,
    val createdByName: String = "",
    val departmentId: Long,
    val publishedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

enum class NoticePriority {
    URGENT, HIGH, NORMAL, LOW
}

@Entity(
    tableName = "notice_seen_status",
    primaryKeys = ["noticeId", "userId"]
)
data class NoticeSeenStatus(
    val noticeId: Long,
    val userId: String,
    val seenAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "resources")
data class Resource(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val subject: String,
    val fileUrl: String,
    val fileType: String = "",
    val uploadedBy: String,
    val uploadedByName: String = "",
    val departmentId: Long,
    val uploadedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
