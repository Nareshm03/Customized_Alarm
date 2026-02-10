package com.example.teacherscheduler.util

import android.content.Context
import android.util.Log
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.model.Meeting
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class BackupRestoreHelper(private val context: Context) {
    
    private val repository = Repository(context)
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    data class BackupData(
        val classes: List<Class>,
        val meetings: List<Meeting>,
        val backupDate: Long,
        val version: Int = 1
    )
    
    suspend fun createBackup(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val classes = repository.getAllClassesDirect()
            val meetings = repository.getAllMeetingsDirect()
            
            val backupData = BackupData(
                classes = classes,
                meetings = meetings,
                backupDate = System.currentTimeMillis()
            )
            
            val json = gson.toJson(backupData)
            val fileName = "teacher_scheduler_backup_${dateFormat.format(Date())}.json"
            val backupDir = File(context.getExternalFilesDir(null), "backups")
            
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val backupFile = File(backupDir, fileName)
            backupFile.writeText(json)
            
            Log.d("BackupRestore", "Backup created: ${backupFile.absolutePath}")
            Result.success(backupFile)
        } catch (e: Exception) {
            Log.e("BackupRestore", "Backup failed", e)
            Result.failure(e)
        }
    }
    
    suspend fun restoreBackup(backupFile: File): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val json = backupFile.readText()
            val backupData = gson.fromJson(json, BackupData::class.java)
            
            var restoredCount = 0
            
            backupData.classes.forEach { classItem ->
                repository.insertClass(classItem)
                restoredCount++
            }
            
            backupData.meetings.forEach { meeting ->
                repository.insertMeeting(meeting)
                restoredCount++
            }
            
            Log.d("BackupRestore", "Restored $restoredCount items")
            Result.success(restoredCount)
        } catch (e: Exception) {
            Log.e("BackupRestore", "Restore failed", e)
            Result.failure(e)
        }
    }
    
    fun getBackupFiles(): List<File> {
        val backupDir = File(context.getExternalFilesDir(null), "backups")
        return if (backupDir.exists()) {
            backupDir.listFiles()?.filter { it.extension == "json" }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList()
        }
    }
    
    fun deleteBackup(file: File): Boolean {
        return try {
            file.delete()
        } catch (e: Exception) {
            Log.e("BackupRestore", "Delete failed", e)
            false
        }
    }
}
