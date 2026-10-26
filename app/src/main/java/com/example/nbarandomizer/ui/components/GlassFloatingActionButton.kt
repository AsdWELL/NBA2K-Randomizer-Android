package com.example.nbarandomizer.ui.components

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.ColorUtils
import com.example.nbarandomizer.R
import com.example.nbarandomizer.extensions.addNavigationIconGlow
import com.google.android.material.color.MaterialColors
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.shape.RelativeCornerSize
import kotlin.math.min

class GlassFloatingActionButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.floatingActionButtonStyle
) : FloatingActionButton(context, attrs, defStyleAttr) {
    private val backdrop = BackdropBlurController(this)
    private val circle = Path()
    private var iconGlow: Drawable? = null
    private val pressedGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        val accent = MaterialColors.getColor(this@GlassFloatingActionButton,
            com.google.android.material.R.attr.colorPrimary)
        style = Paint.Style.STROKE
        strokeWidth = resources.displayMetrics.density
        color = ColorUtils.setAlphaComponent(accent, 82)
        setShadowLayer(resources.getDimension(R.dimen.bottom_navigation_icon_glow_radius),
            0f, 0f, ColorUtils.setAlphaComponent(accent, 135))
    }

    init {
        shapeAppearanceModel = shapeAppearanceModel.toBuilder()
            .setAllCornerSizes(RelativeCornerSize(0.5f)).build()
        backgroundTintList = AppCompatResources.getColorStateList(context, R.color.bottom_navigation_background_tint)
        setRippleColor(Color.TRANSPARENT)
        if (drawable != null) iconGlow = addNavigationIconGlow()
    }

    fun setBlurSource(source: View) = backdrop.setSource(source, excludeHost = true)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        circle.rewind()
        circle.addCircle(w / 2f, h / 2f, min(w, h) / 2f, Path.Direction.CW)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        iconGlow?.alpha = if (isPressed && isEnabled) 180 else 0
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        val saveCount = canvas.save()
        canvas.clipPath(circle)
        backdrop.draw(canvas)
        canvas.restoreToCount(saveCount)
        super.draw(canvas)
        if (isPressed && isEnabled) canvas.drawPath(circle, pressedGlow)
    }
}
