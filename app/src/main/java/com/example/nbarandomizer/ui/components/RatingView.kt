package com.example.nbarandomizer.ui.components

import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.nbarandomizer.models.Rating
import com.example.nbarandomizer.ui.providers.CardOutlineProvider

class RatingView(
    private val card: CardView,
    private val value: TextView,
    private val name: TextView? = null
) {
    init {
        card.cardElevation = 5f
        card.preventCornerOverlap = false
        card.clipToOutline = false
        card.outlineProvider = CardOutlineProvider(card)
    }

    fun bind(rating: Rating) {
        value.text = rating.value.toString()
        name?.text = rating.name
        card.setCardBackgroundColor(rating.color)
        card.outlineSpotShadowColor = rating.color
    }
}
