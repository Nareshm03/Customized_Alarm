package com.example.teacherscheduler.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * DepartmentAnnouncement - Announcements for department members
 *
 * Enables HOD to:
 * - Broadcast messages to all department teachers
 * - Schedule important announcements
 * - Track who has read the announcement
 *
 * Use cases:
 * - Department meetings
 * - Policy updates
 * - Event notifications
 * - General communication
 */
@Entity(
    tableName = "department_announcements",
    foreignKeys = [
        ForeignKey(
            entity = Department::class,
            parentColumns = ["id"],
            childColumns = ["departmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["departmentId"]),
        Index(value = ["createdBy"]),
        Index(value = ["isActive"]),
        Index(value = ["priority"]),
        Index(value = ["publishedAt"])
    ]
)
data class DepartmentAnnouncement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Department reference
    val departmentId: Long,

    // Content
    val title: String,
    val message: String,
    val priority: AnnouncementPriority = AnnouncementPriority.NORMAL,

    // Creator information
    val createdBy: String,                   // User ID of creator (usually HOD)
    val createdByName: String = "",          // Cached creator name

    // Publishing
    val publishedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null,            // Optional expiration date

    // Status
    val isActive: Boolean = true,
    val isPinned: Boolean = false,           // Pinned announcements show at top

    // Statistics
    val totalReaders: Int = 0,               // Number of members who read it
    val totalMembers: Int = 0,               // Total members when published

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * No-arg constructor for Room
     */
    constructor() : this(
        id = 0,
        departmentId = 0,
        title = "",
        message = "",
        priority = AnnouncementPriority.NORMAL,
        createdBy = "",
        createdByName = "",
        publishedAt = System.currentTimeMillis(),
        expiresAt = null,
        isActive = true,
        isPinned = false,
        totalReaders = 0,
        totalMembers = 0,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    /**
     * Check if announcement has expired
     */
    fun isExpired(): Boolean {
        return expiresAt?.let { it < System.currentTimeMillis() } ?: false
    }

    /**
     * Get read percentage
     */
    fun getReadPercentage(): Int {
        return if (totalMembers > 0) {
            (totalReaders * 100) / totalMembers
        } else {
            0
        }
    }

    /**
     * Get priority color
     */
    fun getPriorityColor(): String = when (priority) {
        AnnouncementPriority.URGENT -> "#F44336"    // Red
        AnnouncementPriority.HIGH -> "#FF9800"      // Orange
        AnnouncementPriority.NORMAL -> "#4CAF50"    // Green
        AnnouncementPriority.LOW -> "#2196F3"       // Blue
    }
}

/**
 * Priority levels for announcements
 */
enum class AnnouncementPriority {
    URGENT,     // Critical, immediate attention
    HIGH,       // Important, read soon
    NORMAL,     // Standard announcement
    LOW         // FYI, optional reading
}

