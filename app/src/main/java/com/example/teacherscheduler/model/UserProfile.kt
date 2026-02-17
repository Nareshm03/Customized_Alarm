package com.example.teacherscheduler.model

/**
 * Model class for user profile information
 * Supports role-based access control with HOD and TEACHER roles
 */
data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val teacherId: String = "",
    val gender: String = "",
    val designation: String = "",
    val department: String = "",                 // Department name (legacy field)
    val officeLocation: String = "",
    val profilePictureUrl: String = "",
    val role: String = UserRole.TEACHER.name,    // Role as String for Firestore compatibility (HOD or TEACHER)
    val departmentId: String = "",               // Department ID as String (for Firestore and external integrations)
    val primaryDepartmentId: Long = 0            // Reference to Department entity (for Room database)
) {
    // No-arg constructor for Firestore
    constructor() : this("", "", "", "", "", "", "", "", "", "", UserRole.TEACHER.name, "", 0)

    /**
     * Get the UserRole enum from the role string
     */
    fun getUserRole(): UserRole = UserRole.fromString(role)

    /**
     * Check if user is HOD
     */
    fun isHOD(): Boolean = getUserRole().isHOD()

    /**
     * Check if user is Teacher
     */
    fun isTeacher(): Boolean = getUserRole().isTeacher()

    /**
     * Get role display name
     */
    fun getRoleDisplayName(): String = getUserRole().displayName

    /**
     * Check if user has a primary department assigned
     */
    fun hasDepartment(): Boolean = primaryDepartmentId > 0 || departmentId.isNotEmpty()

    /**
     * Get department identifier
     * Prefers departmentId (String) over primaryDepartmentId (Long)
     */
    fun getDepartmentIdentifier(): String {
        return if (departmentId.isNotEmpty()) {
            departmentId
        } else if (primaryDepartmentId > 0) {
            primaryDepartmentId.toString()
        } else {
            ""
        }
    }
}