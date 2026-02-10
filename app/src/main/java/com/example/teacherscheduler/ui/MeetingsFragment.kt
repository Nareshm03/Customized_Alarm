package com.example.teacherscheduler.ui

import android.app.Activity
import android.content.Intent
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
import com.example.teacherscheduler.databinding.FragmentMeetingsBinding
import com.example.teacherscheduler.model.Meeting
import com.example.teacherscheduler.ui.adapter.MeetingAdapter
import com.example.teacherscheduler.util.SwipeToDeleteCallback
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MeetingsFragment : Fragment() {
    private var _binding: FragmentMeetingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var meetingAdapter: MeetingAdapter
    private lateinit var repository: Repository
    private lateinit var settingsManager: SettingsManager
    private var allMeetings: List<Meeting> = emptyList()
    private var deletedMeeting: Meeting? = null

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
        settingsManager = SettingsManager(requireContext())
        setupRecyclerView()
        setupSearch()
        setupSortButton()
        setupRecyclerView()
        setupSearch()
        setupSortButton()
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
            setHasFixedSize(true)
            setItemViewCacheSize(20)
            itemAnimator?.changeDuration = 300
        }
        
        val swipeHandler = object : SwipeToDeleteCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val meeting = meetingAdapter.currentList[position]
                
                lifecycleScope.launch {
                    deletedMeeting = meeting
                    repository.deleteMeeting(meeting)
                    Snackbar.make(binding.root, "${meeting.title} deleted", Snackbar.LENGTH_LONG)
                        .setAction("UNDO") {
                            deletedMeeting?.let {
                                lifecycleScope.launch {
                                    repository.insertMeeting(it)
                                    deletedMeeting = null
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
            repository.getAllActiveMeetings().collect { meetings ->
                allMeetings = meetings
                filterMeetings(binding.searchEditText.text.toString())
            }
        }
    }
    
    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterMeetings(s.toString())
            }
        })
    }
    
    private fun filterMeetings(query: String) {
        val filtered = if (query.isEmpty()) {
            allMeetings
        } else {
            allMeetings.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.withWhom.contains(query, ignoreCase = true) ||
                it.location.contains(query, ignoreCase = true)
            }
        }
        
        val sorted = sortMeetings(filtered)
        
        meetingAdapter.submitList(sorted)

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
                "No meetings yet\nSchedule your first meeting and stay organized 🤝"
            } else {
                "No meetings found for \"$query\""
            }
        }
    }
    
    private fun sortMeetings(meetings: List<Meeting>): List<Meeting> {
        return when (settingsManager.getSortOrder()) {
            "name" -> meetings.sortedBy { it.title }
            "location" -> meetings.sortedBy { it.location }
            "time" -> meetings.sortedBy { it.startTime }
            else -> meetings.sortedBy { it.date }
        }
    }
    
    private fun setupSortButton() {
        binding.sortButton?.setOnClickListener {
            showSortDialog()
        }
    }
    
    private fun showSortDialog() {
        val options = arrayOf("By Date", "By Name", "By Location", "By Time")
        val current = when (settingsManager.getSortOrder()) {
            "name" -> 1
            "location" -> 2
            "time" -> 3
            else -> 0
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Sort Meetings")
            .setSingleChoiceItems(options, current) { dialog, which ->
                val order = when (which) {
                    1 -> "name"
                    2 -> "location"
                    3 -> "time"
                    else -> "date"
                }
                settingsManager.setSortOrder(order)
                filterMeetings(binding.searchEditText.text.toString())
                dialog.dismiss()
            }
            .show()
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