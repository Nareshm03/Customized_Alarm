package com.example.teacherscheduler.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.teacherscheduler.R
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.databinding.ActivityAddEditTodoBinding
import com.example.teacherscheduler.model.ToDo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddEditToDoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditTodoBinding
    private lateinit var repository: Repository
    private var todoId: Long = 0
    private var existingToDo: ToDo? = null
    private var selectedDueDate: Calendar? = null

    private val categories = listOf(
        "Grading",
        "Lesson Planning",
        "Administrative",
        "Meetings",
        "Professional Development",
        "Student Support",
        "Other"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = Repository(this)
        todoId = intent.getLongExtra("TODO_ID", 0)

        setupToolbar()
        setupCategoryDropdown()
        setupDateTimePickers()
        setupButtons()

        if (todoId > 0) {
            loadToDo()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (todoId > 0) "Edit To-Do" else "Add To-Do"
    }

    private fun setupCategoryDropdown() {
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, categories)
        binding.editCategory.setAdapter(adapter)
    }

    private fun setupDateTimePickers() {
        binding.editDueDate.setOnClickListener {
            showDatePicker()
        }

        binding.editDueTime.setOnClickListener {
            if (selectedDueDate == null) {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Select Date First")
                    .setMessage("Please select a due date before setting the time.")
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                showTimePicker()
            }
        }

        binding.dueDateInputLayout.setEndIconOnClickListener {
            selectedDueDate = null
            binding.editDueDate.setText("")
            binding.editDueTime.setText("")
        }

        binding.dueTimeInputLayout.setEndIconOnClickListener {
            binding.editDueTime.setText("")
            selectedDueDate?.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
            }
        }
    }

    private fun showDatePicker() {
        val calendar = selectedDueDate ?: Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                if (selectedDueDate == null) {
                    selectedDueDate = Calendar.getInstance()
                }
                selectedDueDate?.apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = selectedDueDate ?: Calendar.getInstance()

        TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedDueDate?.apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                updateTimeDisplay()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    private fun updateDateDisplay() {
        selectedDueDate?.let {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.editDueDate.setText(dateFormat.format(it.time))
        }
    }

    private fun updateTimeDisplay() {
        selectedDueDate?.let {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            binding.editDueTime.setText(timeFormat.format(it.time))
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener {
            saveToDo()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun loadToDo() {
        lifecycleScope.launch {
            val todo = repository.getToDoById(todoId)
            if (todo != null) {
                existingToDo = todo
                populateFields(todo)
            }
        }
    }

    private fun populateFields(todo: ToDo) {
        binding.editTitle.setText(todo.title)
        binding.editDescription.setText(todo.description)
        binding.editCategory.setText(todo.category, false)

        when (todo.priority) {
            ToDo.Priority.LOW -> binding.chipLow.isChecked = true
            ToDo.Priority.MEDIUM -> binding.chipMedium.isChecked = true
            ToDo.Priority.HIGH -> binding.chipHigh.isChecked = true
            ToDo.Priority.URGENT -> binding.chipUrgent.isChecked = true
        }

        todo.dueDate?.let {
            selectedDueDate = Calendar.getInstance().apply { time = it }
            updateDateDisplay()
            updateTimeDisplay()
        }

        binding.switchNotifications.isChecked = todo.notificationsEnabled
    }

    private fun saveToDo() {
        val title = binding.editTitle.text.toString().trim()

        if (title.isEmpty()) {
            binding.titleInputLayout.error = "Title is required"
            return
        }

        val description = binding.editDescription.text.toString().trim()
        val category = binding.editCategory.text.toString().trim()

        val priority = when {
            binding.chipLow.isChecked -> ToDo.Priority.LOW
            binding.chipMedium.isChecked -> ToDo.Priority.MEDIUM
            binding.chipHigh.isChecked -> ToDo.Priority.HIGH
            binding.chipUrgent.isChecked -> ToDo.Priority.URGENT
            else -> ToDo.Priority.MEDIUM
        }

        val dueDate = selectedDueDate?.time
        val notificationsEnabled = binding.switchNotifications.isChecked

        lifecycleScope.launch {
            if (existingToDo != null) {
                // Update existing
                val updatedToDo = existingToDo!!.copy(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    dueDate = dueDate,
                    notificationsEnabled = notificationsEnabled
                )
                repository.updateToDo(updatedToDo)
            } else {
                // Create new
                val newToDo = ToDo(
                    title = title,
                    description = description,
                    category = category,
                    priority = priority,
                    dueDate = dueDate,
                    notificationsEnabled = notificationsEnabled
                )
                repository.insertToDo(newToDo)
            }
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

