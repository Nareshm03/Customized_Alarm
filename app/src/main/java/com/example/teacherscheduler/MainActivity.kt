package com.example.teacherscheduler

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.teacherscheduler.ui.compose.MainNavigationScreen
import com.example.teacherscheduler.ui.EnhancedProfileActivity
import com.example.teacherscheduler.ui.OnboardingActivity
import com.example.teacherscheduler.ui.theme.TeacherSchedulerTheme
import com.example.teacherscheduler.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val userViewModel: UserViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (OnboardingActivity.shouldShowOnboarding(this)) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }
        
        setContent {
            TeacherSchedulerTheme {
                MainNavigationScreen(
                    onProfileClick = {
                        startActivity(Intent(this, EnhancedProfileActivity::class.java))
                    }
                )
            }
        }
    }
}
