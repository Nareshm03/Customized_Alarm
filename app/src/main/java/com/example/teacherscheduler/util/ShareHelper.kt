package com.example.teacherscheduler.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ShareHelper {
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    
    fun shareScheduleAsText(context: Context, classes: List<Class>, meetings: List<Meeting>) {
        val text = buildScheduleText(classes, meetings)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "My Schedule")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        
        context.startActivity(Intent.createChooser(intent, "Share Schedule"))
    }
    
    fun shareBackupFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(intent, "Share Backup"))
    }
    
    private fun buildScheduleText(classes: List<Class>, meetings: List<Meeting>): String {
        return buildString {
            appendLine("📚 My Schedule")
            appendLine()
            
            if (classes.isNotEmpty()) {
                appendLine("CLASSES:")
                classes.forEach { c ->
                    appendLine("• ${c.subject}")
                    appendLine("  ${dateFormat.format(c.startDate)} at ${timeFormat.format(c.startTime)}")
                    appendLine("  Room: ${c.roomNumber}")
                    appendLine()
                }
            }
            
            if (meetings.isNotEmpty()) {
                appendLine("MEETINGS:")
                meetings.forEach { m ->
                    appendLine("• ${m.title}")
                    appendLine("  ${dateFormat.format(m.startDate)} at ${timeFormat.format(m.startTime)}")
                    appendLine("  With: ${m.withWhom}")
                    appendLine()
                }
            }
        }
    }
}
