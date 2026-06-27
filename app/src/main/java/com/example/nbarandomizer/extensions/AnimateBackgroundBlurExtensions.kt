package com.example.nbarandomizer.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.example.nbarandomizer.R

fun Fragment.makeBlurBackground() {
    val startBlurRadius = 1f
    val endBlurRadius = 25f
    val blurDuration = 350L
    val backgroundView = requireActivity().findViewById<View>(R.id.blur_container)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ValueAnimator.ofFloat(startBlurRadius, endBlurRadius).apply {
            duration = blurDuration
            addUpdateListener { animator ->
                val radius = animator.animatedValue as Float
                backgroundView.setRenderEffect(
                    RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
                )
            }
            start()
        }
    }

    requireActivity().onBackPressedDispatcher.addCallback(
        viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val topFragment = parentFragmentManager.fragments.lastOrNull { it.isVisible }

                if (topFragment != this@makeBlurBackground) {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                    return
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ValueAnimator.ofFloat(endBlurRadius, startBlurRadius).apply {
                        duration = blurDuration
                        addUpdateListener { animator ->
                            val radius = animator.animatedValue as Float
                            backgroundView?.setRenderEffect(
                                RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP)
                            )
                        }
                        addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                backgroundView?.setRenderEffect(null)
                            }
                        })
                        start()
                    }
                }

                isEnabled = false

                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    )
}