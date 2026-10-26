package com.example.nbarandomizer.extensions

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.example.nbarandomizer.R

fun ImageView.addNavigationIconGlow(): Drawable {
    val icon = drawable
    val bitmap = icon.toBitmap()
    val offset = IntArray(2)
    val paint = Paint().apply {
        maskFilter = BlurMaskFilter(
            resources.getDimension(R.dimen.bottom_navigation_icon_glow_radius),
            BlurMaskFilter.Blur.NORMAL
        )
    }
    val mask = bitmap.extractAlpha(paint, offset).apply {
        density = resources.displayMetrics.densityDpi
    }
    val layers = LayerDrawable(arrayOf(BitmapDrawable(resources, mask), icon)).apply {
        // Negative insets leave room for the halo without changing the icon's size.
        setLayerInset(0, offset[0], offset[1],
            bitmap.width - mask.width - offset[0], bitmap.height - mask.height - offset[1])
    }.mutate() as LayerDrawable
    setImageDrawable(layers)
    return layers.getDrawable(0).apply { alpha = 0 }
}
