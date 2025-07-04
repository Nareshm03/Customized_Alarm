package com.example.teacherscheduler.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.teacherscheduler.R
import com.example.teacherscheduler.databinding.ActivitySignInBinding
import com.example.teacherscheduler.data.remote.FirebaseService
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseService: FirebaseService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseService = FirebaseService()

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.buttonSignIn.setOnClickListener {
            signIn()
        }

        binding.buttonSignUp.setOnClickListener {
            signUp()
        }
    }

    private fun signIn() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.buttonSignIn.isEnabled = false
        binding.buttonSignIn.text = getString(R.string.signing_in)

        lifecycleScope.launch {
            val success = firebaseService.signIn(email, password)

            if (success) {
                Toast.makeText(this@SignInActivity, "Sign in successful!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@SignInActivity, "Sign in failed. Please check your credentials.", Toast.LENGTH_SHORT).show()
            }

            binding.buttonSignIn.isEnabled = true
            binding.buttonSignIn.text = getString(R.string.sign_in)
        }
    }

    private fun signUp() {
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        binding.buttonSignUp.isEnabled = false
        binding.buttonSignUp.text = getString(R.string.creating_account)

        lifecycleScope.launch {
            val success = firebaseService.signUp(email, password)

            if (success) {
                Toast.makeText(this@SignInActivity, "Account created successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@SignInActivity, "Account creation failed. Please try again.", Toast.LENGTH_SHORT).show()
            }

            binding.buttonSignUp.isEnabled = true
            binding.buttonSignUp.text = getString(R.string.create_account)
        }
    }
}