package com.example.nbarandomizer.ui.components

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.graphics.ColorUtils
import com.example.nbarandomizer.R
import com.google.android.material.color.MaterialColors
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class DropNavigationIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var onPositionChanged: ((Float) -> Unit)? = null

    private data class Shape(
        val left: Float,
        val scaleX: Float,
        val scaleY: Float,
        val taper: Float,
        val direction: Float
    )

    private val highlightOpacity = 0.75f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = MaterialColors.getColor(this@DropNavigationIndicatorView,
            com.google.android.material.R.attr.colorSecondaryContainer)
        // Paint opacity avoids a View alpha layer that would crop the outer glow.
        alpha = (255 * highlightOpacity).toInt()
    }
    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        val accent = MaterialColors.getColor(this@DropNavigationIndicatorView,
            com.google.android.material.R.attr.colorPrimary)
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density
        color = ColorUtils.setAlphaComponent(accent, (110 * highlightOpacity).toInt())
        setShadowLayer(resources.getDimension(R.dimen.bottom_navigation_icon_glow_radius),
            0f, 0f, ColorUtils.setAlphaComponent(accent, (180 * highlightOpacity).toInt()))
    }
    private val path = Path()
    private var animator: ValueAnimator? = null
    private var positioned = false
    private var targetLeft = Float.NaN
    private var taper = 0f
    private var direction = 1f
    private var dragOrigin = 0f
    private var dragShape: Shape? = null

    fun snapTo(left: Float) {
        stopAnimation()
        dragShape = null
        setRestingShape(left)
    }

    fun animateTo(left: Float) {
        if (!positioned || !ValueAnimator.areAnimatorsEnabled()) {
            snapTo(left)
            return
        }
        if (animator?.isStarted == true && abs(targetLeft - left) < 0.001f) return
        if (abs(x - left) < 0.001f && abs(scaleX - 1f) < 0.001f &&
            abs(scaleY - 1f) < 0.001f && taper < 0.001f) {
            snapTo(left)
            return
        }

        val from = snapshot()
        stopAnimation()
        dragShape = null
        targetLeft = left
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 360L
            interpolator = LinearInterpolator()
            addUpdateListener { applyDrop(from, left, it.animatedValue as Float) }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (animator === animation) animator = null
                }
            })
            start()
        }
    }

    fun beginDrag(pageLeft: Float) {
        if (!positioned) setRestingShape(pageLeft)
        stopAnimation()
        dragOrigin = pageLeft
        dragShape = snapshot()
    }

    fun followDrag(pageLeft: Float, start: Float, end: Float) {
        if (!ValueAnimator.areAnimatorsEnabled()) {
            snapTo(pageLeft)
            return
        }
        if (dragShape == null) beginDrag(pageLeft)
        val from = dragShape ?: return
        val delta = pageLeft - dragOrigin
        val destination = if (delta * (end - start) >= 0f) end else start
        val distance = abs(destination - dragOrigin)
        val progress = if (distance < 0.001f) 0f else (abs(delta) / distance).coerceIn(0f, 1f)
        applyDrop(from, destination, progress)
    }

    private fun snapshot() = Shape(x, scaleX, scaleY, taper, direction)

    private fun stopAnimation() {
        val previous = animator
        animator = null
        previous?.cancel()
    }

    private fun setRestingShape(left: Float) {
        x = left
        scaleX = 1f
        scaleY = 1f
        taper = 0f
        targetLeft = left
        positioned = true
        visibility = VISIBLE
        onPositionChanged?.invoke(left)
        invalidate()
    }

    private fun applyDrop(from: Shape, destination: Float, progress: Float) {
        if (width == 0 || height == 0) return
        if (progress >= 1f) {
            setRestingShape(destination)
            return
        }

        val head = mix(from.left, destination, easeOut(progress))
        val tail = mix(from.left, destination, easeInOut(progress))
        val moving = abs(destination - from.left) > 0.5f
        val wave = if (moving) sin(PI * progress).toFloat() else 0f
        val blend = easeOut((progress * 4f).coerceAtMost(1f))
        val density = resources.displayMetrics.density
        val dropWidth = width + abs(head - tail) * 0.65f + wave * 12f * density
        val dropHeight = height - wave * 14f * density
        val nextDirection = if (destination >= from.left) 1f else -1f

        x = (head + tail) / 2f
        scaleX = mix(from.scaleX, dropWidth / width, blend)
        scaleY = mix(from.scaleY, dropHeight / height, blend)
        taper = mix(from.taper, wave, blend)
        direction = mix(from.direction, nextDirection, blend)
        visibility = VISIBLE
        onPositionChanged?.invoke(x)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val rx = width / 2f
        val ry = height / 2f
        val left = ry * (1f - taper * 0.48f * (1f + direction) / 2f)
        val right = ry * (1f - taper * 0.48f * (1f - direction) / 2f)
        val k = 0.55228475f

        path.rewind()
        path.moveTo(0f, ry)
        path.cubicTo(0f, ry - k * left, rx - k * rx, 0f, rx, 0f)
        path.cubicTo(rx + k * rx, 0f, width.toFloat(), ry - k * right, width.toFloat(), ry)
        path.cubicTo(width.toFloat(), ry + k * right, rx + k * rx, height.toFloat(), rx, height.toFloat())
        path.cubicTo(rx - k * rx, height.toFloat(), 0f, ry + k * left, 0f, ry)
        path.close()
        canvas.drawPath(path, paint)
        canvas.drawPath(path, glowPaint)
    }

    override fun onDetachedFromWindow() {
        stopAnimation()
        dragShape = null
        super.onDetachedFromWindow()
    }

    private fun mix(start: Float, end: Float, progress: Float) = start + (end - start) * progress

    private fun easeOut(progress: Float): Float {
        val remaining = 1f - progress
        return 1f - remaining * remaining * remaining
    }

    private fun easeInOut(progress: Float): Float = if (progress < 0.5f) {
        4f * progress * progress * progress
    } else {
        val remaining = 1f - progress
        1f - 4f * remaining * remaining * remaining
    }
}
