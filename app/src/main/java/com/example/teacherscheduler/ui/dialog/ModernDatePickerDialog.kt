package com.example.teacherscheduler.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.example.teacherscheduler.databinding.DialogModernDatePickerBinding
import java.text.SimpleDateFormat
import java.util.*

class ModernDatePickerDialog : DialogFragment() {
    
    private var _binding: DialogModernDatePickerBinding? = null
    private val binding get() = _binding!!
    
    private var selectedDate = Calendar.getInstance()
    private var onDateSelectedListener: ((Calendar) -> Unit)? = null
    
    private val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())

    companion object {
        fun newInstance(
            initialDate: Calendar = Calendar.getInstance(),
            onDateSelected: (Calendar) -> Unit
        ): ModernDatePickerDialog {
            return ModernDatePickerDialog().apply {
                this.selectedDate = initialDate.clone() as Calendar
                this.onDateSelectedListener = onDateSelected
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
        _binding = DialogModernDatePickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupDatePicker()
        setupClickListeners()
        updateSelectedDateText()
    }

    private fun setupDatePicker() {
        binding.datePicker.init(
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        ) { _, year, month, dayOfMonth ->
            selectedDate.set(year, month, dayOfMonth)
            updateSelectedDateText()
        }
        
        // Set minimum date to today
        binding.datePicker.minDate = System.currentTimeMillis()
    }

    private fun setupClickListeners() {
        binding.buttonCancel.setOnClickListener {
            dismiss()
        }
        
        binding.buttonOk.setOnClickListener {
            onDateSelectedListener?.invoke(selectedDate)
            dismiss()
        }
    }

    private fun updateSelectedDateText() {
        binding.textSelectedDate.text = dateFormat.format(selectedDate.time)
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