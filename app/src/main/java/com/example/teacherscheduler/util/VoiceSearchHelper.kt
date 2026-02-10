package com.example.teacherscheduler.util

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import java.util.*

/**
 * Voice search helper for speech-to-text functionality
 */
class VoiceSearchHelper(private val activity: Activity) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var onResultListener: ((String) -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null

    init {
        if (!SpeechRecognizer.isRecognitionAvailable(activity)) {
            Toast.makeText(activity, "Speech recognition not available", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Start voice recognition
     */
    fun startListening(
        onResult: (String) -> Unit,
        onError: ((String) -> Unit)? = null
    ) {
        this.onResultListener = onResult
        this.onErrorListener = onError

        // Haptic feedback when starting
        HapticFeedbackUtil.lightFeedback(activity)

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity).apply {
            setRecognitionListener(recognitionListener)
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.startListening(intent)
    }

    /**
     * Stop voice recognition
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    /**
     * Clean up resources
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            // Ready to listen
        }

        override fun onBeginningOfSpeech() {
            // User started speaking
            HapticFeedbackUtil.lightFeedback(activity)
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Volume changed
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Buffer received
        }

        override fun onEndOfSpeech() {
            // User stopped speaking
            HapticFeedbackUtil.lightFeedback(activity)
        }

        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                else -> "Unknown error"
            }

            HapticFeedbackUtil.errorFeedback(activity)
            onErrorListener?.invoke(errorMessage)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val recognizedText = matches[0]
                HapticFeedbackUtil.successFeedback(activity)
                onResultListener?.invoke(recognizedText)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            // Partial results available
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            // Event occurred
        }
    }

    companion object {
        const val REQUEST_CODE_SPEECH_INPUT = 1001

        /**
         * Parse voice command for scheduling
         */
        fun parseScheduleCommand(text: String): ScheduleCommand? {
            val lowerText = text.lowercase(Locale.getDefault())

            // Parse class scheduling
            if (lowerText.contains("add class") || lowerText.contains("schedule class")) {
                return ScheduleCommand.AddClass(extractSubject(lowerText))
            }

            // Parse meeting scheduling
            if (lowerText.contains("add meeting") || lowerText.contains("schedule meeting")) {
                return ScheduleCommand.AddMeeting(extractMeetingPerson(lowerText))
            }

            // Parse search
            if (lowerText.contains("search") || lowerText.contains("find")) {
                return ScheduleCommand.Search(extractSearchQuery(lowerText))
            }

            return null
        }

        private fun extractSubject(text: String): String {
            // Extract subject after "add class" or "schedule class"
            val patterns = listOf("add class ", "schedule class ")
            patterns.forEach { pattern ->
                val index = text.indexOf(pattern)
                if (index >= 0) {
                    return text.substring(index + pattern.length).trim()
                }
            }
            return ""
        }

        private fun extractMeetingPerson(text: String): String {
            // Extract person after "with"
            val withIndex = text.indexOf("with ")
            if (withIndex >= 0) {
                return text.substring(withIndex + 5).trim()
            }
            return ""
        }

        private fun extractSearchQuery(text: String): String {
            // Extract search query after "search" or "find"
            val patterns = listOf("search for ", "search ", "find ")
            patterns.forEach { pattern ->
                val index = text.indexOf(pattern)
                if (index >= 0) {
                    return text.substring(index + pattern.length).trim()
                }
            }
            return ""
        }
    }

    sealed class ScheduleCommand {
        data class AddClass(val subject: String) : ScheduleCommand()
        data class AddMeeting(val person: String) : ScheduleCommand()
        data class Search(val query: String) : ScheduleCommand()
    }
}

