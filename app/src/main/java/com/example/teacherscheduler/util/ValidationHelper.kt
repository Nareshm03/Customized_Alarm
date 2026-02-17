package com.example.teacherscheduler.util

import com.google.android.material.textfield.TextInputLayout
import java.util.*

object ValidationHelper {
    
    fun validateNotEmpty(layout: TextInputLayout, fieldName: String): Boolean {
        val text = layout.editText?.text?.toString()?.trim() ?: ""
        return if (text.isEmpty()) {
            layout.error = "$fieldName cannot be empty"
            false
        } else {
            layout.error = null
            true
        }
    }
    
    fun validateTimeOrder(startTime: Date, endTime: Date, startLayout: TextInputLayout, endLayout: TextInputLayout): Boolean {
        return if (startTime >= endTime) {
            endLayout.error = "End time must be after start time"
            false
        } else {
            endLayout.error = null
            true
        }
    }
    
    fun validateNotPastDate(date: Date, layout: TextInputLayout): Boolean {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        return if (date.before(today)) {
            layout.error = "Date cannot be in the past"
            false
        } else {
            layout.error = null
            true
        }
    }
    
    fun validateHasDueDate(date: Date?, layout: TextInputLayout): Boolean {
        return if (date == null) {
            layout.error = "Due date is required"
            false
        } else {
            layout.error = null
            true
        }
    }
}
