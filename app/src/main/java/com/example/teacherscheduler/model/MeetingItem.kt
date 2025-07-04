package com.example.teacherscheduler.model

import java.util.Date

/**
 * Model class for meeting items in Firestore
 */
data class MeetingItem(
    val id: Long = 0,
    val title: String = "",
    val with: String = "",
    val location: String = "",
    val date: Date = Date(),
    val startTime: Date = Date(),
    val endTime: Date = Date(),
    val notificationsEnabled: Boolean = true,
    val reminderMinutes: Int = 15,
    val notes: String = "",
    val semesterId: Long = 0
) {
    // No-arg constructor for Firestore
    constructor() : this(
        0, "", "", "", Date(), Date(), Date(),
        true, 15, "", 0
    )
}