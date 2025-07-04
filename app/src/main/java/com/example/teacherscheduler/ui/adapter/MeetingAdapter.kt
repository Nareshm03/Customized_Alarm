package com.example.teacherscheduler.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.teacherscheduler.R
import com.example.teacherscheduler.databinding.ItemMeetingBinding
import com.example.teacherscheduler.model.Meeting
import com.example.teacherscheduler.util.CompletionStatusHelper

class MeetingAdapter(
    private val onEditClick: (Meeting) -> Unit,
    private val onDeleteClick: (Meeting) -> Unit
) : ListAdapter<Meeting, MeetingAdapter.ViewHolder>(MeetingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMeetingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemMeetingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(meeting: Meeting) {
            with(binding) {
                val completionHelper = CompletionStatusHelper(binding.root.context)
                val isCompleted = completionHelper.isMeetingCompleted(meeting.id)
                
                textTitle.text = meeting.title
                textWithWhom.text = meeting.withWhom
                textLocation.text = "Location: ${meeting.location}"
                textDateTime.text = meeting.getFormattedDateTime()
                
                // Show completion status
                if (isCompleted) {
                    textTitle.text = "✅ ${meeting.title} (Completed)"
                    root.alpha = 0.7f
                    root.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.completed_background))
                } else {
                    root.alpha = 1.0f
                    root.setBackgroundColor(ContextCompat.getColor(binding.root.context, android.R.color.transparent))
                }

                // Set click listener on the whole card for editing
                root.setOnClickListener { onEditClick(meeting) }
                buttonDelete.setOnClickListener { onDeleteClick(meeting) }
            }
        }
    }

    private class MeetingDiffCallback : DiffUtil.ItemCallback<Meeting>() {
        override fun areItemsTheSame(oldItem: Meeting, newItem: Meeting): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Meeting, newItem: Meeting): Boolean {
            return oldItem == newItem
        }
    }
}