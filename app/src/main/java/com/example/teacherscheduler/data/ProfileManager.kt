package com.example.teacherscheduler.data

import android.content.Context
import android.content.SharedPreferences
import com.example.teacherscheduler.model.UserProfile
import com.google.gson.Gson

class ProfileManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        private const val PREFS_NAME = "teacher_profile_prefs"
        private const val KEY_USER_PROFILE = "user_profile"
    }

    fun saveUserProfile(profile: UserProfile) {
        val profileJson = gson.toJson(profile)
        sharedPreferences.edit().putString(KEY_USER_PROFILE, profileJson).apply()
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
                officeLocation = "Room 101"
            )
        }
    }
}