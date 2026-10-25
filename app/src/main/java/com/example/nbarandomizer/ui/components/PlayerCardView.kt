package com.example.nbarandomizer.ui.components

import com.example.nbarandomizer.databinding.PlayerCardBinding
import com.example.nbarandomizer.models.Player

class PlayerCardView(private val binding: PlayerCardBinding) {
    val root get() = binding.root
    private val info = PlayerInfoView(binding.photo, binding.name, binding.details)
    private val ratings = PlayerRatingsView(
        RatingView(binding.overallCardView, binding.overallTextView),
        RatingView(binding.threePtCardView, binding.threePtTextView),
        RatingView(binding.dunkCardView, binding.dunkTextView)
    )

    fun bind(player: Player, displayName: String = player.playerName) {
        root.transitionName = "team_player_${player.id}"
        info.bind(player, circleCrop = true, displayName = displayName)
        ratings.bind(player)
    }

    fun onReplace(action: () -> Unit) {
        binding.refreshBtn.setOnClickListener { action() }
    }
}
