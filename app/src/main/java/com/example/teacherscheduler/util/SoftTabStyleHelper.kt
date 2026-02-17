package com.example.teacherscheduler.util

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import androidx.core.content.ContextCompat
import com.example.teacherscheduler.R
import com.google.android.material.tabs.TabLayout

/**
 * Helper class to apply soft UI styling to TabLayout items
 */
object SoftTabStyleHelper {

    /**
     * Apply soft pill background to selected tab
     */
    fun applyPillBackground(tab: TabLayout.Tab, isSelected: Boolean, context: Context) {
        val view = tab.view

        if (isSelected) {
            // Create soft lavender pill background
            val pillBackground = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = dpToPx(context, 28f)
                setColor(ContextCompat.getColor(context, R.color.soft_nav_selected_bg))
            }

            // Apply padding for pill effect
            view.setPadding(
                dpToPx(context, 12f).toInt(),
                dpToPx(context, 8f).toInt(),
                dpToPx(context, 12f).toInt(),
                dpToPx(context, 8f).toInt()
            )

            view.background = pillBackground
        } else {
            // Remove background for unselected tabs
            view.background = null
            view.setPadding(
                dpToPx(context, 8f).toInt(),
                dpToPx(context, 8f).toInt(),
                dpToPx(context, 8f).toInt(),
                dpToPx(context, 8f).toInt()
            )
        }
    }

    /**
     * Setup TabLayout with soft UI styling
     */
    fun setupSoftTabLayout(tabLayout: TabLayout, context: Context) {
        // Add selection listener to apply pill backgrounds
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    applyPillBackground(it, true, context)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
                    applyPillBackground(it, false, context)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Do nothing
            }
        })

        // Apply initial styling
        val selectedTab = tabLayout.getTabAt(tabLayout.selectedTabPosition)
        selectedTab?.let {
            applyPillBackground(it, true, context)
        }
    }

    private fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }
}


