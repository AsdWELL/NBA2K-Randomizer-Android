package com.example.nbarandomizer.ui.components

import com.example.nbarandomizer.databinding.PlayerCardBinding
import com.example.nbarandomizer.models.Player

class PlayerCardView(private val binding: PlayerCardBinding) {
    val root get() = binding.root
    private val info = PlayerInfoView(binding.photo, binding.name.root, binding.details.root)
    private val ratings = PlayerRatingsView(
        binding.overallCardView,
        binding.threePtCardView,
        binding.dunkCardView
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
