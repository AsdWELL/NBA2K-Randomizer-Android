package com.example.nbarandomizer.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout

class BlurredNavigationLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val backdrop = BackdropBlurController(this)
    private val backdropClip = Path()

    fun setBlurSource(source: View) = backdrop.setSource(source)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        backdropClip.rewind()
        backdropClip.addRoundRect(0f, 0f, w.toFloat(), h.toFloat(), h / 2f, h / 2f,
            Path.Direction.CW)
    }

    override fun draw(canvas: Canvas) {
        val saveCount = canvas.save()
        // Clip only the backdrop; child glows may extend outside the capsule.
        canvas.clipPath(backdropClip)
        backdrop.draw(canvas)
        canvas.restoreToCount(saveCount)
        super.draw(canvas)
    }
}
