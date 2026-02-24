package com.example.teacherscheduler.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.*
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.teacherscheduler.MainActivity
import com.example.teacherscheduler.R
import com.example.teacherscheduler.firebase.FirebaseService
import com.example.teacherscheduler.ui.compose.LoginScreen
import com.example.teacherscheduler.ui.compose.RegisterScreen
import com.example.teacherscheduler.ui.theme.TeacherSchedulerTheme
import com.example.teacherscheduler.viewmodel.UserViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager
    private val userViewModel: UserViewModel by viewModels()
    
    @Inject
    lateinit var firebaseService: FirebaseService

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        if (auth.currentUser != null) {
            fetchRoleAndNavigate()
            return
        }

        setContent {
            TeacherSchedulerTheme {
                var showRegister by remember { mutableStateOf(false) }

                if (showRegister) {
                    RegisterScreen(
                        onRegisterSuccess = { email, password ->
                            registerWithEmail(email, password)
                        },
                        onBackToLogin = {
                            showRegister = false
                        }
                    )
                } else {
                    LoginScreen(
                        onLoginSuccess = { email, password ->
                            loginWithEmail(email, password)
                        },
                        onRegisterClick = {
                            showRegister = true
                        },
                        onGoogleSignIn = {
                            signInWithGoogle()
                        }
                    )
                }
            }
        }
    }

    private fun signInWithGoogle() {
        lifecycleScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(R.string.default_web_client_id))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = this@LoginActivity
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdToken = GoogleIdTokenCredential
                        .createFrom(credential.data)
                        .idToken
                    firebaseAuthWithGoogle(googleIdToken)
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Unexpected credential type",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Google Sign-In failed", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Google sign-in failed: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(TAG, "Google ID token parsing failed", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Sign-in error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Check if user profile exists, if not create it
                    lifecycleScope.launch {
                        val profile = firebaseService.getUserProfile()
                        if (profile == null) {
                            val user = auth.currentUser
                            firebaseService.saveUserProfile(
                                name = user?.displayName ?: "Google User",
                                email = user?.email ?: "",
                                role = "teacher",
                                department = "General"
                            )
                        }
                        fetchRoleAndNavigate()
                    }
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchRoleAndNavigate() {
        lifecycleScope.launch {
            userViewModel.fetchRoleFromFirestore()
            startMainActivity()
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    fetchRoleAndNavigate()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun registerWithEmail(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    lifecycleScope.launch {
                        // Create basic profile so Firestore rules pass
                        val success = firebaseService.saveUserProfile(
                            name = email.substringBefore("@"),
                            email = email,
                            role = "teacher",
                            department = "General"
                        )
                        if (success) {
                            Toast.makeText(this@LoginActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                            fetchRoleAndNavigate()
                        } else {
                            // Even if profile fails, we have the auth account
                            fetchRoleAndNavigate()
                        }
                    }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
