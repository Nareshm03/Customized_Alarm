package com.example.teacherscheduler.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.teacherscheduler.databinding.ActivityBackupBinding
import com.example.teacherscheduler.util.BackupRestoreHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.File

class BackupActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityBackupBinding
    private lateinit var backupHelper: BackupRestoreHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        backupHelper = BackupRestoreHelper(this)
        
        setupToolbar()
        setupButtons()
        loadBackups()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Backup & Restore"
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
    
    private fun setupButtons() {
        binding.buttonCreateBackup.setOnClickListener { createBackup() }
    }
    
    private fun loadBackups() {
        val backups = backupHelper.getBackupFiles()
        binding.emptyView.visibility = if (backups.isEmpty()) View.VISIBLE else View.GONE
    }
    
    private fun createBackup() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val result = backupHelper.createBackup()
            binding.progressBar.visibility = View.GONE
            
            result.onSuccess {
                Toast.makeText(this@BackupActivity, "Backup created: ${it.name}", Toast.LENGTH_LONG).show()
                loadBackups()
            }.onFailure {
                Toast.makeText(this@BackupActivity, "Backup failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
