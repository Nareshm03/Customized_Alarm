package com.example.teacherscheduler.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.teacherscheduler.R
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.databinding.ActivityAddEditClassModernBinding
import com.example.teacherscheduler.model.Class
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ModernAddEditClassActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditClassModernBinding
    private lateinit var repository: Repository
    private var editingClass: Class? = null
    private var selectedDate = Calendar.getInstance()
    private var startTime = Calendar.getInstance()
    private var endTime = Calendar.getInstance()
    private val selectedDays = mutableSetOf<String>()
    
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    companion object {
        const val EXTRA_CLASS_ID = "extra_class_id"

        fun newIntent(context: Context, classId: Long? = null): Intent {
            return Intent(context, ModernAddEditClassActivity::class.java).apply {
                classId?.let { putExtra(EXTRA_CLASS_ID, it) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditClassModernBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = Repository(this)
        
        setupToolbar()
        setupClickListeners()
        setupRecurringOptions()
        loadClassIfEditing()
        
        // Set default times
        startTime.set(Calendar.HOUR_OF_DAY, 9)
        startTime.set(Calendar.MINUTE, 0)
        endTime.set(Calendar.HOUR_OF_DAY, 10)
        endTime.set(Calendar.MINUTE, 0)
        
        updateTimeButtons()
        updateDateButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        val classId = intent.getLongExtra(EXTRA_CLASS_ID, -1L)
        val isEditing = classId != -1L
        
        binding.toolbar.title = if (isEditing) "Edit Class" else "Add Class"
        binding.headerTitle.text = if (isEditing) "Edit Class Details" else "Create New Class"
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.buttonDate.setOnClickListener { showDatePicker() }
        binding.buttonStartTime.setOnClickListener { showStartTimePicker() }
        binding.buttonEndTime.setOnClickListener { showEndTimePicker() }
        binding.buttonSave.setOnClickListener { saveClass() }
        binding.buttonCancel.setOnClickListener { finish() }
        binding.buttonDelete.setOnClickListener { showDeleteConfirmation() }
    }

    private fun setupRecurringOptions() {
        binding.switchRecurring.setOnCheckedChangeListener { _, isChecked ->
            binding.layoutDaysOfWeek.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
        
        // Setup day chips
        val dayChips = listOf(
            binding.chipMonday to "Monday",
            binding.chipTuesday to "Tuesday", 
            binding.chipWednesday to "Wednesday",
            binding.chipThursday to "Thursday",
            binding.chipFriday to "Friday",
            binding.chipSaturday to "Saturday",
            binding.chipSunday to "Sunday"
        )
        
        dayChips.forEach { (chip, day) ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedDays.add(day)
                } else {
                    selectedDays.remove(day)
                }
            }
        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            R.style.MaterialDatePickerTheme,
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                updateDateButton()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showStartTimePicker() {
        TimePickerDialog(
            this,
            R.style.MaterialTimePickerTheme,
            { _, hourOfDay, minute ->
                startTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                startTime.set(Calendar.MINUTE, minute)
                
                // Auto-adjust end time to be 1 hour later
                endTime.timeInMillis = startTime.timeInMillis + (60 * 60 * 1000)
                
                updateTimeButtons()
            },
            startTime.get(Calendar.HOUR_OF_DAY),
            startTime.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun showEndTimePicker() {
        TimePickerDialog(
            this,
            R.style.MaterialTimePickerTheme,
            { _, hourOfDay, minute ->
                endTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                endTime.set(Calendar.MINUTE, minute)
                updateTimeButtons()
            },
            endTime.get(Calendar.HOUR_OF_DAY),
            endTime.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun updateDateButton() {
        binding.buttonDate.text = dateFormat.format(selectedDate.time)
    }

    private fun updateTimeButtons() {
        binding.buttonStartTime.text = timeFormat.format(startTime.time)
        binding.buttonEndTime.text = timeFormat.format(endTime.time)
    }

    private fun loadClassIfEditing() {
        val classId = intent.getLongExtra(EXTRA_CLASS_ID, -1L)
        if (classId != -1L) {
            binding.buttonDelete.visibility = View.VISIBLE
            
            lifecycleScope.launch {
                try {
                    editingClass = repository.getClassById(classId)
                    editingClass?.let { populateFields(it) }
                } catch (e: Exception) {
                    Toast.makeText(this@ModernAddEditClassActivity, 
                        "Error loading class: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun populateFields(classItem: Class) {
        binding.editSubject.setText(classItem.subject)
        binding.editDepartment.setText(classItem.department)
        binding.editRoomNumber.setText(classItem.roomNumber)
        
        // Set date and time
        selectedDate.timeInMillis = classItem.getStartDateTime()
        startTime.timeInMillis = classItem.getStartDateTime()
        endTime.timeInMillis = classItem.getEndDateTime()
        
        updateDateButton()
        updateTimeButtons()
        
        // Set notifications
        binding.switchNotifications.isChecked = classItem.notificationsEnabled
        
        // Handle recurring classes
        if (classItem.isRecurring) {
            binding.switchRecurring.isChecked = true
            // Parse and set selected days if you have that data
        }
    }

    private fun saveClass() {
        if (!validateInput()) return
        
        val subject = binding.editSubject.text.toString().trim()
        val department = binding.editDepartment.text.toString().trim()
        val roomNumber = binding.editRoomNumber.text.toString().trim()
        
        // Combine date with times
        val startDateTime = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
            set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, startTime.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val endDateTime = Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
            set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
            set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, endTime.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, endTime.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val classItem = if (editingClass != null) {
            editingClass!!.copy(
                subject = subject,
                department = department,
                roomNumber = roomNumber,
                startDate = Date(selectedDate.timeInMillis),
                endDate = Date(selectedDate.timeInMillis),
                startTime = Date(startTime.timeInMillis),
                endTime = Date(endTime.timeInMillis),
                notificationsEnabled = binding.switchNotifications.isChecked,
                isRecurring = binding.switchRecurring.isChecked
            )
        } else {
            Class(
                subject = subject,
                department = department,
                roomNumber = roomNumber,
                startDate = Date(selectedDate.timeInMillis),
                endDate = Date(selectedDate.timeInMillis),
                startTime = Date(startTime.timeInMillis),
                endTime = Date(endTime.timeInMillis),
                notificationsEnabled = binding.switchNotifications.isChecked,
                isRecurring = binding.switchRecurring.isChecked
            )
        }

        lifecycleScope.launch {
            try {
                if (editingClass != null) {
                    repository.updateClass(classItem)
                    Toast.makeText(this@ModernAddEditClassActivity, 
                        "Class updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    repository.insertClass(classItem)
                    Toast.makeText(this@ModernAddEditClassActivity, 
                        "Class created successfully", Toast.LENGTH_SHORT).show()
                }
                
                // Handle recurring classes
                if (binding.switchRecurring.isChecked && selectedDays.isNotEmpty()) {
                    createRecurringClasses(classItem)
                }
                
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@ModernAddEditClassActivity, 
                    "Error saving class: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createRecurringClasses(baseClass: Class) {
        // Implementation for creating recurring classes
        // This would create multiple class instances based on selected days
        lifecycleScope.launch {
            try {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = baseClass.getStartDateTime()
                
                // Create classes for the next 12 weeks for each selected day
                for (week in 1..12) {
                    selectedDays.forEach { dayName ->
                        val dayOfWeek = getDayOfWeek(dayName)
                        val classDate = Calendar.getInstance().apply {
                            timeInMillis = baseClass.getStartDateTime()
                            add(Calendar.WEEK_OF_YEAR, week)
                            set(Calendar.DAY_OF_WEEK, dayOfWeek)
                        }
                        
                        val endDate = Calendar.getInstance().apply {
                            timeInMillis = baseClass.getEndDateTime()
                            add(Calendar.WEEK_OF_YEAR, week)
                            set(Calendar.DAY_OF_WEEK, dayOfWeek)
                        }
                        
                        val recurringClass = baseClass.copy(
                            id = 0, // New ID will be generated
                            startDate = Date(classDate.timeInMillis),
                            endDate = Date(endDate.timeInMillis),
                            startTime = baseClass.startTime,
                            endTime = baseClass.endTime
                        )
                        
                        repository.insertClass(recurringClass)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@ModernAddEditClassActivity, 
                    "Error creating recurring classes: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDayOfWeek(dayName: String): Int {
        return when (dayName) {
            "Sunday" -> Calendar.SUNDAY
            "Monday" -> Calendar.MONDAY
            "Tuesday" -> Calendar.TUESDAY
            "Wednesday" -> Calendar.WEDNESDAY
            "Thursday" -> Calendar.THURSDAY
            "Friday" -> Calendar.FRIDAY
            "Saturday" -> Calendar.SATURDAY
            else -> Calendar.MONDAY
        }
    }

    private fun validateInput(): Boolean {
        val subject = binding.editSubject.text.toString().trim()
        val department = binding.editDepartment.text.toString().trim()
        
        if (subject.isEmpty()) {
            binding.textInputLayoutSubject.error = "Subject is required"
            return false
        }
        
        if (department.isEmpty()) {
            binding.textInputLayoutDepartment.error = "Department is required"
            return false
        }
        
        if (endTime.timeInMillis <= startTime.timeInMillis) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
            return false
        }
        
        // Clear any previous errors
        binding.textInputLayoutSubject.error = null
        binding.textInputLayoutDepartment.error = null
        
        return true
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Class")
            .setMessage("Are you sure you want to delete this class? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteClass()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteClass() {
        editingClass?.let { classItem ->
            lifecycleScope.launch {
                try {
                    repository.deleteClass(classItem)
                    Toast.makeText(this@ModernAddEditClassActivity, 
                        "Class deleted successfully", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@ModernAddEditClassActivity, 
                        "Error deleting class: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}