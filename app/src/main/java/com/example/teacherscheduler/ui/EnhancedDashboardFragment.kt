package com.example.teacherscheduler.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.teacherscheduler.R
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.databinding.FragmentDashboardBinding
import com.example.teacherscheduler.ui.adapter.ClassAdapter
import com.example.teacherscheduler.ui.adapter.MeetingAdapter
import com.example.teacherscheduler.ui.adapter.ToDoAdapter
import com.example.teacherscheduler.viewmodel.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EnhancedDashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val dashboardViewModel: DashboardViewModel by viewModels()

    private lateinit var classAdapter: ClassAdapter
    private lateinit var meetingAdapter: MeetingAdapter
    private lateinit var todoAdapter: ToDoAdapter
    private lateinit var repository: Repository

    private val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository = Repository(requireContext())

        setupViews()
        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViews() {
        binding.textViewDate.text = dateFormat.format(Date())
    }

    private fun setupRecyclerViews() {
        classAdapter = ClassAdapter(
            onEditClick = { classItem ->
                val intent = ModernAddEditClassActivity.newIntent(requireContext(), classItem.id)
                startActivity(intent)
            },
            onDeleteClick = { classItem ->
                dashboardViewModel.deleteClass(classItem)
            }
        )

        binding.recyclerViewClasses.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = classAdapter
            isNestedScrollingEnabled = false
        }

        meetingAdapter = MeetingAdapter(
            onEditClick = { meeting ->
                val intent = Intent(requireContext(), ModernAddEditMeetingActivity::class.java)
                intent.putExtra(ModernAddEditMeetingActivity.EXTRA_MEETING_ID, meeting.id)
                startActivity(intent)
            },
            onDeleteClick = { meeting ->
                dashboardViewModel.deleteMeeting(meeting)
            }
        )

        binding.recyclerViewMeetings.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = meetingAdapter
            isNestedScrollingEnabled = false
        }

        todoAdapter = ToDoAdapter(
            onItemClick = { todo ->
                val intent = Intent(requireContext(), AddEditToDoActivity::class.java)
                intent.putExtra("TODO_ID", todo.id)
                startActivity(intent)
            },
            onCheckClick = { todo, isCompleted ->
                lifecycleScope.launch {
                    repository.toggleToDoCompletion(todo.id, isCompleted)
                }
            },
            onEditClick = { todo ->
                val intent = Intent(requireContext(), AddEditToDoActivity::class.java)
                intent.putExtra("TODO_ID", todo.id)
                startActivity(intent)
            },
            onDeleteClick = { todo ->
                lifecycleScope.launch {
                    repository.deleteToDo(todo)
                }
            }
        )

        binding.recyclerViewToDos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = todoAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners() {
        binding.buttonAddClass.setOnClickListener {
            startActivity(Intent(requireContext(), ModernAddEditClassActivity::class.java))
        }

        binding.buttonAddMeeting.setOnClickListener {
            startActivity(Intent(requireContext(), ModernAddEditMeetingActivity::class.java))
        }

        binding.buttonAddToDo.setOnClickListener {
            startActivity(Intent(requireContext(), AddEditToDoActivity::class.java))
        }

        // Setup stats card clicks to navigate to tabs
        // Note: Navigation disabled as this fragment is not currently used in the app
        val todayClassesCard = binding.statsContainer.getChildAt(0)
        todayClassesCard.setOnClickListener {
            // (activity as? MainActivity)?.switchToTab(1)
        }

        val upcomingMeetingsCard = binding.statsContainer.getChildAt(1)
        upcomingMeetingsCard.setOnClickListener {
            // (activity as? MainActivity)?.switchToTab(2)
        }

        val activeToDosCard = binding.statsContainer.getChildAt(2)
        activeToDosCard.setOnClickListener {
            // (activity as? MainActivity)?.switchToTab(3)
        }

        // Setup enhanced calendar
        setupCalendar()
    }

    private fun updateWelcomeMessage() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        val greeting = when (hour) {
            in 5..11 -> "Good Morning! 👋"
            in 12..16 -> "Good Afternoon! 👋"
            in 17..20 -> "Good Evening! 👋"
            else -> "Hello! 👋"
        }

        binding.textViewWelcome.text = greeting
    }

    private fun animateCounter(textView: TextView, targetValue: Int) {
        val animator = android.animation.ValueAnimator.ofInt(0, targetValue)
        animator.duration = 800 // 800ms animation
        animator.interpolator = android.view.animation.DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            textView.text = animation.animatedValue.toString()
        }
        animator.start()
    }

    private fun updateFocusMessage(classesCount: Int, meetingsCount: Int, todosCount: Int) {
        val focusMessage = when {
            classesCount == 0 && meetingsCount == 0 && todosCount == 0 ->
                "You're free today — plan something meaningful ✨"
            classesCount > 0 && meetingsCount > 0 ->
                "Busy day ahead — you have $classesCount ${if (classesCount == 1) "class" else "classes"} and $meetingsCount ${if (meetingsCount == 1) "meeting" else "meetings"} 📚"
            classesCount > 0 && meetingsCount == 0 ->
                "Focus on teaching — $classesCount ${if (classesCount == 1) "class" else "classes"} scheduled today 📖"
            classesCount == 0 && meetingsCount > 0 ->
                "Meeting day — $meetingsCount ${if (meetingsCount == 1) "meeting" else "meetings"} on your schedule 🤝"
            todosCount > 0 ->
                "Light schedule — great time to tackle those $todosCount to-dos ✅"
            else ->
                "Make today count! 🌟"
        }

        binding.textViewFocus.text = focusMessage
    }

    private fun setupCalendar() {
        // Enhanced calendar view
        binding.enhancedCalendarView.setOnDateSelectedListener { selectedDate ->
            // Date selected - could filter events by date
        }

        // Week grid view
        binding.weekGridView.onEventClickListener = { event ->
            when (event) {
                is com.example.teacherscheduler.model.Class -> {
                    val intent = ModernAddEditClassActivity.newIntent(requireContext(), event.id)
                    startActivity(intent)
                }
                is com.example.teacherscheduler.model.Meeting -> {
                    val intent = Intent(requireContext(), ModernAddEditMeetingActivity::class.java)
                    intent.putExtra(ModernAddEditMeetingActivity.EXTRA_MEETING_ID, event.id)
                    startActivity(intent)
                }
            }
        }

        // View toggle
        binding.viewToggleGroup.check(R.id.btnMonthView)

        binding.viewToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnMonthView -> {
                        binding.enhancedCalendarView.visibility = View.VISIBLE
                        binding.weekGridView.visibility = View.GONE
                    }
                    R.id.btnWeekView -> {
                        binding.enhancedCalendarView.visibility = View.GONE
                        binding.weekGridView.visibility = View.VISIBLE
                    }
                }
            }
        }

        updateCalendarWithEvents()
    }

    private fun updateCalendarWithEvents() {
        lifecycleScope.launch {
            try {
                val classes = repository.getAllActiveClassesSync()
                val meetings = repository.getAllActiveMeetingsSync()

                binding.enhancedCalendarView.setEvents(classes, meetings)
                binding.weekGridView.setEvents(classes, meetings)
            } catch (e: Exception) {
                // Silent error handling
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            dashboardViewModel.dashboardState.collectLatest { state ->
                updateWelcomeMessage()

                // Animate stats counting
                animateCounter(binding.todayClassesCount, state.todayClassesCount)
                animateCounter(binding.upcomingMeetingsCount, state.upcomingMeetingsCount)
                animateCounter(binding.activeToDosCount, state.activeToDosCount)

                // Update focus message based on today's schedule
                updateFocusMessage(state.todayClassesCount, state.upcomingMeetingsCount, state.activeToDosCount)

                // Weekly stats
                binding.textWeekClasses.text = state.weekClassesCount.toString()
                binding.textWeekMeetings.text = state.weekMeetingsCount.toString()
                binding.textWeekHours.text = String.format(Locale.getDefault(), "%.1f hrs", state.weekHours)

                // Next event
                binding.textNextEventTitle.text = state.nextEventTitle
                if (state.nextEventTime.isNotEmpty()) {
                    binding.textNextEventTime.text = state.nextEventTime
                    binding.textNextEventTime.visibility = View.VISIBLE
                } else {
                    binding.textNextEventTime.visibility = View.GONE
                }

                // Insights
                updateInsights(state.insights)

                classAdapter.submitList(state.todayClasses)
                meetingAdapter.submitList(state.upcomingMeetings)
                todoAdapter.submitList(state.urgentToDos)

                binding.textViewNoClasses.visibility = if (state.todayClasses.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerViewClasses.visibility = if (state.todayClasses.isEmpty()) View.GONE else View.VISIBLE

                binding.textViewNoMeetings.visibility = if (state.upcomingMeetings.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerViewMeetings.visibility = if (state.upcomingMeetings.isEmpty()) View.GONE else View.VISIBLE

                binding.textViewNoToDos.visibility = if (state.urgentToDos.isEmpty()) View.VISIBLE else View.GONE
                binding.recyclerViewToDos.visibility = if (state.urgentToDos.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun updateInsights(insights: List<String>) {
        // Update insight 1 (always visible if there are insights)
        if (insights.isNotEmpty()) {
            binding.textInsight1.text = insights[0]
            binding.textInsight1.visibility = View.VISIBLE
        } else {
            binding.textInsight1.visibility = View.GONE
        }

        // Update insight 2
        if (insights.size > 1) {
            binding.textInsight2.text = insights[1]
            binding.textInsight2.visibility = View.VISIBLE
        } else {
            binding.textInsight2.visibility = View.GONE
        }

        // Update insight 3
        if (insights.size > 2) {
            binding.textInsight3.text = insights[2]
            binding.textInsight3.visibility = View.VISIBLE
        } else {
            binding.textInsight3.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}