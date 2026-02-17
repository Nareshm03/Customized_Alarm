package com.example.teacherscheduler.ui.components

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.example.teacherscheduler.R
import android.graphics.Color

class PremiumPrimaryButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialButton(context, attrs, defStyleAttr) {

    private val normalColor = Color.parseColor("#E8CFC1")
    private val pressedColor = Color.parseColor("#D4B8A8")
    private val textColor = Color.parseColor("#2B2B2B")

    init {
        setupButton()
    }

    private fun setupButton() {
        val gradient = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(normalColor)
            cornerRadius = 88f // 22dp * 4
        }
        
        background = gradient
        elevation = 16f
        translationZ = 8f
        
        setTextColor(textColor)
        textSize = 16f
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                animatePress(true)
                (background as? GradientDrawable)?.setColor(pressedColor)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                animatePress(false)
                (background as? GradientDrawable)?.setColor(normalColor)
            }
        }
        return super.onTouchEvent(event)
    }

    private fun animatePress(pressed: Boolean) {
        val scale = if (pressed) 0.95f else 1.0f
        val elevation = if (pressed) 8f else 16f
        
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(this@PremiumPrimaryButton, "scaleX", scale),
                ObjectAnimator.ofFloat(this@PremiumPrimaryButton, "scaleY", scale),
                ObjectAnimator.ofFloat(this@PremiumPrimaryButton, "elevation", elevation)
            )
            duration = 150
            start()
        }
    }
}