package com.example.teacherscheduler.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.databinding.FragmentClassesBinding
import com.example.teacherscheduler.model.Class
import com.example.teacherscheduler.ui.adapter.ClassAdapter
import kotlinx.coroutines.launch

class ClassesFragment : Fragment() {
    private var _binding: FragmentClassesBinding? = null
    private val binding get() = _binding!!
    private lateinit var classAdapter: ClassAdapter
    private lateinit var repository: Repository

    private val addEditLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Data will automatically refresh via LiveData
        }
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
        repository = Repository(requireContext())
        setupRecyclerView()
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
        }
    }

    private fun setupObservers() {
        repository.getAllActiveClasses().observe(viewLifecycleOwner) { classes ->
            classAdapter.submitList(classes)

            binding.emptyView.visibility = if (classes.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (classes.isEmpty()) View.GONE else View.VISIBLE

            // Update empty message
            if (classes.isEmpty()) {
                binding.textEmptyMessage.text = "No classes\nTap + to add your first class"
            }
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