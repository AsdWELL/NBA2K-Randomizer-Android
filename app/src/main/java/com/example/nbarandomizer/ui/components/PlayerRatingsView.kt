package com.example.nbarandomizer.ui.components

import com.example.nbarandomizer.models.Player

class PlayerRatingsView(
    private val overall: RatingView,
    private val threePoint: RatingView,
    private val dunk: RatingView
) {
    fun bind(player: Player) {
        overall.bind(player.overall)
        threePoint.bind(player.threePointRating)
        dunk.bind(player.dunkRating)
    }
}
