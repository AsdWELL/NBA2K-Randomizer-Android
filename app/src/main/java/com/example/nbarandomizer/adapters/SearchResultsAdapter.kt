package com.example.nbarandomizer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.nbarandomizer.callbacks.SearchPlayerResultsDiffCallback
import com.example.nbarandomizer.databinding.ItemSearchPlayerBinding
import com.example.nbarandomizer.listeners.IPlayerDetailsListener
import com.example.nbarandomizer.models.SearchPlayerResult
import com.example.nbarandomizer.ui.providers.CardOutlineProvider

class SearchResultViewHolder(private val binding: ItemSearchPlayerBinding) : RecyclerView.ViewHolder(binding.root) {
    init {
        setupCard(binding.overallCardView)
    }

    private fun setupCard(card: CardView) {
        card.cardElevation = 5f
        card.preventCornerOverlap = false
        card.clipToOutline = false
        card.outlineProvider = CardOutlineProvider(card)
    }

    private fun setCardColor(card: CardView, color: Int) {
        card.setCardBackgroundColor(color)
        card.outlineSpotShadowColor = color
    }

    fun bind(searchPlayerResult: SearchPlayerResult, playerDetailsListener: IPlayerDetailsListener) {
        itemView.transitionName = searchPlayerResult.url

        with(binding) {
            name.text = searchPlayerResult.name
            team.text = searchPlayerResult.team
            overallTextView.text = searchPlayerResult.overall.value.toString()

            setCardColor(overallCardView, searchPlayerResult.overall.color)

            Glide.with(binding.root)
                .load(searchPlayerResult.photoUrl)
                .signature(ObjectKey("${searchPlayerResult.team}_${searchPlayerResult.photoUrl}"))
                .into(photo)
        }

        itemView.setOnClickListener {
            playerDetailsListener.showPlayerDetails(searchPlayerResult, it)
        }
    }
}

class SearchResultsAdapter(private val playerDetailsListener: IPlayerDetailsListener)
    : ListAdapter<SearchPlayerResult, SearchResultViewHolder>(SearchPlayerResultsDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSearchPlayerBinding.inflate(inflater, parent, false)

        return SearchResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bind(currentList[position], playerDetailsListener)
    }
}