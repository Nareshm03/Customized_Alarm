package com.example.teacherscheduler.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teacherscheduler.databinding.FragmentTodosBinding
import com.example.teacherscheduler.model.ToDo
import com.example.teacherscheduler.ui.adapter.ToDoAdapter
import com.example.teacherscheduler.util.SwipeToDeleteCallback
import com.example.teacherscheduler.viewmodel.ToDoViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ToDosFragment : Fragment() {
    private var _binding: FragmentTodosBinding? = null
    private val binding get() = _binding!!
    private lateinit var todoAdapter: ToDoAdapter
    private lateinit var viewModel: ToDoViewModel
    private var allToDos: List<ToDo> = emptyList()
    private var currentFilter: FilterType = FilterType.ALL
    private var deletedToDo: ToDo? = null

    private enum class FilterType {
        ALL, ACTIVE, COMPLETED, OVERDUE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[ToDoViewModel::class.java]
        setupRecyclerView()
        setupSearch()
        setupFilters()
        setupRecyclerView()
        setupFilters()
        setupSearch()
        setupObservers()
    }


    private fun setupRecyclerView() {
        todoAdapter = ToDoAdapter(
            onItemClick = { todo ->
                showAddEditToDoActivity(todo)
            },
            onCheckClick = { todo, isCompleted ->
                viewModel.toggleCompletion(todo.id, isCompleted)
            },
            onEditClick = { todo ->
                showAddEditToDoActivity(todo)
            },
            onDeleteClick = { todo ->
                showDeleteConfirmation(todo)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = todoAdapter
            setHasFixedSize(true)
        }

        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val todo = todoAdapter.currentList[position]

                lifecycleScope.launch {
                    deletedToDo = todo
                    viewModel.deleteToDo(todo)
                    Snackbar.make(binding.root, "${todo.title} deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO") {
                            deletedToDo?.let {
                                lifecycleScope.launch {
                                    viewModel.insertToDo(it)
                                    deletedToDo = null
                                }
                            }
                        }.show()
                }
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterToDos(s.toString())
            }
        })
    }

    private fun setupFilters() {
        binding.chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = FilterType.ALL
                applyFilter()
            }
        }

        binding.chipActive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = FilterType.ACTIVE
                applyFilter()
            }
        }

        binding.chipCompleted.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = FilterType.COMPLETED
                applyFilter()
            }
        }

        binding.chipOverdue.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = FilterType.OVERDUE
                applyFilter()
            }
        }

        binding.filterButton.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.allToDos.collectLatest { todos ->
                allToDos = todos
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val filteredList = when (currentFilter) {
            FilterType.ALL -> allToDos
            FilterType.ACTIVE -> allToDos.filter { !it.isCompleted }
            FilterType.COMPLETED -> allToDos.filter { it.isCompleted }
            FilterType.OVERDUE -> allToDos.filter { it.isOverdue() }
        }

        updateUI(filteredList)
    }

    private fun filterToDos(query: String) {
        val filtered = allToDos.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }

        val finalList = when (currentFilter) {
            FilterType.ALL -> filtered
            FilterType.ACTIVE -> filtered.filter { !it.isCompleted }
            FilterType.COMPLETED -> filtered.filter { it.isCompleted }
            FilterType.OVERDUE -> filtered.filter { it.isOverdue() }
        }

        updateUI(finalList)
    }

    private fun updateUI(todos: List<ToDo>) {
        todoAdapter.submitList(todos)

        if (todos.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            binding.emptyView.visibility = View.VISIBLE
            binding.textEmptyMessage.text = when (currentFilter) {
                FilterType.ALL -> "No tasks yet"
                FilterType.ACTIVE -> "No active tasks"
                FilterType.COMPLETED -> "No completed tasks"
                FilterType.OVERDUE -> "No overdue tasks"
            }
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyView.visibility = View.GONE
        }
    }

    private fun showFilterDialog() {
        val categories = listOf("All", "Low Priority", "Medium Priority", "High Priority", "Urgent")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filter by Priority")
            .setItems(categories.toTypedArray()) { _, which ->
                when (which) {
                    0 -> applyFilter()
                    1 -> filterByPriority(ToDo.Priority.LOW)
                    2 -> filterByPriority(ToDo.Priority.MEDIUM)
                    3 -> filterByPriority(ToDo.Priority.HIGH)
                    4 -> filterByPriority(ToDo.Priority.URGENT)
                }
            }
            .show()
    }

    private fun filterByPriority(priority: ToDo.Priority) {
        val filtered = allToDos.filter { it.priority == priority && !it.isCompleted }
        updateUI(filtered)
    }

    private fun showDeleteConfirmation(todo: ToDo) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete '${todo.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteToDo(todo)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    fun showAddEditToDoActivity(todo: ToDo? = null) {
        val intent = Intent(requireContext(), AddEditToDoActivity::class.java)
        todo?.let { intent.putExtra("TODO_ID", it.id) }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

