package com.example.nbarandomizer.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.example.nbarandomizer.R
import com.google.android.material.color.MaterialColors
import kotlin.math.ceil

class BlurredNavigationLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var blurSource: View? = null
    private var renderer: BlurRenderer? = null
    private var observedTree: ViewTreeObserver? = null
    private val visibleBounds = Rect()
    private val sourceLocation = IntArray(2)
    private val navigationLocation = IntArray(2)
    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) updateBackdrop()
        true
    }

    init {
        clipToOutline = true
    }

    // The source excludes the navigation itself to avoid capturing it recursively.
    fun setBlurSource(source: View) {
        blurSource = source
        invalidate()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun updateBackdrop() {
        val source = blurSource ?: return
        if (!isHardwareAccelerated || !source.isShown || width == 0 || height == 0 ||
            !getGlobalVisibleRect(visibleBounds)) return

        val currentRenderer = renderer ?: BlurRenderer(
            resources.getDimension(R.dimen.bottom_navigation_blur_radius),
            MaterialColors.getColor(this, android.R.attr.colorBackground)
        ).also { renderer = it }
        source.getLocationInWindow(sourceLocation)
        getLocationInWindow(navigationLocation)
        // The display list references this node; updating it needs no invalidate/extra frame.
        currentRenderer.record(
            source, width, height,
            navigationLocation[0] - sourceLocation[0],
            navigationLocation[1] - sourceLocation[1]
        )
    }

    override fun draw(canvas: Canvas) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && canvas.isHardwareAccelerated) {
            renderer?.draw(canvas)
        }
        // Keep the translucent background, indicator and icons above the blurred content.
        super.draw(canvas)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            observedTree = viewTreeObserver.also { it.addOnPreDrawListener(preDrawListener) }
        }
    }

    override fun onDetachedFromWindow() {
        observedTree?.takeIf { it.isAlive }?.removeOnPreDrawListener(preDrawListener)
        observedTree = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            renderer?.release()
            renderer = null
        }
        super.onDetachedFromWindow()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private class BlurRenderer(radius: Float, private val backgroundColor: Int) {
        private val padding = ceil(radius * 3f).toInt()
        private val node = RenderNode("Navigation backdrop").apply {
            setRenderEffect(RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP))
        }

        fun record(source: View, width: Int, height: Int, offsetX: Int, offsetY: Int) {
            node.setPosition(0, 0, width + padding * 2, height + padding * 2)
            val canvas = node.beginRecording()
            try {
                canvas.drawColor(backgroundColor)
                canvas.translate((padding - offsetX).toFloat(), (padding - offsetY).toFloat())
                source.draw(canvas)
            } finally {
                node.endRecording()
            }
        }

        fun draw(canvas: Canvas) {
            val saveCount = canvas.save()
            canvas.translate(-padding.toFloat(), -padding.toFloat())
            canvas.drawRenderNode(node)
            canvas.restoreToCount(saveCount)
        }

        fun release() {
            node.discardDisplayList()
        }
    }
}
