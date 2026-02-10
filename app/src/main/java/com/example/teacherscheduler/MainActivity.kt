package com.example.teacherscheduler

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.teacherscheduler.data.SettingsManager
import com.example.teacherscheduler.databinding.ActivityMainBinding
import com.example.teacherscheduler.notification.EnhancedNotificationHelper
import com.example.teacherscheduler.ui.ModernAddEditMeetingActivity
import com.example.teacherscheduler.ui.ClassesFragment
import com.example.teacherscheduler.ui.EnhancedDashboardFragment
import com.example.teacherscheduler.ui.MeetingsFragment
import com.example.teacherscheduler.ui.ModernAddEditClassActivity
import com.example.teacherscheduler.ui.NotificationSettingsActivity
import com.example.teacherscheduler.ui.EnhancedProfileActivity
import com.example.teacherscheduler.ui.BackupActivity
import com.example.teacherscheduler.ui.OnboardingActivity
import com.example.teacherscheduler.ui.SettingsFragment
import com.example.teacherscheduler.ui.TestNotificationActivity
import com.example.teacherscheduler.ui.ToDosFragment
import com.example.teacherscheduler.ui.AddEditToDoActivity
import com.example.teacherscheduler.ui.TimetableFragment
import com.example.teacherscheduler.util.HapticFeedbackHelper
import com.example.teacherscheduler.util.NetworkStatusMonitor
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationHelper: EnhancedNotificationHelper
    private lateinit var settingsManager: SettingsManager
    private lateinit var networkMonitor: NetworkStatusMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before super.onCreate
        settingsManager = SettingsManager(this)
        applyTheme()
        
        super.onCreate(savedInstanceState)
        
        // Check onboarding
        if (OnboardingActivity.shouldShowOnboarding(this)) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }
        
        // Firebase Auth disabled for development
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_title)

        // Initialize components
        notificationHelper = EnhancedNotificationHelper(this)

        // Check exact alarm permission
        if (!notificationHelper.canScheduleExactAlarms()) {
            Log.w("MainActivity", "Exact alarm permission not granted. Notifications may not work reliably.")
        }

        // Debug sound settings on startup
        debugSoundSettings()
        
        // Add test notification button for debugging (temporary)
        binding.fab.setOnLongClickListener {
            notificationHelper.sendTestNotification()
            true
        }

        setupTabs()
        setupFab()
        setupNetworkMonitor()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                // Create a dialog fragment for settings
                val settingsFragment = SettingsFragment()
                settingsFragment.show(supportFragmentManager, "settings_dialog")
                true
            }
            R.id.action_dark_mode -> {
                toggleDarkMode()
                true
            }
            R.id.action_test_notifications -> {
                val intent = Intent(this, TestNotificationActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_notification_settings -> {
                val intent = Intent(this, NotificationSettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_profile -> {
                val intent = Intent(this, EnhancedProfileActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_backup -> {
                val intent = Intent(this, BackupActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    // Logout functionality (Firebase disabled for development)
    private fun logout() {
        // Clear local data if needed
        settingsManager.clearUserData()
        
        // Show logout confirmation
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout? (Note: Firebase is disabled, this will just reset local settings)")
            .setPositiveButton("Logout") { _, _ ->
                // In a real app with Firebase, you'd do FirebaseAuth.getInstance().signOut()
                // For now, we just go back to onboarding or close the app
                recreate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupTabs() {
        val adapter = TabsPagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        // Fix nested scrolling conflicts
        binding.viewPager.isNestedScrollingEnabled = true
        binding.viewPager.offscreenPageLimit = 4

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Timetable"
                    tab.setIcon(R.drawable.ic_timetable_24)
                }
                1 -> {
                    tab.text = "Dashboard"
                    tab.setIcon(R.drawable.ic_dashboard_24)
                }
                2 -> {
                    tab.text = "Classes"
                    tab.setIcon(R.drawable.ic_class_24)
                }
                3 -> {
                    tab.text = "Meetings"
                    tab.setIcon(R.drawable.ic_meeting)
                }
                4 -> {
                    tab.text = "To-Dos"
                    tab.setIcon(R.drawable.ic_assignment_24)
                }
            }
        }.attach()
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            // Add haptic feedback
            HapticFeedbackHelper.lightTap(it)

            val currentFragment = getCurrentFragment()
            when (currentFragment) {
                is TimetableFragment -> {
                    // Quick add dialog
                    showAddOptionsDialog()
                }
                is EnhancedDashboardFragment -> {
                    // Show options to add class or meeting
                    showAddOptionsDialog()
                }
                is ClassesFragment -> currentFragment.showAddEditClassActivity(null)
                is MeetingsFragment -> currentFragment.showAddEditMeetingActivity(null)
                is ToDosFragment -> currentFragment.showAddEditToDoActivity(null)
            }
        }
    }

    private fun showAddOptionsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Add New")
            .setMessage("What would you like to add?")
            .setPositiveButton("Class") { _, _ ->
                startActivity(Intent(this, ModernAddEditClassActivity::class.java))
            }
            .setNegativeButton("Meeting") { _, _ ->
                startActivity(Intent(this, ModernAddEditMeetingActivity::class.java))
            }
            .setNeutralButton("To-Do") { _, _ ->
                startActivity(Intent(this, AddEditToDoActivity::class.java))
            }
            .show()
    }

    private fun getCurrentFragment(): Fragment? {
        val adapter = binding.viewPager.adapter as? TabsPagerAdapter
        return adapter?.getFragment(binding.viewPager.currentItem)
    }
    
    // Public method to switch tabs from fragments
    fun switchToTab(tabIndex: Int) {
        if (tabIndex in 0..4) {
            binding.viewPager.currentItem = tabIndex
        }
    }

    override fun onResume() {
        super.onResume()

        // Fragments will automatically refresh via LiveData
    }

    private class TabsPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        private val fragments = mutableMapOf<Int, Fragment>()

        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): Fragment {
            val fragment = when (position) {
                0 -> TimetableFragment()
                1 -> EnhancedDashboardFragment()
                2 -> ClassesFragment()
                3 -> MeetingsFragment()
                4 -> ToDosFragment()
                else -> TimetableFragment()
            }
            fragments[position] = fragment
            return fragment
        }

        fun getFragment(position: Int): Fragment? = fragments[position]
    }
    
    private fun debugSoundSettings() {
        val enhancedHelper = EnhancedNotificationHelper(this)
        enhancedHelper.checkSoundSettings()
    }
    
    private fun setupNetworkMonitor() {
        networkMonitor = NetworkStatusMonitor(this)
        networkMonitor.observe(this) { isOnline ->
            binding.offlineIndicator.visibility = if (isOnline) {
                android.view.View.GONE
            } else {
                android.view.View.VISIBLE
            }
        }
    }
    
    private fun applyTheme() {
        val isDarkMode = settingsManager.isDarkModeEnabled()
        if (isDarkMode) {
            setTheme(R.style.AppTheme)  // AppTheme is now the dark mode theme
        } else {
            setTheme(R.style.AppTheme_Light)
        }
    }
    
    private fun toggleDarkMode() {
        val currentMode = settingsManager.isDarkModeEnabled()
        settingsManager.setDarkModeEnabled(!currentMode)
        recreate()
    }
}
