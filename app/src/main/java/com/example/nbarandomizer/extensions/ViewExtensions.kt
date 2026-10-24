package com.example.nbarandomizer.extensions

import android.view.View
import android.view.ViewGroup

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.findByTransitionName(name: String, excluded: View? = null): View? {
    if (this === excluded) return null
    if (transitionName == name) return this
    if (this is ViewGroup) {
        for (index in 0 until childCount) {
            getChildAt(index).findByTransitionName(name, excluded)?.let { return it }
        }
    }
    return null
}
