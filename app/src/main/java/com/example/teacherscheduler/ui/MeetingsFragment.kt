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
import com.example.teacherscheduler.databinding.FragmentMeetingsBinding
import com.example.teacherscheduler.model.Meeting
import com.example.teacherscheduler.ui.adapter.MeetingAdapter
import kotlinx.coroutines.launch

class MeetingsFragment : Fragment() {
    private var _binding: FragmentMeetingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var meetingAdapter: MeetingAdapter
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
        _binding = FragmentMeetingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = Repository(requireContext())
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        meetingAdapter = MeetingAdapter(
            onEditClick = { meeting ->
                showAddEditMeetingActivity(meeting)
            },
            onDeleteClick = { meeting ->
                lifecycleScope.launch {
                    repository.deleteMeeting(meeting)
                }
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = meetingAdapter
        }
    }

    private fun setupObservers() {
        repository.getAllActiveMeetings().observe(viewLifecycleOwner) { meetings ->
            meetingAdapter.submitList(meetings)

            binding.emptyView.visibility = if (meetings.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerView.visibility = if (meetings.isEmpty()) View.GONE else View.VISIBLE

            // Update empty message
            if (meetings.isEmpty()) {
                binding.textEmptyMessage.text = "No meetings\nTap + to add your first meeting"
            }
        }
    }

    fun showAddEditMeetingActivity(meeting: Meeting?) {
        val intent = ModernAddEditMeetingActivity.newIntent(requireContext(), meeting?.id)
        addEditLauncher.launch(intent)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}