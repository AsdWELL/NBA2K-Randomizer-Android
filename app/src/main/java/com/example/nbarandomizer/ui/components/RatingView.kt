package com.example.nbarandomizer.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.nbarandomizer.databinding.ViewRatingContentBinding
import com.example.nbarandomizer.models.Rating
import com.example.nbarandomizer.ui.providers.CardOutlineProvider

class RatingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.cardview.R.attr.cardViewStyle
) : CardView(context, attrs, defStyleAttr) {
    private val binding = ViewRatingContentBinding.inflate(LayoutInflater.from(context), this)

    init {
        cardElevation = 5f
        preventCornerOverlap = false
        clipToOutline = false
        outlineProvider = CardOutlineProvider(this)
    }

    fun bind(rating: Rating, name: TextView? = null) {
        binding.ratingValue.text = rating.value.toString()
        name?.text = rating.name
        setCardBackgroundColor(rating.color)
        outlineSpotShadowColor = rating.color
    }
}
