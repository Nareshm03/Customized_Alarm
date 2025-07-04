package com.example.teacherscheduler.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.teacherscheduler.databinding.DialogModernTimePickerBinding
import java.text.SimpleDateFormat
import java.util.*

class ModernTimePickerDialog : DialogFragment() {
    
    private var _binding: DialogModernTimePickerBinding? = null
    private val binding get() = _binding!!
    
    private var selectedTime = Calendar.getInstance()
    private var onTimeSelectedListener: ((Calendar) -> Unit)? = null
    private var dialogTitle = "Select Time"
    
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    companion object {
        fun newInstance(
            initialTime: Calendar = Calendar.getInstance(),
            title: String = "Select Time",
            onTimeSelected: (Calendar) -> Unit
        ): ModernTimePickerDialog {
            return ModernTimePickerDialog().apply {
                this.selectedTime = initialTime.clone() as Calendar
                this.dialogTitle = title
                this.onTimeSelectedListener = onTimeSelected
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogModernTimePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupTimePicker()
        setupClickListeners()
        setupQuickTimeButtons()
        updateTimeDisplay()
        
        binding.textTimePickerTitle.text = dialogTitle
    }

    private fun setupTimePicker() {
        binding.timePicker.setIs24HourView(false)
        binding.timePicker.hour = selectedTime.get(Calendar.HOUR_OF_DAY)
        binding.timePicker.minute = selectedTime.get(Calendar.MINUTE)
        
        binding.timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
            selectedTime.set(Calendar.MINUTE, minute)
            updateTimeDisplay()
        }
    }

    private fun setupClickListeners() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
        
        binding.buttonOk.setOnClickListener {
            onTimeSelectedListener?.invoke(selectedTime)
            dismiss()
        }
    }

    private fun setupQuickTimeButtons() {
        binding.chip9am.setOnClickListener {
            setTime(9, 0)
        }
        
        binding.chip12pm.setOnClickListener {
            setTime(12, 0)
        }
        
        binding.chip2pm.setOnClickListener {
            setTime(14, 0)
        }
        
        binding.chip5pm.setOnClickListener {
            setTime(17, 0)
        }
    }

    private fun setTime(hour: Int, minute: Int) {
        selectedTime.set(Calendar.HOUR_OF_DAY, hour)
        selectedTime.set(Calendar.MINUTE, minute)
        
        binding.timePicker.hour = hour
        binding.timePicker.minute = minute
        
        updateTimeDisplay()
    }

    private fun updateTimeDisplay() {
        val timeText = timeFormat.format(selectedTime.time)
        binding.textTimeDisplay.text = timeText
        binding.textSelectedTime.text = timeText
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}