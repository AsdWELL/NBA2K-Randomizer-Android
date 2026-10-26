package com.example.nbarandomizer.ui.components

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RenderEffect
import android.graphics.RenderNode
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import com.example.nbarandomizer.R
import com.google.android.material.color.MaterialColors
import kotlin.math.ceil

internal class BackdropBlurController(private val host: View) :
    View.OnAttachStateChangeListener, ViewTreeObserver.OnPreDrawListener {
    private var source: View? = null
    private var excludeHost = false
    private var renderer: BlurRenderer? = null
    private var observedTree: ViewTreeObserver? = null
    private val visibleBounds = Rect()
    private val sourceLocation = IntArray(2)
    private val hostLocation = IntArray(2)

    init {
        host.addOnAttachStateChangeListener(this)
        if (host.isAttachedToWindow) observe()
    }

    fun setSource(source: View, excludeHost: Boolean = false) {
        this.source = source
        this.excludeHost = excludeHost
        host.invalidate()
    }

    private fun observe() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            observedTree = host.viewTreeObserver.also { it.addOnPreDrawListener(this) }
        }
    }

    override fun onViewAttachedToWindow(view: View) = observe()

    override fun onViewDetachedFromWindow(view: View) {
        observedTree?.takeIf { it.isAlive }?.removeOnPreDrawListener(this)
        observedTree = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            renderer?.release()
            renderer = null
        }
    }

    override fun onPreDraw(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) updateBackdrop()
        return true
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun renderer(): BlurRenderer = renderer ?: BlurRenderer(
        host.resources.getDimension(R.dimen.bottom_navigation_blur_radius),
        MaterialColors.getColor(host, android.R.attr.colorBackground)
    ).also { renderer = it }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun updateBackdrop() {
        val source = source ?: return
        if (!host.isHardwareAccelerated || !host.isShown || !source.isShown ||
            host.width == 0 || host.height == 0 || !host.getGlobalVisibleRect(visibleBounds)) return
        source.getLocationInWindow(sourceLocation)
        host.getLocationInWindow(hostLocation)
        // Update the referenced node in this frame without scheduling another redraw.
        renderer().record(source, host.width, host.height,
            hostLocation[0] - sourceLocation[0], hostLocation[1] - sourceLocation[1],
            if (excludeHost) host else null)
    }

    fun draw(canvas: Canvas) {
        if (source != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && canvas.isHardwareAccelerated) {
            // Retain the node even if an ancestor records this host before its first pre-draw.
            renderer().draw(canvas)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private class BlurRenderer(radius: Float, private val backgroundColor: Int) {
        private val padding = ceil(radius * 3f).toInt()
        private val node = RenderNode("Backdrop blur").apply {
            setRenderEffect(RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP))
        }

        fun record(source: View, width: Int, height: Int, offsetX: Int, offsetY: Int, excluded: View?) {
            node.setPosition(0, 0, width + padding * 2, height + padding * 2)
            val canvas = node.beginRecording()
            val visibility = excluded?.visibility
            try {
                // Exclude this control when the source is its ancestor, without invalidating
                // layout or capturing the control recursively into its own background.
                excluded?.setTransitionVisibility(View.INVISIBLE)
                canvas.drawColor(backgroundColor)
                canvas.translate((padding - offsetX).toFloat(), (padding - offsetY).toFloat())
                source.draw(canvas)
            } finally {
                if (visibility != null) excluded?.setTransitionVisibility(visibility)
                node.endRecording()
            }
        }

        fun draw(canvas: Canvas) {
            val saveCount = canvas.save()
            canvas.translate(-padding.toFloat(), -padding.toFloat())
            canvas.drawRenderNode(node)
            canvas.restoreToCount(saveCount)
        }

        fun release() = node.discardDisplayList()
    }
}
