package com.example.teacherscheduler.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teacherscheduler.databinding.DialogSmartSuggestionsBinding
import com.example.teacherscheduler.databinding.ItemSuggestionBinding
import com.example.teacherscheduler.util.AnimationUtil
import com.example.teacherscheduler.util.HapticFeedbackUtil
import com.example.teacherscheduler.util.SmartFormHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Smart suggestions dialog with animations
 */
class SmartSuggestionsDialog : BottomSheetDialogFragment() {

    private var _binding: DialogSmartSuggestionsBinding? = null
    private val binding get() = _binding!!

    private var suggestionType: SuggestionType = SuggestionType.SUBJECT
    private var smartHelper: SmartFormHelper? = null
    private var onSuggestionSelected: ((String) -> Unit)? = null

    enum class SuggestionType {
        SUBJECT, DEPARTMENT, ROOM, MEETING_LOCATION, TIME_SLOT
    }

    companion object {
        fun newInstance(
            type: SuggestionType,
            smartHelper: SmartFormHelper,
            onSelected: (String) -> Unit
        ): SmartSuggestionsDialog {
            return SmartSuggestionsDialog().apply {
                this.suggestionType = type
                this.smartHelper = smartHelper
                this.onSuggestionSelected = onSelected
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSmartSuggestionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        loadSuggestions()

        // Animate dialog entrance
        AnimationUtil.slideUp(binding.root, 300)
    }

    private fun setupUI() {
        binding.textTitle.text = when (suggestionType) {
            SuggestionType.SUBJECT -> "Subject Suggestions"
            SuggestionType.DEPARTMENT -> "Department Suggestions"
            SuggestionType.ROOM -> "Room Suggestions"
            SuggestionType.MEETING_LOCATION -> "Location Suggestions"
            SuggestionType.TIME_SLOT -> "Available Time Slots"
        }

        binding.recyclerSuggestions.layoutManager = LinearLayoutManager(context)
    }

    private fun loadSuggestions() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerSuggestions.visibility = View.GONE

        CoroutineScope(Dispatchers.IO).launch {
            val suggestions = when (suggestionType) {
                SuggestionType.SUBJECT -> smartHelper?.getSubjectSuggestions() ?: emptyList()
                SuggestionType.DEPARTMENT -> smartHelper?.getDepartmentSuggestions() ?: emptyList()
                SuggestionType.ROOM -> smartHelper?.getRoomSuggestions() ?: emptyList()
                SuggestionType.MEETING_LOCATION -> smartHelper?.getMeetingLocationSuggestions() ?: emptyList()
                SuggestionType.TIME_SLOT -> emptyList() // TODO: Implement time slot suggestions
            }

            // Get auto-fill suggestions from history
            val historySuggestions = when (suggestionType) {
                SuggestionType.SUBJECT -> smartHelper?.getAutoFillSuggestions("subject") ?: emptyList()
                SuggestionType.DEPARTMENT -> smartHelper?.getAutoFillSuggestions("department") ?: emptyList()
                SuggestionType.ROOM -> smartHelper?.getAutoFillSuggestions("room") ?: emptyList()
                SuggestionType.MEETING_LOCATION -> smartHelper?.getAutoFillSuggestions("meeting_location") ?: emptyList()
                else -> emptyList()
            }

            // Combine and deduplicate
            val allSuggestions = (historySuggestions + suggestions).distinct()

            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.GONE
                binding.recyclerSuggestions.visibility = View.VISIBLE

                val adapter = SuggestionsAdapter(allSuggestions) { suggestion ->
                    HapticFeedbackUtil.lightFeedback(requireContext())
                    onSuggestionSelected?.invoke(suggestion)
                    dismiss()
                }
                binding.recyclerSuggestions.adapter = adapter

                // Animate items
                animateItems()
            }
        }
    }

    private fun animateItems() {
        val adapter = binding.recyclerSuggestions.adapter
        if (adapter != null) {
            for (i in 0 until adapter.itemCount) {
                val viewHolder = binding.recyclerSuggestions.findViewHolderForAdapterPosition(i)
                viewHolder?.itemView?.let { view ->
                    view.alpha = 0f
                    view.translationY = 50f
                    view.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(300)
                        .setStartDelay((i * 50).toLong())
                        .start()
                }
            }
        }
    }

    inner class SuggestionsAdapter(
        private val suggestions: List<String>,
        private val onItemClick: (String) -> Unit
    ) : RecyclerView.Adapter<SuggestionsAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemSuggestionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(suggestions[position])
        }

        override fun getItemCount() = suggestions.size

        inner class ViewHolder(private val binding: ItemSuggestionBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(suggestion: String) {
                binding.textSuggestion.text = suggestion

                binding.root.setOnClickListener {
                    AnimationUtil.scaleDownButton(it) {
                        onItemClick(suggestion)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

