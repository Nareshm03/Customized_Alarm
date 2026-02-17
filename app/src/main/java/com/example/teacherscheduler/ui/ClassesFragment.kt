package com.example.teacherscheduler.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teacherscheduler.R
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.data.SettingsManager
import com.example.teacherscheduler.databinding.FragmentClassesBinding
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.ui.adapter.ClassAdapter
import com.example.teacherscheduler.util.fadeIn
import com.example.teacherscheduler.util.SwipeToDeleteCallback
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ClassesFragment : Fragment() {
    private var _binding: FragmentClassesBinding? = null
    private val binding get() = _binding!!
    private lateinit var classAdapter: ClassAdapter
    private lateinit var repository: Repository
    private lateinit var settingsManager: SettingsManager
    private var allClasses: List<Class> = emptyList()
    private var deletedClass: Class? = null

    private val addEditLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Data will automatically refresh via LiveData observer in setupObservers()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClassesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.fadeIn()
        repository = Repository(requireContext())
        settingsManager = SettingsManager(requireContext())
        setupRecyclerView()
        setupSearch()
        setupSortButton()
        setupEmptyAction()
        setupObservers()
    }


    private fun setupRecyclerView() {
        classAdapter = ClassAdapter(
            onEditClick = { classItem ->
                showAddEditClassActivity(classItem)
            },
            onDeleteClick = { classItem ->
                lifecycleScope.launch {
                    repository.deleteClass(classItem)
                }
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = classAdapter
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            itemAnimator?.changeDuration = 300
        }
        
        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val classItem = classAdapter.currentList[position]
                
                lifecycleScope.launch {
                    deletedClass = classItem
                    repository.deleteClass(classItem)
                    Snackbar.make(binding.root, "${classItem.subject} deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO") {
                            deletedClass?.let {
                                lifecycleScope.launch {
                                    repository.insertClass(it)
                                    deletedClass = null
                                }
                            }
                        }.show()
                }
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            repository.getAllActiveClasses().collect { classes ->
                allClasses = classes
                filterClasses(binding.searchEditText.text.toString())
            }
        }
    }
    
    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterClasses(s.toString())
            }
        })
    }
    
    private fun filterClasses(query: String) {
        val filtered = if (query.isEmpty()) {
            allClasses
        } else {
            allClasses.filter {
                it.subject.contains(query, ignoreCase = true) ||
                it.department.contains(query, ignoreCase = true) ||
                it.roomNumber.contains(query, ignoreCase = true)
            }
        }
        
        val sorted = sortClasses(filtered)
        
        classAdapter.submitList(sorted)

        val wasEmpty = binding.emptyView.visibility == View.VISIBLE
        val isEmpty = sorted.isEmpty()

        binding.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE

        // Animate empty state when it appears
        if (isEmpty && !wasEmpty) {
            val fadeInAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.empty_state_fade_in)
            binding.emptyView.startAnimation(fadeInAnim)

            // Animate the icon separately for a nice effect
            binding.emptyView.findViewById<View>(R.id.emptyStateIcon)?.let { icon ->
                val bounceAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.empty_state_icon_bounce)
                icon.startAnimation(bounceAnim)
            }
        }

        if (sorted.isEmpty()) {
            binding.textEmptyMessage.text = if (query.isEmpty()) {
                "No classes yet\nAdd your first class and stay on track 🎯"
            } else {
                "No classes found for \"$query\""
            }
        }
    }
    
    private fun sortClasses(classes: List<Class>): List<Class> {
        return when (settingsManager.getSortOrder()) {
            "name" -> classes.sortedBy { it.subject }
            "department" -> classes.sortedBy { it.department }
            "time" -> classes.sortedBy { it.startTime }
            else -> classes.sortedBy { it.startDate }
        }
    }
    
    private fun setupSortButton() {
        binding.sortButton.setOnClickListener {
            showSortDialog()
        }
    }
    
    private fun showSortDialog() {
        val options = arrayOf("By Date", "By Name", "By Department", "By Time")
        val current = when (settingsManager.getSortOrder()) {
            "name" -> 1
            "department" -> 2
            "time" -> 3
            else -> 0
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Sort Classes")
            .setSingleChoiceItems(options, current) { dialog, which ->
                val order = when (which) {
                    1 -> "name"
                    2 -> "department"
                    3 -> "time"
                    else -> "date"
                }
                settingsManager.setSortOrder(order)
                filterClasses(binding.searchEditText.text.toString())
                dialog.dismiss()
            }
            .show()
    }

    private fun setupEmptyAction() {
        binding.emptyActionButton?.setOnClickListener {
            showAddEditClassActivity(null)
        }
    }

    fun showAddEditClassActivity(classItem: Class?) {
        val intent = AddEditClassActivity.newIntent(requireContext(), classItem?.id)
        addEditLauncher.launch(intent)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}