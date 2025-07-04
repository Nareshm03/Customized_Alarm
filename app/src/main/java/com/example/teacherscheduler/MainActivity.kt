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
import com.example.teacherscheduler.ui.LoginActivity
import com.example.teacherscheduler.ui.MeetingsFragment
import com.example.teacherscheduler.ui.ModernAddEditClassActivity
import com.example.teacherscheduler.ui.NotificationSettingsActivity
import com.example.teacherscheduler.ui.EnhancedProfileActivity
import com.example.teacherscheduler.ui.SettingsFragment
import com.example.teacherscheduler.ui.TestNotificationActivity

import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var settingsManager: SettingsManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        
        // Check if user is logged in
        if (auth.currentUser == null) {
            // User not logged in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = getString(R.string.app_title)

        // Initialize components
        notificationHelper = EnhancedNotificationHelper(this)
        settingsManager = SettingsManager(this)

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
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    // Add logout functionality
    private fun logout() {
        auth.signOut()
        // Note: Google Sign-In logout is now handled by CredentialManager in LoginActivity
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setupTabs() {
        val adapter = TabsPagerAdapter(this)
        binding.viewPager.adapter = adapter
        
        // Fix nested scrolling conflicts
        binding.viewPager.isNestedScrollingEnabled = true
        binding.viewPager.offscreenPageLimit = 3

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "DASHBOARD"
                1 -> tab.text = "CLASSES"
                2 -> tab.text = "MEETINGS"
            }
        }.attach()
    }

    private fun setupFab() {
        binding.fab.setOnClickListener {
            val currentFragment = getCurrentFragment()
            when (currentFragment) {
                is EnhancedDashboardFragment -> {
                    // Show options to add class or meeting
                    showAddOptionsDialog()
                }
                is ClassesFragment -> currentFragment.showAddEditClassActivity(null)
                is MeetingsFragment -> currentFragment.showAddEditMeetingActivity(null)
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
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun getCurrentFragment(): Fragment? {
        val adapter = binding.viewPager.adapter as? TabsPagerAdapter
        return adapter?.getFragment(binding.viewPager.currentItem)
    }
    
    // Public method to switch tabs from fragments
    fun switchToTab(tabIndex: Int) {
        if (tabIndex in 0..2) {
            binding.viewPager.currentItem = tabIndex
        }
    }

    override fun onResume() {
        super.onResume()

        // Fragments will automatically refresh via LiveData
    }

    private class TabsPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        private val fragments = mutableMapOf<Int, Fragment>()

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            val fragment = when (position) {
                0 -> EnhancedDashboardFragment()
                1 -> ClassesFragment()
                2 -> MeetingsFragment()
                else -> EnhancedDashboardFragment()
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
}