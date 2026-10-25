package com.example.nbarandomizer.extensions

import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

fun EditText.submitOnEnter(imeAction: Int, onSubmit: () -> Unit = {}) {
    setOnEditorActionListener { _, actionId, event ->
        val isEnter = event?.keyCode == KeyEvent.KEYCODE_ENTER ||
            event?.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER
        if (actionId != imeAction && !isEnter) {
            false
        } else {
            if (!isEnter || (event?.action == KeyEvent.ACTION_UP && event.isCanceled.not())) {
                onSubmit()
                clearFocus()
                val keyboard = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                keyboard?.hideSoftInputFromWindow(windowToken, 0)
            }
            true
        }
    }
}

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
