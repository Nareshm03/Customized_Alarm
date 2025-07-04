package com.example.teacherscheduler.model

/**
 * Model class for user profile information
 */
data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val teacherId: String = "",
    val gender: String = "",
    val designation: String = "",
    val department: String = "",
    val officeLocation: String = "",
    val profilePictureUrl: String = ""
) {
    // No-arg constructor for Firestore
    constructor() : this("", "", "", "", "", "", "", "", "", "")
}