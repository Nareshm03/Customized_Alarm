package com.example.teacherscheduler.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.teacherscheduler.MainActivity
import com.example.teacherscheduler.R
import com.example.teacherscheduler.ui.PermissionActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    
    private lateinit var auth: FirebaseAuth
    // Updated Google Sign-In approach for 2025
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        Log.d("Auth", "LoginActivity onCreate started")
        
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        Log.d("Auth", "Firebase Auth initialized successfully")
        
        // Initialize Credential Manager (2025 approach)
        credentialManager = CredentialManager.create(this)
        Log.d("Auth", "Credential Manager initialized successfully")
        
        // Check if user is already logged in
        if (auth.currentUser != null) {
            Log.d("Auth", "User already logged in: ${auth.currentUser!!.email}")
            startMainActivity()
            return
        }
        
        Log.d("Auth", "No user logged in, showing login screen")
        setupViews()
        setupAnimations()
    }

    private fun setupViews() {
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val googleSignInButton = findViewById<Button>(R.id.googleSignInButton)
        val testFirebaseButton = findViewById<Button>(R.id.testFirebaseButton)
        
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validate email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            Log.d("Auth", "Attempting login with: $email")
            
            // Show progress
            loginButton.isEnabled = false
            loginButton.text = "Signing In..."
            
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    // Re-enable button
                    loginButton.isEnabled = true
                    loginButton.text = "Login"
                    
                    if (task.isSuccessful) {
                        Log.d("Auth", "Login successful for: ${auth.currentUser?.email}")
                        Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                        startMainActivity()
                    } else {
                        val errorMessage = when (task.exception?.message) {
                            "There is no user record corresponding to this identifier. The user may have been deleted." -> 
                                "No account found with this email. Please register first."
                            "The password is invalid or the user does not have a password." -> 
                                "Incorrect password. Please try again."
                            "The email address is badly formatted." -> 
                                "Please enter a valid email address."
                            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> 
                                "Network error. Please check your internet connection."
                            else -> task.exception?.message ?: "Login failed. Please try again."
                        }
                        
                        Log.e("Auth", "Login failed: ${task.exception?.message}")
                        Log.e("Auth", "Exception type: ${task.exception?.javaClass?.simpleName}")
                        
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
        }
        
        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validate email format
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            Log.d("Auth", "Attempting registration with: $email")
            
            // Show progress
            registerButton.isEnabled = false
            registerButton.text = "Creating Account..."
            
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    // Re-enable button
                    registerButton.isEnabled = true
                    registerButton.text = "Register"
                    
                    if (task.isSuccessful) {
                        Log.d("Auth", "Registration successful for: ${auth.currentUser?.email}")
                        Toast.makeText(this, "Registration successful! Welcome!", Toast.LENGTH_SHORT).show()
                        startMainActivity()
                    } else {
                        val errorMessage = when (task.exception?.message) {
                            "The email address is already in use by another account." -> 
                                "This email is already registered. Try logging in instead."
                            "The email address is badly formatted." -> 
                                "Please enter a valid email address."
                            "The given password is invalid. [ Password should be at least 6 characters ]" -> 
                                "Password must be at least 6 characters long."
                            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." -> 
                                "Network error. Please check your internet connection."
                            else -> task.exception?.message ?: "Registration failed. Please try again."
                        }
                        
                        Log.e("Auth", "Registration failed: ${task.exception?.message}")
                        Log.e("Auth", "Exception type: ${task.exception?.javaClass?.simpleName}")
                        
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
        }
        
        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
        
        testFirebaseButton.setOnClickListener {
            // Simple Firebase connectivity test
            testFirebaseConnection()
        }
    }



    private fun signInWithGoogle() {
        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(getString(R.string.default_web_client_id)) // Get this from google-services.json
                .build()
            
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            
            // Log the client ID being used
            Log.d("Auth", "Using server client ID: ${getString(R.string.default_web_client_id)}")
            
            lifecycleScope.launch {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = this@LoginActivity,
                    )
                    handleSignIn(result)
                } catch (e: GetCredentialException) {
                    Log.e("Auth", "Google Sign-In failed: During begin sign in: ${e.message}")
                    
                    // Show a more helpful error message
                    val errorMessage = when {
                        e.message?.contains("Developer console is not set up correctly") == true -> 
                            "Google Sign-In is not properly configured. Please check Firebase setup."
                        e.message?.contains("network") == true -> 
                            "Network error. Please check your internet connection."
                        else -> "Google Sign-In failed: ${e.message}"
                    }
                    
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Log.e("Auth", "Unexpected error setting up Google Sign-In: ${e.message}")
            Toast.makeText(this@LoginActivity, "Error setting up Google Sign-In", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun handleSignIn(result: GetCredentialResponse) {
        try {
            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val googleIdToken = credential.idToken
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
            auth.signInWithCredential(firebaseCredential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("Auth", "Google sign-in successful")
                        Toast.makeText(this, "Google sign-in successful!", Toast.LENGTH_SHORT).show()
                        startMainActivity()
                    } else {
                        Log.e("Auth", "Firebase auth failed: ${task.exception?.message}")
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            Log.e("Auth", "Unexpected credential type: ${e.message}")
            Toast.makeText(this, "Authentication failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startMainActivity() {
        // First check if we need to request permissions
        if (shouldRequestPermissions()) {
            // Start PermissionActivity to handle permissions
            startActivity(Intent(this, PermissionActivity::class.java))
        } else {
            // Directly go to MainActivity if permissions are already granted
            startActivity(Intent(this, MainActivity::class.java))
        }
        finish()
    }
    
    private fun shouldRequestPermissions(): Boolean {
        // In this simplified version, we'll skip permission checks
        // and rely on the PermissionActivity to handle them
        return false
    }
    
    private fun testFirebaseConnection() {
        Log.d("Auth", "Testing Firebase connection...")
        Toast.makeText(this, "Testing Firebase connection...", Toast.LENGTH_SHORT).show()
        
        // Test with a unique email to avoid conflicts
        val testEmail = "test${System.currentTimeMillis()}@example.com"
        val testPassword = "test123456"
        
        Log.d("Auth", "Creating test user: $testEmail")
        
        auth.createUserWithEmailAndPassword(testEmail, testPassword)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Auth", "✅ Firebase Auth is working! Test user created successfully.")
                    Toast.makeText(this, "✅ Firebase Auth is working!", Toast.LENGTH_LONG).show()
                    
                    // Also check Google Sign-In configuration
                    checkGoogleSignInConfiguration()
                    
                    // Clean up - delete the test user
                    auth.currentUser?.delete()?.addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            Log.d("Auth", "Test user cleaned up successfully")
                        }
                    }
                } else {
                    Log.e("Auth", "❌ Firebase Auth test failed: ${task.exception?.message}")
                    Toast.makeText(this, "❌ Firebase Auth failed: ${task.exception?.message}", 
                        Toast.LENGTH_LONG).show()
                }
            }
    }
    
    private fun checkGoogleSignInConfiguration() {
        try {
            // Use our helper to diagnose issues
            val issues = com.example.teacherscheduler.util.GoogleSignInHelper.diagnoseIssues(this)
            
            // Get the web client ID
            val webClientId = getString(R.string.default_web_client_id)
            Log.d("Auth", "Web Client ID: $webClientId")
            
            // Check if the package name in google-services.json matches the app's package name
            val packageName = applicationContext.packageName
            Log.d("Auth", "App package name: $packageName")
            
            // Get SHA-1 fingerprint
            val sha1 = com.example.teacherscheduler.util.GoogleSignInHelper.getSHA1Fingerprint(this)
            
            // Build a detailed message
            val message = """
                Google Sign-In Configuration:
                - Web Client ID: ${webClientId.take(15)}...
                - Package Name: $packageName
                - SHA-1: ${sha1.take(20)}...
                
                Potential issues:
                ${issues.joinToString("\n")}
                
                To fix Google Sign-In:
                1. Add SHA-1 fingerprint to Firebase console
                2. Enable Google Sign-In in Firebase Authentication
                3. Configure OAuth consent screen in Google Cloud Console
                4. Download updated google-services.json
            """.trimIndent()
            
            Log.d("Auth", message)
            
            // Show a simplified message to the user
            val userMessage = if (issues.size > 1) {
                "Found ${issues.size} potential issues with Google Sign-In configuration. See logs for details."
            } else {
                "Google Sign-In configuration checked. See logs for details."
            }
            
            Toast.makeText(this, userMessage, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("Auth", "Error checking Google Sign-In configuration: ${e.message}")
            Toast.makeText(this, "Error checking Google Sign-In configuration: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupAnimations() {
        // Get views for animation
        val appLogo = findViewById<View>(R.id.appLogo)
        val appTitle = findViewById<View>(R.id.appTitle)
        val appSubtitle = findViewById<View>(R.id.appSubtitle)
        val loginCard = findViewById<CardView>(R.id.loginCard)
        
        // Set initial states
        appLogo.alpha = 0f
        appLogo.translationY = -100f
        appTitle.alpha = 0f
        appTitle.translationY = -50f
        appSubtitle.alpha = 0f
        appSubtitle.translationY = -30f
        loginCard.alpha = 0f
        loginCard.translationY = 100f
        
        // Create animations
        val logoAnimator = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(appLogo, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(appLogo, "translationY", -100f, 0f)
            )
            duration = 800
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        val titleAnimator = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(appTitle, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(appTitle, "translationY", -50f, 0f)
            )
            duration = 600
            startDelay = 200
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        val subtitleAnimator = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(appSubtitle, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(appSubtitle, "translationY", -30f, 0f)
            )
            duration = 600
            startDelay = 400
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        val cardAnimator = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(loginCard, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(loginCard, "translationY", 100f, 0f)
            )
            duration = 800
            startDelay = 600
            interpolator = AccelerateDecelerateInterpolator()
        }
        
        // Start animations
        logoAnimator.start()
        titleAnimator.start()
        subtitleAnimator.start()
        cardAnimator.start()
    }
}