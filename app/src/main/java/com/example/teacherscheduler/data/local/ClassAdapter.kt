package com.example.teacherscheduler.data.local

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.teacherscheduler.R
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.util.formatTimeRange
import com.example.teacherscheduler.util.formatTime
import java.util.Calendar

class ClassAdapter(private val listener: OnClassClickListener) : 
    ListAdapter<Class, ClassAdapter.ClassViewHolder>(ClassDiffCallback()) {

    interface OnClassClickListener {
        fun onClassClick(classItem: Class)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_class, parent, false)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        val classItem = getItem(position)
        holder.bind(classItem)
    }

    inner class ClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewSubject: TextView = itemView.findViewById(R.id.textViewSubject)
        private val textViewDepartment: TextView = itemView.findViewById(R.id.textViewDepartment)
        private val textViewRoom: TextView = itemView.findViewById(R.id.textViewRoom)
        private val textViewTime: TextView = itemView.findViewById(R.id.textViewTime)
        private val textViewDays: TextView = itemView.findViewById(R.id.textViewDays)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onClassClick(getItem(position))
                }
            }
        }

        fun bind(classItem: Class) {
            textViewSubject.text = classItem.subject
            textViewDepartment.text = classItem.department
            textViewRoom.text = classItem.roomNumber
            textViewTime.text = classItem.getFormattedTime()
            
            // Format days of week
            val daysOfWeek = classItem.daysOfWeek.map { dayNumber ->
                when (dayNumber) {
                    Calendar.MONDAY -> "Mon"
                    Calendar.TUESDAY -> "Tue"
                    Calendar.WEDNESDAY -> "Wed"
                    Calendar.THURSDAY -> "Thu"
                    Calendar.FRIDAY -> "Fri"
                    Calendar.SATURDAY -> "Sat"
                    Calendar.SUNDAY -> "Sun"
                    else -> ""
                }
            }.filter { it.isNotEmpty() }
            
            textViewDays.text = daysOfWeek.joinToString(", ")
        }
    }

    class ClassDiffCallback : DiffUtil.ItemCallback<Class>() {
        override fun areItemsTheSame(oldItem: Class, newItem: Class): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Class, newItem: Class): Boolean {
            return oldItem == newItem
        }
    }
}