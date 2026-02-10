package com.example.teacherscheduler.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.teacherscheduler.R
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.util.CsvExporter
import com.example.teacherscheduler.util.GoogleCalendarSync
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var repository: Repository
    
    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showSyncDialog()
        } else {
            Toast.makeText(this, "Calendar permission required", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_new)
        
        repository = Repository(this)
        
        setupToolbar()
        setupButtons()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Settings"
    }
    
    private fun setupButtons() {
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnExportCsv).setOnClickListener {
            exportToCsv()
        }
        
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnSyncCalendar).setOnClickListener {
            requestCalendarPermission()
        }
    }
    
    private fun exportToCsv() {
        lifecycleScope.launch {
            try {
                val classes = repository.getAllActiveClassesSync()
                val meetings = repository.getAllActiveMeetingsSync()
                
                val classFile = CsvExporter.exportClasses(this@SettingsActivity, classes)
                val meetingFile = CsvExporter.exportMeetings(this@SettingsActivity, meetings)
                
                val uri = FileProvider.getUriForFile(
                    this@SettingsActivity,
                    "${packageName}.fileprovider",
                    classFile
                )
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "Export CSV"))
                
                Toast.makeText(this@SettingsActivity, "CSV exported successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun requestCalendarPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == 
                PackageManager.PERMISSION_GRANTED -> {
                showSyncDialog()
            }
            else -> {
                calendarPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR)
            }
        }
    }
    
    private fun showSyncDialog() {
        lifecycleScope.launch {
            val classes = repository.getAllActiveClassesSync()
            val meetings = repository.getAllActiveMeetingsSync()
            
            AlertDialog.Builder(this@SettingsActivity)
                .setTitle("Sync to Google Calendar")
                .setMessage("Sync ${classes.size} classes and ${meetings.size} meetings to your Google Calendar?")
                .setPositiveButton("Sync") { _, _ ->
                    syncToCalendar(classes, meetings)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    
    private fun syncToCalendar(
        classes: List<com.example.teacherscheduler.model.Class>,
        meetings: List<com.example.teacherscheduler.model.Meeting>
    ) {
        lifecycleScope.launch {
            try {
                var synced = 0
                classes.forEach { classItem ->
                    GoogleCalendarSync.syncClassToCalendar(this@SettingsActivity, classItem)
                    synced++
                }
                meetings.forEach { meeting ->
                    GoogleCalendarSync.syncMeetingToCalendar(this@SettingsActivity, meeting)
                    synced++
                }
                Toast.makeText(this@SettingsActivity, "Synced $synced items", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@SettingsActivity, "Sync failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
