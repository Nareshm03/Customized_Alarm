package com.example.teacherscheduler.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.teacherscheduler.databinding.ActivityOnboardingBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    private var isFinishing = false

    companion object {
        private const val TAG = "OnboardingActivity"

        fun shouldShowOnboarding(context: Context): Boolean {
            return try {
                !context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    .getBoolean("onboarding_completed", false)
            } catch (e: Exception) {
                Log.e(TAG, "Error checking onboarding status", e)
                false
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            Log.d(TAG, "OnboardingActivity onCreate started")

            binding = ActivityOnboardingBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setupViewPager()
            setupButtons()

            Log.d(TAG, "OnboardingActivity onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate", e)
            // If onboarding fails, just skip it
            finishOnboarding()
        }
    }
    
    private fun setupViewPager() {
        try {
            val pages = listOf(
                OnboardingPage("Welcome to Teacher Scheduler", "Manage your classes and meetings efficiently", android.R.drawable.ic_dialog_info),
                OnboardingPage("Smart Notifications", "Get reminders before your classes and meetings", android.R.drawable.ic_dialog_alert),
                OnboardingPage("Sync Across Devices", "Your schedule syncs automatically with Firebase", android.R.drawable.ic_menu_rotate)
            )

            binding.viewPager.adapter = OnboardingAdapter(pages)

            // Use try-catch for TabLayoutMediator to prevent crashes
            try {
                TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()
            } catch (e: Exception) {
                Log.e(TAG, "Error attaching TabLayoutMediator", e)
            }

            binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    try {
                        binding.buttonNext.text = if (position == pages.size - 1) "Get Started" else "Next"
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating button text", e)
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupViewPager", e)
            // If setup fails, just finish onboarding
            finishOnboarding()
        }
    }
    
    private fun setupButtons() {
        binding.buttonNext.setOnClickListener {
            try {
                if (binding.viewPager.currentItem < 2) {
                    binding.viewPager.currentItem++
                } else {
                    finishOnboarding()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in button click", e)
                finishOnboarding()
            }
        }
        
        binding.buttonSkip.setOnClickListener {
            finishOnboarding()
        }
    }
    
    private fun finishOnboarding() {
        if (isFinishing) {
            Log.d(TAG, "Already finishing, ignoring duplicate call")
            return
        }

        isFinishing = true
        Log.d(TAG, "Finishing onboarding")

        // Use coroutine to save preference asynchronously
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .putBoolean("onboarding_completed", true)
                        .apply()
                }

                // Start MainActivity on main thread
                withContext(Dispatchers.Main) {
                    try {
                        val intent = Intent(this@OnboardingActivity, com.example.teacherscheduler.MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting MainActivity", e)
                        finish()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving onboarding completion", e)
                withContext(Dispatchers.Main) {
                    finish()
                }
            }
        }
    }
    
    data class OnboardingPage(val title: String, val description: String, val icon: Int)
    
    private class OnboardingAdapter(private val pages: List<OnboardingPage>) : 
        androidx.recyclerview.widget.RecyclerView.Adapter<OnboardingViewHolder>() {
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): OnboardingViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(com.example.teacherscheduler.R.layout.item_onboarding_page, parent, false)
            return OnboardingViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
            holder.bind(pages[position])
        }
        
        override fun getItemCount() = pages.size
    }
    
    private class OnboardingViewHolder(view: android.view.View) : 
        androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        
        private val imageIcon: android.widget.ImageView = view.findViewById(com.example.teacherscheduler.R.id.imageIcon)
        private val textTitle: android.widget.TextView = view.findViewById(com.example.teacherscheduler.R.id.textTitle)
        private val textDescription: android.widget.TextView = view.findViewById(com.example.teacherscheduler.R.id.textDescription)

        fun bind(page: OnboardingPage) {
            imageIcon.setImageResource(page.icon)
            textTitle.text = page.title
            textDescription.text = page.description
        }
    }
}
