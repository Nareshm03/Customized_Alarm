package com.example.teacherscheduler.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Department - First-class entity in the system
 *
 * Represents an academic department with:
 * - One HOD (Head of Department)
 * - Many teachers
 * - Department-level resources and announcements
 *
 * This enables:
 * - Centralized department management
 * - Bulk task assignment to all teachers
 * - Department-wide announcements
 * - Resource allocation and tracking
 */
@Entity(
    tableName = "departments",
    indices = [
        Index(value = ["departmentCode"], unique = true),
        Index(value = ["hodId"]),
        Index(value = ["isActive"])
    ]
)
data class Department(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Basic Information
    val departmentCode: String,              // e.g., "CS", "MATH", "PHY"
    val departmentName: String,              // e.g., "Computer Science"
    val description: String = "",            // Department description

    // HOD Information
    val hodId: String,                       // User ID of the HOD
    val hodName: String = "",                // Cached HOD name for quick display
    val hodEmail: String = "",               // Cached HOD email

    // Department Details
    val building: String = "",               // Building name/number
    val floor: String = "",                  // Floor number
    val officeRoom: String = "",             // Main office room number
    val phoneExtension: String = "",         // Phone extension
    val email: String = "",                  // Department email

    // Statistics (cached for performance)
    val totalTeachers: Int = 0,              // Number of teachers in department
    val totalClasses: Int = 0,               // Total active classes
    val totalStudents: Int = 0,              // Total students (future)

    // Status
    val isActive: Boolean = true,            // Active/Inactive department

    // Timestamps
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastSyncTimestamp: Long = 0
) {
    /**
     * No-arg constructor for Room and Firestore
     */
    constructor() : this(
        id = 0,
        departmentCode = "",
        departmentName = "",
        description = "",
        hodId = "",
        hodName = "",
        hodEmail = "",
        building = "",
        floor = "",
        officeRoom = "",
        phoneExtension = "",
        email = "",
        totalTeachers = 0,
        totalClasses = 0,
        totalStudents = 0,
        isActive = true,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        lastSyncTimestamp = 0
    )

    /**
     * Get formatted department display name
     */
    fun getDisplayName(): String = "$departmentCode - $departmentName"

    /**
     * Get full address
     */
    fun getFullAddress(): String = buildString {
        if (building.isNotEmpty()) append("Building: $building")
        if (floor.isNotEmpty()) {
            if (isNotEmpty()) append(", ")
            append("Floor: $floor")
        }
        if (officeRoom.isNotEmpty()) {
            if (isNotEmpty()) append(", ")
            append("Room: $officeRoom")
        }
    }

    /**
     * Get contact info
     */
    fun getContactInfo(): String = buildString {
        if (email.isNotEmpty()) append("Email: $email")
        if (phoneExtension.isNotEmpty()) {
            if (isNotEmpty()) append("\n")
            append("Ext: $phoneExtension")
        }
    }

    /**
     * Check if user is HOD of this department
     */
    fun isHOD(userId: String): Boolean = hodId == userId
}

