package com.example.nbarandomizer.adapters.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.example.nbarandomizer.databinding.PlayerCardBinding
import com.example.nbarandomizer.listeners.IPlayerCardListener
import com.example.nbarandomizer.ui.components.PlayerCardView
import com.example.nbarandomizer.ui.models.TeamSlot

class TeamViewHolder(binding: PlayerCardBinding) : RecyclerView.ViewHolder(binding.root) {
    private val card = PlayerCardView(binding)
    var isAnimating = false
    var playerId: Int? = null
        private set

    fun bind(slot: TeamSlot, listener: IPlayerCardListener) {
        playerId = slot.player.id
        card.bind(slot.player, slot.displayName)
        card.onReplace {
            if (!isAnimating && bindingAdapterPosition != RecyclerView.NO_POSITION) {
                listener.onPlayerCardClick(slot.index)
            }
        }
        itemView.setOnLongClickListener {
            listener.showPlayerDetails(slot.player, it)
            true
        }
    }
}
