package com.example.teacherscheduler.ui.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.teacherscheduler.R
import com.example.teacherscheduler.databinding.ItemTodoBinding
import com.example.teacherscheduler.model.ToDo
import com.example.teacherscheduler.util.scalePress
import com.example.teacherscheduler.util.slideUpFadeIn
import java.text.SimpleDateFormat
import java.util.Locale

class ToDoAdapter(
    private val onItemClick: (ToDo) -> Unit,
    private val onCheckClick: (ToDo, Boolean) -> Unit,
    private val onEditClick: (ToDo) -> Unit,
    private val onDeleteClick: (ToDo) -> Unit
) : ListAdapter<ToDo, ToDoAdapter.ViewHolder>(ToDoDiffCallback()) {

    private var lastPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTodoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
        holder.itemView.slideUpFadeIn(position * 50L)
    }

    inner class ViewHolder(private val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(todo: ToDo) {
            binding.apply {
                textTitle.text = todo.title

                // Category
                if (todo.category.isNotEmpty()) {
                    textCategory.visibility = View.VISIBLE
                    textCategory.text = todo.category
                } else {
                    textCategory.visibility = View.GONE
                }

                // Description
                if (todo.description.isNotEmpty()) {
                    textDescription.visibility = View.VISIBLE
                    textDescription.text = todo.description
                } else {
                    textDescription.visibility = View.GONE
                }

                // Due date
                if (todo.dueDate != null) {
                    iconDueDate.visibility = View.VISIBLE
                    textDueDate.visibility = View.VISIBLE
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    textDueDate.text = "Due: ${dateFormat.format(todo.dueDate)}"

                    // Change color if overdue
                    if (todo.isOverdue()) {
                        textDueDate.setTextColor(ContextCompat.getColor(root.context, android.R.color.holo_red_dark))
                        iconDueDate.setColorFilter(ContextCompat.getColor(root.context, android.R.color.holo_red_dark))
                    } else if (todo.isDueSoon()) {
                        textDueDate.setTextColor(ContextCompat.getColor(root.context, android.R.color.holo_orange_dark))
                        iconDueDate.setColorFilter(ContextCompat.getColor(root.context, android.R.color.holo_orange_dark))
                    } else {
                        textDueDate.setTextColor(ContextCompat.getColor(root.context, R.color.text_secondary_light))
                        iconDueDate.setColorFilter(ContextCompat.getColor(root.context, R.color.text_secondary_light))
                    }
                } else {
                    iconDueDate.visibility = View.GONE
                    textDueDate.visibility = View.GONE
                }

                // Priority
                chipPriority.text = todo.priority.displayName
                val priorityColor = todo.getPriorityColor()
                chipPriority.setChipBackgroundColorResource(
                    when (todo.priority) {
                        ToDo.Priority.LOW -> R.color.priority_low
                        ToDo.Priority.MEDIUM -> R.color.priority_medium
                        ToDo.Priority.HIGH -> R.color.priority_high
                        ToDo.Priority.URGENT -> R.color.priority_urgent
                    }
                )
                priorityIndicator.setBackgroundColor(priorityColor)

                // Completion status
                checkboxComplete.isChecked = todo.isCompleted
                if (todo.isCompleted) {
                    textTitle.paintFlags = textTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    root.alpha = 0.6f
                } else {
                    textTitle.paintFlags = textTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    root.alpha = 1.0f
                }

                // Click listeners
                root.setOnClickListener { 
                    it.scalePress()
                    onItemClick(todo) 
                }
                checkboxComplete.setOnCheckedChangeListener(null)
                checkboxComplete.setOnClickListener {
                    val isChecked = checkboxComplete.isChecked
                    onCheckClick(todo, isChecked)
                }

                btnEdit.setOnClickListener { 
                    it.scalePress()
                    onEditClick(todo) 
                }
                btnDelete.setOnClickListener { 
                    it.scalePress()
                    onDeleteClick(todo) 
                }
            }
        }
    }

    private class ToDoDiffCallback : DiffUtil.ItemCallback<ToDo>() {
        override fun areItemsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ToDo, newItem: ToDo): Boolean {
            return oldItem == newItem
        }
    }
}

