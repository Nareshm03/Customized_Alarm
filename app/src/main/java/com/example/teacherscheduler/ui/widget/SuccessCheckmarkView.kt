package com.example.teacherscheduler.ui.widget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.example.teacherscheduler.R

/**
 * Animated success checkmark view
 */
class SuccessCheckmarkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.class_color_primary)
        style = Paint.Style.STROKE
        strokeWidth = 8f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.class_color_secondary)
        style = Paint.Style.FILL
    }

    private val checkPath = Path()
    private val circlePath = Path()
    private var progress = 0f
    private var animator: ValueAnimator? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setupPaths()
    }

    private fun setupPaths() {
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = Math.min(width, height) / 2f * 0.8f

        // Circle path
        circlePath.reset()
        circlePath.addCircle(centerX, centerY, radius, Path.Direction.CW)

        // Checkmark path
        checkPath.reset()
        val checkStartX = centerX - radius * 0.4f
        val checkStartY = centerY
        val checkMiddleX = centerX - radius * 0.1f
        val checkMiddleY = centerY + radius * 0.3f
        val checkEndX = centerX + radius * 0.5f
        val checkEndY = centerY - radius * 0.4f

        checkPath.moveTo(checkStartX, checkStartY)
        checkPath.lineTo(checkMiddleX, checkMiddleY)
        checkPath.lineTo(checkEndX, checkEndY)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw circle
        canvas.drawPath(circlePath, circlePaint)

        // Draw checkmark with progress
        if (progress > 0) {
            val pathMeasure = PathMeasure(checkPath, false)
            val length = pathMeasure.length
            val drawPath = Path()
            pathMeasure.getSegment(0f, length * progress, drawPath, true)
            canvas.drawPath(drawPath, paint)
        }
    }

    /**
     * Animate the checkmark
     */
    fun animateCheckmark(duration: Long = 600, onEnd: (() -> Unit)? = null) {
        animator?.cancel()

        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                progress = animation.animatedValue as Float
                invalidate()
            }
            start()
        }

        // Add delay then callback
        postDelayed({
            onEnd?.invoke()
        }, duration + 500)
    }

    /**
     * Reset the checkmark
     */
    fun reset() {
        animator?.cancel()
        progress = 0f
        invalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}

