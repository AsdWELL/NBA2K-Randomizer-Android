package com.example.nbarandomizer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.nbarandomizer.callbacks.PlayerDiffCallback
import com.example.nbarandomizer.databinding.ItemPlayerBinding
import com.example.nbarandomizer.listeners.IPlayerDetailsListener
import com.example.nbarandomizer.models.Player
import com.example.nbarandomizer.ui.providers.CardOutlineProvider

class PlayerViewHolder(private val binding: ItemPlayerBinding) : RecyclerView.ViewHolder(binding.root) {
    init {
        setupCard(binding.overallCardView)
        setupCard(binding.threePtCardView)
        setupCard(binding.dunkCardView)
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

    fun bind(player: Player, playerDetailsListener: IPlayerDetailsListener) {
        with(binding) {
            itemView.transitionName = player.id.toString()

            name.text = player.name
            details.text = "${player.position} | ${player.height}cm | ${player.team}"
            overallTextView.text = player.overall.value.toString()
            threePtTextView.text = player.threePointRating.value.toString()
            dunkTextView.text = player.dunkRating.value.toString()

            setCardColor(overallCardView, player.overall.color)
            setCardColor(threePtCardView, player.threePointRating.color)
            setCardColor(dunkCardView, player.dunkRating.color)

            Glide.with(binding.root)
                .load(player.photoUrl)
                .signature(ObjectKey("${player.team}_${player.photoUrl}"))
                .into(photo)
        }

        itemView.setOnLongClickListener {
            playerDetailsListener.showPlayerDetails(player, it)
            true
        }
    }
}

class PlayerAdapter(private val playerDetailsListener: IPlayerDetailsListener)
    : ListAdapter<Player, PlayerViewHolder>(PlayerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPlayerBinding.inflate(inflater, parent, false)

        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(currentList[position], playerDetailsListener)
    }
}