package com.example.teacherscheduler.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.teacherscheduler.R
import com.example.teacherscheduler.databinding.ItemClassBinding
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.util.CompletionStatusHelper
import com.example.teacherscheduler.util.HapticFeedbackHelper
import com.example.teacherscheduler.util.scalePress
import com.example.teacherscheduler.util.slideUpFadeIn

class ClassAdapter(
    private val onEditClick: (Class) -> Unit,
    private val onDeleteClick: (Class) -> Unit
) : ListAdapter<Class, ClassAdapter.ViewHolder>(ClassDiffCallback()) {
    
    private var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClassBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.slideUpFadeIn(position * 50L)
    }

    inner class ViewHolder(private val binding: ItemClassBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(classItem: Class) {
            binding.apply {
                val completionHelper = CompletionStatusHelper(binding.root.context)
                val isAttended = completionHelper.isClassAttendedToday(classItem.id)
                
                textSubject.text = classItem.subject
                textDepartment.text = classItem.department
                textRoom.text = "Room: ${classItem.roomNumber}"
                textTime.text = classItem.getFormattedTime()
                textDate.text = classItem.getFormattedDate()
                
                // Properly check if class is happening today
                val isHappeningToday = isClassHappeningToday(classItem)
                chipStatus.visibility = if (isHappeningToday) View.VISIBLE else View.GONE

                // Show completion status
                if (isAttended) {
                    textSubject.text = "✅ ${classItem.subject} (Attended)"
                    root.alpha = 0.7f
                    root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.completed_background))
                } else {
                    root.alpha = 1.0f
                    root.setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.transparent))
                }

                buttonEdit.setOnClickListener {
                    it.scalePress()
                    HapticFeedbackHelper.lightTap(it)
                    onEditClick(classItem)
                }
                buttonDelete.setOnClickListener {
                    it.scalePress()
                    HapticFeedbackHelper.heavyImpact(it)
                    onDeleteClick(classItem)
                }
            }
        }

        private fun isClassHappeningToday(classItem: Class): Boolean {
            val now = java.util.Calendar.getInstance()
            val todayDayOfWeek = now.get(java.util.Calendar.DAY_OF_WEEK)

            return if (classItem.isRecurring) {
                // For recurring classes, check if today is one of the scheduled days
                classItem.daysOfWeek.contains(todayDayOfWeek)
            } else {
                // For non-recurring classes, check if the date is today
                val classDate = java.util.Calendar.getInstance()
                classDate.time = classItem.startDate

                now.get(java.util.Calendar.YEAR) == classDate.get(java.util.Calendar.YEAR) &&
                now.get(java.util.Calendar.DAY_OF_YEAR) == classDate.get(java.util.Calendar.DAY_OF_YEAR)
            }
        }
    }

    private class ClassDiffCallback : DiffUtil.ItemCallback<Class>() {
        override fun areItemsTheSame(oldItem: Class, newItem: Class): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Class, newItem: Class): Boolean {
            return oldItem == newItem
        }
    }
}