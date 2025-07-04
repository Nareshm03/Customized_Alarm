package com.example.teacherscheduler.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.teacherscheduler.R
import com.example.teacherscheduler.data.DataManager
import com.example.teacherscheduler.data.FirestoreManager
import com.example.teacherscheduler.data.ProfileManager
import com.example.teacherscheduler.data.Repository
import com.example.teacherscheduler.databinding.ActivityEnhancedProfileBinding
import com.example.teacherscheduler.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class EnhancedProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEnhancedProfileBinding
    private lateinit var profileManager: ProfileManager
    private lateinit var repository: Repository
    private var selectedImageUri: Uri? = null
    private var originalProfile: UserProfile? = null

    // Register for activity result to handle image selection
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                try {
                    // Take persistent URI permission to maintain access
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                    binding.imageViewProfile.setImageURI(uri)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Unable to access the selected image", Toast.LENGTH_SHORT).show()
                    selectedImageUri = null
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnhancedProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize managers
        profileManager = ProfileManager(this)
        repository = Repository(this)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // Set up collapsing toolbar
        binding.collapsingToolbar.title = "My Profile"

        // Set up components
        setupGenderSpinner()
        setupClickListeners()
        loadUserProfile()
        loadStatistics()
    }

    private fun setupGenderSpinner() {
        val genders = arrayOf("Male", "Female", "Transgender", "Non-binary", "Prefer not to say")
        val adapter = ArrayAdapter(this, R.layout.dropdown_item, genders)
        binding.spinnerGender.setAdapter(adapter)
        
        binding.spinnerGender.setOnClickListener {
            binding.spinnerGender.showDropDown()
        }
    }

    private fun setupClickListeners() {
        // Profile picture change
        binding.fabChangePicture.setOnClickListener {
            openImagePicker()
        }

        // Save button
        binding.buttonSave.setOnClickListener {
            saveUserProfile()
        }

        // Reset button
        binding.buttonReset.setOnClickListener {
            resetToOriginal()
        }

        // Quick Actions
        findViewById<View>(R.id.cardShareProfile)?.setOnClickListener {
            shareProfile()
        }

        findViewById<View>(R.id.cardExportProfile)?.setOnClickListener {
            exportProfile()
        }

        findViewById<View>(R.id.cardPrintProfile)?.setOnClickListener {
            printProfile()
        }

        findViewById<View>(R.id.cardBackupProfile)?.setOnClickListener {
            backupProfile()
        }
    }

    private fun openImagePicker() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Unable to open image picker", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserProfile() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        
        if (currentUser != null) {
            binding.progressBar.visibility = View.VISIBLE
            
            lifecycleScope.launch {
                try {
                    val firestoreManager = FirestoreManager(this@EnhancedProfileActivity)
                    val cloudProfile = firestoreManager.getUserProfile()
                    
                    if (cloudProfile != null) {
                        DataManager.updateUserProfile(cloudProfile)
                        profileManager.saveUserProfile(cloudProfile)
                        
                        withContext(Dispatchers.Main) {
                            displayProfile(cloudProfile)
                            binding.progressBar.visibility = View.GONE
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            val localProfile = profileManager.getUserProfile()
                            displayProfile(localProfile)
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        val localProfile = profileManager.getUserProfile()
                        displayProfile(localProfile)
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@EnhancedProfileActivity, "Error loading cloud profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            val localProfile = profileManager.getUserProfile()
            displayProfile(localProfile)
        }
    }
    
    private fun displayProfile(profile: UserProfile) {
        // Store original profile for reset functionality
        originalProfile = profile
        
        // Set header information
        binding.textViewName.text = if (profile.name.isNotEmpty()) profile.name else "Your Name"
        binding.textViewEmail.text = if (profile.email.isNotEmpty()) profile.email else "your.email@example.com"
        
        // Set form fields
        binding.editTextName.setText(profile.name)
        binding.editTextEmail.setText(profile.email)
        binding.editTextPhone.setText(profile.phone)
        binding.editTextTeacherId.setText(profile.teacherId)
        binding.editTextDesignation.setText(profile.designation)
        binding.editTextDepartment.setText(profile.department)
        binding.editTextOffice.setText(profile.officeLocation)
        
        // Set gender
        if (profile.gender.isNotEmpty()) {
            binding.spinnerGender.setText(profile.gender, false)
        } else {
            binding.spinnerGender.setText("", false)
        }
        
        // Load profile picture
        val pictureUrl = profile.profilePictureUrl
        if (pictureUrl.isNotEmpty()) {
            try {
                val uri = Uri.parse(pictureUrl)
                try {
                    binding.imageViewProfile.setImageURI(uri)
                    selectedImageUri = uri
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    selectedImageUri = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Update profile completion progress
        updateProfileCompletion(profile)
    }

    private fun loadStatistics() {
        lifecycleScope.launch {
            try {
                // Get total classes and meetings
                val totalClasses = repository.getAllActiveClassesSync().size
                val totalMeetings = repository.getAllActiveMeetingsSync().size
                
                // Get member since date (from profile creation or Firebase auth)
                val currentUser = FirebaseAuth.getInstance().currentUser
                val memberSince = if (currentUser != null) {
                    val creationTime = currentUser.metadata?.creationTimestamp ?: System.currentTimeMillis()
                    val year = Calendar.getInstance().apply { timeInMillis = creationTime }.get(Calendar.YEAR)
                    year.toString()
                } else {
                    Calendar.getInstance().get(Calendar.YEAR).toString()
                }
                
                withContext(Dispatchers.Main) {
                    binding.textTotalClasses.text = totalClasses.toString()
                    binding.textTotalMeetings.text = totalMeetings.toString()
                    binding.textMemberSince.text = memberSince
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.textTotalClasses.text = "0"
                    binding.textTotalMeetings.text = "0"
                    binding.textMemberSince.text = Calendar.getInstance().get(Calendar.YEAR).toString()
                }
            }
        }
    }

    private fun updateProfileCompletion(profile: UserProfile) {
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

        val completionPercentage = (completedFields * 100) / totalFields
        
        binding.progressProfileCompletion.progress = completionPercentage
        binding.textProfileCompletionPercent.text = "$completionPercentage%"
    }

    private fun resetToOriginal() {
        originalProfile?.let { profile ->
            displayProfile(profile)
            Toast.makeText(this, "Profile reset to original values", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun saveUserProfile() {
        val name = binding.editTextName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()
        val teacherId = binding.editTextTeacherId.text.toString().trim()
        val gender = binding.spinnerGender.text.toString()
        val designation = binding.editTextDesignation.text.toString().trim()
        val department = binding.editTextDepartment.text.toString().trim()
        val office = binding.editTextOffice.text.toString().trim()

        // Validate required fields
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and email are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current user ID from Firebase Auth
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: email
        
        // Create and save profile
        val profile = UserProfile(
            id = userId,
            name = name,
            email = userEmail,
            phone = phone,
            teacherId = teacherId,
            gender = gender,
            designation = designation,
            department = department,
            officeLocation = office,
            profilePictureUrl = selectedImageUri?.toString() ?: ""
        )
        
        // Save to local storage
        profileManager.saveUserProfile(profile)
        
        // Update DataManager with the new profile
        DataManager.updateUserProfile(profile)
        
        // Sync to Firestore if user is logged in
        if (FirebaseAuth.getInstance().currentUser != null) {
            binding.buttonSave.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            
            lifecycleScope.launch {
                try {
                    val firestoreManager = FirestoreManager(this@EnhancedProfileActivity)
                    val success = firestoreManager.syncUserProfile(profile)
                    
                    withContext(Dispatchers.Main) {
                        if (success) {
                            Toast.makeText(this@EnhancedProfileActivity, "Profile saved and synced to cloud", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@EnhancedProfileActivity, "Profile saved locally but sync failed", Toast.LENGTH_SHORT).show()
                        }
                        
                        // Update header
                        binding.textViewName.text = name
                        binding.textViewEmail.text = email
                        
                        // Update original profile for reset functionality
                        originalProfile = profile
                        
                        binding.buttonSave.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EnhancedProfileActivity, "Error during sync: ${e.message}", Toast.LENGTH_SHORT).show()
                        binding.buttonSave.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }
        } else {
            // Update header and finish
            binding.textViewName.text = name
            binding.textViewEmail.text = email
            originalProfile = profile
            
            Toast.makeText(this, "Profile saved locally", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun shareProfile() {
        val profile = profileManager.getUserProfile()
        val shareText = buildString {
            appendLine("📋 Teacher Profile")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine("👤 Name: ${profile.name}")
            if (profile.designation.isNotEmpty()) {
                appendLine("💼 Designation: ${profile.designation}")
            }
            if (profile.department.isNotEmpty()) {
                appendLine("🏢 Department: ${profile.department}")
            }
            if (profile.teacherId.isNotEmpty()) {
                appendLine("🆔 Teacher ID: ${profile.teacherId}")
            }
            if (profile.email.isNotEmpty()) {
                appendLine("📧 Email: ${profile.email}")
            }
            if (profile.phone.isNotEmpty()) {
                appendLine("📱 Phone: ${profile.phone}")
            }
            if (profile.officeLocation.isNotEmpty()) {
                appendLine("📍 Office: ${profile.officeLocation}")
            }
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine("Generated by Teacher Scheduler App")
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Teacher Profile - ${profile.name}")
        }

        startActivity(Intent.createChooser(shareIntent, "Share Profile"))
    }

    private fun exportProfile() {
        Toast.makeText(this, "Export functionality coming soon!", Toast.LENGTH_SHORT).show()
        // TODO: Implement profile export to PDF/JSON
    }

    private fun printProfile() {
        Toast.makeText(this, "Print functionality coming soon!", Toast.LENGTH_SHORT).show()
        // TODO: Implement profile printing
    }

    private fun backupProfile() {
        val profile = profileManager.getUserProfile()
        
        // Create backup intent
        val backupIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "application/json"
            putExtra(Intent.EXTRA_TEXT, "Profile backup for ${profile.name}")
        }

        try {
            startActivity(Intent.createChooser(backupIntent, "Backup Profile"))
            Toast.makeText(this, "Profile backup initiated", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Backup failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}