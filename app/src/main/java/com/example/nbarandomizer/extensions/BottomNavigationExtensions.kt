package com.example.nbarandomizer.extensions

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.example.nbarandomizer.R

fun View.avoidBottomNavigationOverlap(action: View) {
    val bottomMargin = (action.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
    val navigationWidth = resources.getDimensionPixelSize(R.dimen.bottom_navigation_width)
    val navigationHeight = resources.getDimensionPixelSize(R.dimen.bottom_navigation_height)
    val gap = resources.getDimensionPixelSize(R.dimen.bottom_navigation_action_gap)
    var systemBottomInset = 0
    val handler = Handler(Looper.getMainLooper())

    fun updateBottomMargin() {
        handler.post {
            val params = action.layoutParams as ViewGroup.MarginLayoutParams
            val availableWidth = width - paddingLeft - paddingRight
            val measuredNavigationWidth = rootView.findViewById<View>(R.id.bottom_navigation)
                ?.width?.takeIf { it > 0 } ?: navigationWidth
            val spaceBesideNavigation = (availableWidth - measuredNavigationWidth) / 2
            val requiredSpace = action.width + params.marginEnd + gap
            val margin = systemBottomInset + if (spaceBesideNavigation < requiredSpace) {
                bottomMargin + navigationHeight + gap
            } else {
                bottomMargin
            }

            if (params.bottomMargin != margin) {
                action.updateLayoutParams<ViewGroup.MarginLayoutParams> { this.bottomMargin = margin }
            }
        }
    }

    addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateBottomMargin() }
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        systemBottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
        updateBottomMargin()
        insets
    }
    ViewCompat.requestApplyInsets(this)
}
