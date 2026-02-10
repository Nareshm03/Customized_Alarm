package com.example.teacherscheduler.util

object InputValidator {
    
    fun validateClassInput(subject: String, department: String, room: String): ValidationResult {
        if (subject.isBlank()) return ValidationResult.Error("Subject is required")
        if (subject.length < 2) return ValidationResult.Error("Subject too short")
        if (department.isBlank()) return ValidationResult.Error("Department is required")
        if (room.isBlank()) return ValidationResult.Error("Room number is required")
        return ValidationResult.Success
    }
    
    fun validateMeetingInput(title: String, withWhom: String): ValidationResult {
        if (title.isBlank()) return ValidationResult.Error("Title is required")
        if (title.length < 3) return ValidationResult.Error("Title too short")
        if (withWhom.isBlank()) return ValidationResult.Error("Participant is required")
        return ValidationResult.Success
    }
    
    fun validateTimeRange(startTime: Long, endTime: Long): ValidationResult {
        if (endTime <= startTime) return ValidationResult.Error("End time must be after start time")
        val duration = (endTime - startTime) / (1000 * 60)
        if (duration > 480) return ValidationResult.Error("Duration cannot exceed 8 hours")
        return ValidationResult.Success
    }
    
    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}
