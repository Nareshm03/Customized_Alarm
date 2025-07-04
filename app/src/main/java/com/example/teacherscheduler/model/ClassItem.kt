package com.example.teacherscheduler.model

import java.util.Date

/**
 * Model class for class items in Firestore
 */
data class ClassItem(
    val id: Long = 0,
    val subject: String = "",
    val department: String = "",
    val roomNumber: String = "",
    val startDate: Date = Date(),
    val endDate: Date = Date(),
    val startTime: Date = Date(),
    val endTime: Date = Date(),
    val daysOfWeek: List<Int> = listOf(),
    val isRecurring: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val reminderMinutes: Int = 15,
    val description: String = "",
    val semesterId: Long = 0
) {
    // No-arg constructor for Firestore
    constructor() : this(
        0, "", "", "", Date(), Date(), Date(), Date(),
        listOf(), false, true, 15, "", 0
    )
}