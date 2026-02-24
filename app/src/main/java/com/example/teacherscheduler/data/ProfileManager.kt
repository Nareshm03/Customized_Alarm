package com.example.teacherscheduler.data

import android.content.Context
import android.content.SharedPreferences
import com.example.teacherscheduler.model.UserProfile
import com.google.gson.Gson

class ProfileManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveUserProfile(profile: UserProfile) {
        val json = gson.toJson(profile)
        sharedPreferences.edit().putString("user_profile", json).apply()
    }

    fun getUserProfile(): UserProfile {
        val json = sharedPreferences.getString("user_profile", null)
        return if (json != null) {
            gson.fromJson(json, UserProfile::class.java)
        } else {
            // Return a default profile if none is saved
            UserProfile(
                id = "",
                email = "",
                name = "",
                phone = "",
                designation = "",
                teacherId = "",
                gender = "",
                department = "",
                officeLocation = "",
                profilePictureUrl = "",
                role = "TEACHER",
                departmentId = "",
                primaryDepartmentId = 0
            )
        }
    }
}
