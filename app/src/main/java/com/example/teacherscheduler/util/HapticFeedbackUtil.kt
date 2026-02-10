package com.example.teacherscheduler.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import androidx.core.content.ContextCompat

/**
 * Utility class for haptic feedback and animations
 */
object HapticFeedbackUtil {

    private const val LIGHT_VIBRATION_DURATION = 20L
    private const val MEDIUM_VIBRATION_DURATION = 40L
    private const val HEAVY_VIBRATION_DURATION = 60L

    /**
     * Provides light haptic feedback
     */
    fun lightFeedback(context: Context) {
        performHapticFeedback(context, LIGHT_VIBRATION_DURATION, VibrationEffect.DEFAULT_AMPLITUDE)
    }

    /**
     * Provides medium haptic feedback
     */
    fun mediumFeedback(context: Context) {
        performHapticFeedback(context, MEDIUM_VIBRATION_DURATION, 128)
    }

    /**
     * Provides heavy haptic feedback
     */
    fun heavyFeedback(context: Context) {
        performHapticFeedback(context, HEAVY_VIBRATION_DURATION, 255)
    }

    /**
     * Provides success haptic feedback (two quick pulses)
     */
    fun successFeedback(context: Context) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 50, 50, 50)
            val amplitudes = intArrayOf(0, 100, 0, 100)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 50, 50, 50), -1)
        }
    }

    /**
     * Provides error haptic feedback (three quick pulses)
     */
    fun errorFeedback(context: Context) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 30, 30, 30, 30, 30)
            val amplitudes = intArrayOf(0, 150, 0, 150, 0, 150)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 30, 30, 30, 30, 30), -1)
        }
    }

    private fun performHapticFeedback(context: Context, duration: Long, amplitude: Int) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(duration, amplitude))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = ContextCompat.getSystemService(context, VibratorManager::class.java)
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            ContextCompat.getSystemService(context, Vibrator::class.java)
        }
    }
}

/**
 * Animation utilities
 */
object AnimationUtil {

    /**
     * Scale down button press animation
     */
    fun scaleDownButton(view: View, onAnimationEnd: (() -> Unit)? = null) {
        view.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .withEndAction { onAnimationEnd?.invoke() }
                    .start()
            }
            .start()
    }

    /**
     * Pulse animation for success
     */
    fun pulseAnimation(view: View) {
        view.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(150)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .start()
            }
            .start()
    }

    /**
     * Shake animation for error
     */
    fun shakeAnimation(view: View) {
        view.animate()
            .translationX(-25f)
            .setDuration(50)
            .withEndAction {
                view.animate()
                    .translationX(25f)
                    .setDuration(50)
                    .withEndAction {
                        view.animate()
                            .translationX(-25f)
                            .setDuration(50)
                            .withEndAction {
                                view.animate()
                                    .translationX(0f)
                                    .setDuration(50)
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }

    /**
     * Fade in animation
     */
    fun fadeIn(view: View, duration: Long = 300) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .start()
    }

    /**
     * Fade out animation
     */
    fun fadeOut(view: View, duration: Long = 300, onEnd: (() -> Unit)? = null) {
        view.animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction {
                view.visibility = View.GONE
                onEnd?.invoke()
            }
            .start()
    }

    /**
     * Slide up animation
     */
    fun slideUp(view: View, duration: Long = 300) {
        view.translationY = view.height.toFloat()
        view.visibility = View.VISIBLE
        view.animate()
            .translationY(0f)
            .setDuration(duration)
            .start()
    }

    /**
     * Slide down animation
     */
    fun slideDown(view: View, duration: Long = 300, onEnd: (() -> Unit)? = null) {
        view.animate()
            .translationY(view.height.toFloat())
            .setDuration(duration)
            .withEndAction {
                view.visibility = View.GONE
                onEnd?.invoke()
            }
            .start()
    }

    /**
     * Success checkmark animation
     */
    fun showSuccessCheckmark(view: View, context: Context) {
        view.alpha = 0f
        view.scaleX = 0f
        view.scaleY = 0f
        view.visibility = View.VISIBLE

        view.animate()
            .alpha(1f)
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(200)
            .withEndAction {
                HapticFeedbackUtil.successFeedback(context)
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .withEndAction {
                        // Keep visible for 1 second then fade out
                        view.postDelayed({
                            fadeOut(view, 300)
                        }, 1000)
                    }
                    .start()
            }
            .start()
    }
}

/**
 * Extension function for easy button press animation with haptic feedback
 */
fun View.setOnClickWithAnimation(onClick: () -> Unit) {
    this.setOnClickListener {
        HapticFeedbackUtil.lightFeedback(context)
        AnimationUtil.scaleDownButton(this) {
            onClick()
        }
    }
}

