package com.example.teacherscheduler.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.teacherscheduler.R
import com.example.teacherscheduler.databinding.DialogTemplateSelectionBinding
import com.example.teacherscheduler.databinding.ItemTemplateBinding
import com.example.teacherscheduler.util.SmartFormHelper
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TemplateSelectionDialog : BottomSheetDialogFragment() {

    private var _binding: DialogTemplateSelectionBinding? = null
    private val binding get() = _binding!!

    private var templateType: TemplateType = TemplateType.CLASS
    private var onTemplateSelected: ((Any) -> Unit)? = null

    enum class TemplateType {
        CLASS, MEETING
    }

    companion object {
        fun newInstance(
            type: TemplateType,
            onSelected: (Any) -> Unit
        ): TemplateSelectionDialog {
            return TemplateSelectionDialog().apply {
                this.templateType = type
                this.onTemplateSelected = onSelected
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTemplateSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.textTitle.text = when (templateType) {
            TemplateType.CLASS -> "Choose Class Template"
            TemplateType.MEETING -> "Choose Meeting Template"
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val smartHelper = SmartFormHelper(requireContext(), com.example.teacherscheduler.data.Repository(requireContext()))

        val templates = when (templateType) {
            TemplateType.CLASS -> smartHelper.getClassTemplates()
            TemplateType.MEETING -> smartHelper.getMeetingTemplates()
        }

        val adapter = TemplateAdapter(templates) { template ->
            onTemplateSelected?.invoke(template)
            dismiss()
        }

        binding.recyclerTemplates.layoutManager = LinearLayoutManager(context)
        binding.recyclerTemplates.adapter = adapter
    }

    inner class TemplateAdapter(
        private val templates: List<Any>,
        private val onTemplateClick: (Any) -> Unit
    ) : RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
            val binding = ItemTemplateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return TemplateViewHolder(binding)
        }

        override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
            holder.bind(templates[position])
        }

        override fun getItemCount() = templates.size

        inner class TemplateViewHolder(private val binding: ItemTemplateBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(template: Any) {
                when (template) {
                    is SmartFormHelper.ClassTemplate -> {
                        binding.textIcon.text = template.icon
                        binding.textName.text = template.name
                        binding.textDescription.text = "${template.subject} - ${template.duration} min"
                    }
                    is SmartFormHelper.MeetingTemplate -> {
                        binding.textIcon.text = template.icon
                        binding.textName.text = template.name
                        binding.textDescription.text = "${template.title} - ${template.duration} min"
                    }
                }

                binding.root.setOnClickListener {
                    onTemplateClick(template)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

