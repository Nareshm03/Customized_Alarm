package com.example.teacherscheduler.util

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.teacherscheduler.R

/**
 * Extension functions for SwipeRefreshLayout with animations and haptic feedback
 */

/**
 * Setup SwipeRefreshLayout with custom colors and haptic feedback
 */
fun SwipeRefreshLayout.setupWithHaptic(onRefresh: () -> Unit) {
    setColorSchemeResources(
        R.color.colorPrimary,
        R.color.class_color_primary,
        R.color.meeting_color_primary,
        R.color.teal_500
    )

    setProgressBackgroundColorSchemeResource(R.color.surface_light)

    setOnRefreshListener {
        HapticFeedbackUtil.lightFeedback(context)
        onRefresh()
    }
}

/**
 * Complete refresh with success animation
 */
fun SwipeRefreshLayout.completeRefreshWithSuccess(onComplete: (() -> Unit)? = null) {
    // Haptic feedback on success
    HapticFeedbackUtil.successFeedback(context)

    // Keep showing for a brief moment then hide
    postDelayed({
        isRefreshing = false
        onComplete?.invoke()
    }, 500)
}

/**
 * Complete refresh with error
 */
fun SwipeRefreshLayout.completeRefreshWithError(onComplete: (() -> Unit)? = null) {
    // Haptic feedback on error
    HapticFeedbackUtil.errorFeedback(context)

    isRefreshing = false
    onComplete?.invoke()
}

/**
 * Start refresh programmatically
 */
fun SwipeRefreshLayout.startRefreshWithAnimation() {
    isRefreshing = true
    HapticFeedbackUtil.lightFeedback(context)
}

