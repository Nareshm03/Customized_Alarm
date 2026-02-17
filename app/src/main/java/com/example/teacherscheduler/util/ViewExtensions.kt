package com.example.teacherscheduler.util

import android.view.View
import android.view.animation.DecelerateInterpolator

fun View.fadeIn(duration: Long = 300) {
    alpha = 0f
    visibility = View.VISIBLE
    animate().alpha(1f).setDuration(duration).setInterpolator(DecelerateInterpolator()).start()
}

fun View.slideUpFadeIn(delay: Long = 0) {
    alpha = 0f
    translationY = 30f
    visibility = View.VISIBLE
    animate().alpha(1f).translationY(0f).setStartDelay(delay).setDuration(350).setInterpolator(DecelerateInterpolator()).start()
}

fun View.scalePress() {
    animate().scaleX(0.97f).scaleY(0.97f).setDuration(100).withEndAction {
        animate().scaleX(1f).scaleY(1f).setDuration(150).start()
    }.start()
}
