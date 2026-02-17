package com.example.teacherscheduler.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * DepartmentMember - Junction table for Department-User relationship
 *
 * Represents the membership of a user in a department
 * - Links users to departments
 * - Tracks role within department (HOD or TEACHER)
 * - Manages join/leave dates
 */
@Entity(
    tableName = "department_members",
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
        Index(value = ["userId"]),
        Index(value = ["departmentId", "userId"], unique = true),
        Index(value = ["role"]),
        Index(value = ["isActive"])
    ]
)
data class DepartmentMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Relationship
    val departmentId: Long,                  // Department ID
    val userId: String,                      // User ID (from UserProfile)

    // Member Information
    val userName: String = "",               // Cached user name
    val userEmail: String = "",              // Cached user email
    val role: String = UserRole.TEACHER.name, // Role in department (HOD or TEACHER)

    // Status
    val isActive: Boolean = true,            // Active member
    val joinedDate: Long = System.currentTimeMillis(),
    val leftDate: Long? = null,              // Date when left department

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
        userId = "",
        userName = "",
        userEmail = "",
        role = UserRole.TEACHER.name,
        isActive = true,
        joinedDate = System.currentTimeMillis(),
        leftDate = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    /**
     * Check if member is HOD
     */
    fun isHOD(): Boolean = role == UserRole.HOD.name

    /**
     * Get role enum
     */
    fun getUserRole(): UserRole = UserRole.fromString(role)
}

