package com.example.teacherscheduler.model

enum class NotificationType {
    CLASS,
    MEETING
}

data class ScheduleNotification(
    val id: Long,
    val title: String,
    val message: String,
    val triggerTime: Long,
    val type: NotificationType,
    val referenceId: Long, // ID of the class or meeting
    val isEarlyReminder: Boolean = false
)