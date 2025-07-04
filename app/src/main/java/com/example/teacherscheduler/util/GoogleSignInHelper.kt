package com.example.teacherscheduler.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.teacherscheduler.R
import com.google.firebase.auth.FirebaseAuth
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Helper class for diagnosing and fixing Google Sign-In issues
 */
object GoogleSignInHelper {
    private const val TAG = "GoogleSignInHelper"
    
    /**
     * Diagnoses common Google Sign-In issues
     * @return A list of potential issues and solutions
     */
    fun diagnoseIssues(context: Context): List<String> {
        val issues = mutableListOf<String>()
        
        try {
            // Check if Firebase Auth is initialized
            val auth = FirebaseAuth.getInstance()
            // Firebase Auth is always initialized, but check if current user exists for context
            Log.d(TAG, "Firebase Auth instance available, current user: ${auth.currentUser?.email ?: "none"}")
            
            // Check if web client ID exists
            try {
                val webClientId = context.getString(R.string.default_web_client_id)
                if (webClientId.isBlank() || webClientId == "YOUR_WEB_CLIENT_ID") {
                    issues.add("Web Client ID is missing or invalid")
                }
            } catch (e: Exception) {
                issues.add("Could not find default_web_client_id resource: ${e.message}")
            }
            
            // Check package name
            val packageName = context.packageName
            Log.d(TAG, "App package name: $packageName")
            
            // Get app's SHA-1 fingerprint
            val sha1 = getSHA1Fingerprint(context)
            Log.d(TAG, "App SHA-1 fingerprint: $sha1")
            
        } catch (e: Exception) {
            issues.add("Error during diagnosis: ${e.message}")
            Log.e(TAG, "Error during diagnosis", e)
        }
        
        // If no specific issues found, add general advice
        if (issues.isEmpty()) {
            issues.add("No specific issues detected. Ensure you've added your app's SHA-1 fingerprint to Firebase console.")
            issues.add("Make sure Google Sign-In is enabled in Firebase Authentication.")
            issues.add("Configure OAuth consent screen in Google Cloud Console.")
            issues.add("Download and replace the latest google-services.json file.")
        }
        
        return issues
    }
    
    /**
     * Gets the app's SHA-1 fingerprint
     */
    fun getSHA1Fingerprint(context: Context): String {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }
            
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners ?: arrayOf()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures ?: arrayOf()
            }
            
            for (signature in signatures) {
                val md = MessageDigest.getInstance("SHA1")
                md.update(signature.toByteArray())
                val bytes = md.digest()
                
                // Convert to hex string
                val sb = StringBuilder()
                for (b in bytes) {
                    sb.append(String.format("%02X:", b))
                }
                
                // Remove the last colon
                if (sb.isNotEmpty()) {
                    sb.setLength(sb.length - 1)
                }
                
                return sb.toString()
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "No such algorithm", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting SHA1 fingerprint", e)
        }
        
        return "Could not determine SHA-1 fingerprint"
    }
    
    /**
     * Opens Firebase console in a browser
     */
    fun openFirebaseConsole(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://console.firebase.google.com/"))
        context.startActivity(intent)
    }
    
    /**
     * Opens Google Cloud Console in a browser
     */
    fun openGoogleCloudConsole(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://console.cloud.google.com/apis/credentials"))
        context.startActivity(intent)
    }
}
