package com.example.teacherscheduler.model

/**
 * Enum representing user roles in the application
 *
 * HOD (Head of Department) - Has full access including viewing all teachers' data
 * TEACHER - Has access to their own data only
 */
enum class UserRole(val displayName: String, val description: String) {
    /**
     * Head of Department role
     * Permissions:
     * - View all teachers' schedules in the department
     * - View department analytics
     * - Manage department resources
     * - Export department reports
     * - Access to HOD dashboard
     */
    HOD(
        displayName = "Head of Department",
        description = "Full access to department data and analytics"
    ),

    /**
     * Teacher role (Default)
     * Permissions:
     * - Manage own classes, meetings, and to-dos
     * - View own analytics
     * - Export own data
     * - Standard app features
     */
    TEACHER(
        displayName = "Teacher",
        description = "Access to personal schedule and data"
    );

    companion object {
        /**
         * Get UserRole from string value
         */
        fun fromString(value: String?): UserRole {
            return when (value?.uppercase()) {
                "HOD" -> HOD
                "TEACHER" -> TEACHER
                else -> TEACHER // Default to TEACHER
            }
        }

        /**
         * Get default role for new users
         */
        fun getDefault(): UserRole = TEACHER
    }

    /**
     * Check if this role has HOD privileges
     */
    fun isHOD(): Boolean = this == HOD

    /**
     * Check if this role is a regular teacher
     */
    fun isTeacher(): Boolean = this == TEACHER

    /**
     * Get icon resource name for this role
     */
    fun getIconName(): String = when (this) {
        HOD -> "ic_admin"
        TEACHER -> "ic_person"
    }
}

