package com.example.teacherscheduler.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.teacherscheduler.R
import com.example.teacherscheduler.databinding.ItemClassBinding
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.util.CompletionStatusHelper

class ClassAdapter(
    private val onEditClick: (Class) -> Unit,
    private val onDeleteClick: (Class) -> Unit
) : ListAdapter<Class, ClassAdapter.ViewHolder>(ClassDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClassBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
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
                
                // Show completion status
                if (isAttended) {
                    textSubject.text = "✅ ${classItem.subject} (Attended)"
                    root.alpha = 0.7f
                    root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.completed_background))
                } else {
                    root.alpha = 1.0f
                    root.setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.transparent))
                }

                buttonEdit.setOnClickListener { onEditClick(classItem) }
                buttonDelete.setOnClickListener { onDeleteClick(classItem) }
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