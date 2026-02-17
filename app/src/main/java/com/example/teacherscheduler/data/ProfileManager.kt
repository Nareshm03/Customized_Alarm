package com.example.teacherscheduler.data

import android.content.Context
import android.content.SharedPreferences
import com.example.teacherscheduler.model.UserProfile
import com.example.teacherscheduler.model.UserRole
import com.google.gson.Gson

class ProfileManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "teacher_profile_prefs"
        private const val KEY_USER_PROFILE = "user_profile"
        private const val KEY_USER_ROLE = "user_role"
    }

    fun saveUserProfile(profile: UserProfile) {
        val profileJson = gson.toJson(profile)
        sharedPreferences.edit().putString(KEY_USER_PROFILE, profileJson).apply()
        // Also save role separately for quick access
        sharedPreferences.edit().putString(KEY_USER_ROLE, profile.role).apply()
    }

    fun getUserProfile(): UserProfile {
        val profileJson = sharedPreferences.getString(KEY_USER_PROFILE, null)
        return if (profileJson != null) {
            try {
                gson.fromJson(profileJson, UserProfile::class.java)
            } catch (e: Exception) {
                UserProfile()
            }
        } else {
            // Return default profile if none exists
            UserProfile(
                name = "Teacher Name",
                email = "teacher@example.com",
                phone = "",
                teacherId = "T12345",
                gender = "Prefer not to say",
                designation = "Assistant Professor",
                department = "Computer Science",
                officeLocation = "Room 101",
                role = UserRole.TEACHER.name,
                departmentId = "", // Default empty department ID
                primaryDepartmentId = 0 // Default no department reference
            )
        }
    }

    /**
     * Update user's role
     */
    fun updateUserRole(role: UserRole) {
        val profile = getUserProfile()
        val updatedProfile = profile.copy(role = role.name)
        saveUserProfile(updatedProfile)
    }

    /**
     * Update user's department ID
     */
    fun updateDepartmentId(departmentId: String) {
        val profile = getUserProfile()
        val updatedProfile = profile.copy(departmentId = departmentId)
        saveUserProfile(updatedProfile)
    }

    /**
     * Update user's primary department ID (Room database reference)
     */
    fun updatePrimaryDepartmentId(primaryDepartmentId: Long) {
        val profile = getUserProfile()
        val updatedProfile = profile.copy(primaryDepartmentId = primaryDepartmentId)
        saveUserProfile(updatedProfile)
    }

    /**
     * Update both department identifiers
     */
    fun updateDepartment(departmentId: String, primaryDepartmentId: Long) {
        val profile = getUserProfile()
        val updatedProfile = profile.copy(
            departmentId = departmentId,
            primaryDepartmentId = primaryDepartmentId
        )
        saveUserProfile(updatedProfile)
    }

    /**
     * Get current user's role
     */
    fun getUserRole(): UserRole {
        val roleString = sharedPreferences.getString(KEY_USER_ROLE, UserRole.TEACHER.name)
        return UserRole.fromString(roleString)
    }

    /**
     * Check if current user is HOD
     */
    fun isUserHOD(): Boolean {
        return getUserRole() == UserRole.HOD
    }

    /**
     * Check if current user is Teacher
     */
    fun isUserTeacher(): Boolean {
        return getUserRole() == UserRole.TEACHER
    }

    /**
     * Clear all profile data (for logout)
     */
    fun clearProfile() {
        sharedPreferences.edit().clear().apply()
    }
}