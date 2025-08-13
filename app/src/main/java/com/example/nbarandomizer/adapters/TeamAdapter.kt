package com.example.nbarandomizer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.nbarandomizer.callbacks.PlayerDiffCallback
import com.example.nbarandomizer.databinding.PlayerCardBinding
import com.example.nbarandomizer.listeners.IPlayerCardListener
import com.example.nbarandomizer.models.Player
import com.example.nbarandomizer.ui.providers.CardOutlineProvider

class TeamViewHolder(private val binding: PlayerCardBinding) : RecyclerView.ViewHolder(binding.root) {
    init {
        setupCard(binding.overallCardView)
        setupCard(binding.threePtCardView)
        setupCard(binding.dunkCardView)
    }

    var isAnimating = false

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

    fun bind(player: Player, playerCardListener: IPlayerCardListener) {
        with(binding) {
            itemView.id = player.id
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
                .circleCrop()
                .into(photo)

            refreshBtn.setOnClickListener {
                if (isAnimating)
                    return@setOnClickListener

                playerCardListener.onPlayerCardClick(adapterPosition)
            }

            itemView.setOnLongClickListener {
                playerCardListener.showPlayerDetails(player, it)
                true
            }
        }
    }
}

class TeamAdapter(private val playerCardListener: IPlayerCardListener)
    : ListAdapter<Player, TeamViewHolder>(PlayerDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PlayerCardBinding.inflate(inflater, parent, false)

        return TeamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        holder.bind(currentList[position], playerCardListener)
    }
}