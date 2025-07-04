package com.example.teacherscheduler.data.local

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.teacherscheduler.R
import com.example.teacherscheduler.model.Meeting
import com.example.teacherscheduler.util.formatTimeRange
import com.example.teacherscheduler.util.formatTime
import java.text.SimpleDateFormat
import java.util.Locale

class MeetingAdapter(private val listener: OnMeetingClickListener) : 
    ListAdapter<Meeting, MeetingAdapter.MeetingViewHolder>(MeetingDiffCallback()) {

    interface OnMeetingClickListener {
        fun onMeetingClick(meeting: Meeting)
    }
    
    private val dateFormat = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeetingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meeting, parent, false)
        return MeetingViewHolder(view)
    }

    override fun onBindViewHolder(holder: MeetingViewHolder, position: Int) {
        val meeting = getItem(position)
        holder.bind(meeting)
    }

    inner class MeetingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        private val textViewWithWhom: TextView = itemView.findViewById(R.id.textViewWithWhom)
        private val textViewLocation: TextView = itemView.findViewById(R.id.textViewLocation)
        private val textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        private val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onMeetingClick(getItem(position))
                }
            }
        }

        fun bind(meeting: Meeting) {
            textViewTitle.text = meeting.title
            textViewWithWhom.text = meeting.withWhom
            textViewLocation.text = meeting.location
            textViewDate.text = meeting.getFormattedDate()
            textViewTime.text = meeting.getFormattedTime()
        }
    }

    class MeetingDiffCallback : DiffUtil.ItemCallback<Meeting>() {
        override fun areItemsTheSame(oldItem: Meeting, newItem: Meeting): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Meeting, newItem: Meeting): Boolean {
            return oldItem == newItem
        }
    }
}