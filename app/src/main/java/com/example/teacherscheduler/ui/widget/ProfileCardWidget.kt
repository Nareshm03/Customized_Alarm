package com.example.teacherscheduler.ui.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.example.teacherscheduler.data.ProfileManager
import com.example.teacherscheduler.databinding.ProfileCardWidgetBinding
import com.example.teacherscheduler.model.UserProfile
import com.example.teacherscheduler.ui.EnhancedProfileActivity

class ProfileCardWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: ProfileCardWidgetBinding
    private lateinit var profileManager: ProfileManager

    init {
        binding = ProfileCardWidgetBinding.inflate(LayoutInflater.from(context), this, true)
        profileManager = ProfileManager(context)
        setupClickListeners()
        loadProfile()
    }

    private fun setupClickListeners() {
        // Click on card to open enhanced profile
        binding.profileCard.setOnClickListener {
            val intent = Intent(context, EnhancedProfileActivity::class.java)
            context.startActivity(intent)
        }

        // Edit button
        binding.buttonEditProfile.setOnClickListener {
            val intent = Intent(context, EnhancedProfileActivity::class.java)
            context.startActivity(intent)
        }
    }

    fun loadProfile() {
        val profile = profileManager.getUserProfile()
        updateUI(profile)
    }

    private fun updateUI(profile: UserProfile) {
        // Set profile name
        binding.textViewProfileName.text = if (profile.name.isNotEmpty()) {
            profile.name
        } else {
            "Your Name"
        }

        // Set designation
        binding.textViewProfileDesignation.text = if (profile.designation.isNotEmpty()) {
            profile.designation
        } else {
            "Add your designation"
        }

        // Set department
        binding.textViewProfileDepartment.text = if (profile.department.isNotEmpty()) {
            profile.department
        } else {
            "Add your department"
        }

        // Load profile picture
        val pictureUrl = profile.profilePictureUrl
        if (pictureUrl.isNotEmpty()) {
            try {
                val uri = Uri.parse(pictureUrl)
                binding.imageViewProfilePic.setImageURI(uri)
            } catch (e: Exception) {
                // Use default image if there's an error
                e.printStackTrace()
            }
        }

        // Set status based on profile completeness
        val completeness = calculateProfileCompleteness(profile)
        when {
            completeness >= 80 -> {
                binding.textViewProfileStatus.text = "Complete"
                binding.textViewProfileStatus.setBackgroundColor(
                    context.getColor(android.R.color.holo_green_dark)
                )
            }
            completeness >= 50 -> {
                binding.textViewProfileStatus.text = "Partial"
                binding.textViewProfileStatus.setBackgroundColor(
                    context.getColor(android.R.color.holo_orange_dark)
                )
            }
            else -> {
                binding.textViewProfileStatus.text = "Incomplete"
                binding.textViewProfileStatus.setBackgroundColor(
                    context.getColor(android.R.color.holo_red_dark)
                )
            }
        }
    }

    private fun calculateProfileCompleteness(profile: UserProfile): Int {
        var completedFields = 0
        val totalFields = 8

        if (profile.name.isNotEmpty()) completedFields++
        if (profile.email.isNotEmpty()) completedFields++
        if (profile.phone.isNotEmpty()) completedFields++
        if (profile.teacherId.isNotEmpty()) completedFields++
        if (profile.designation.isNotEmpty()) completedFields++
        if (profile.department.isNotEmpty()) completedFields++
        if (profile.officeLocation.isNotEmpty()) completedFields++
        if (profile.profilePictureUrl.isNotEmpty()) completedFields++

        return (completedFields * 100) / totalFields
    }

    fun refreshProfile() {
        loadProfile()
    }
}