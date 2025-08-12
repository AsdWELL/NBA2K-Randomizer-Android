package com.example.nbarandomizer.listeners

import android.view.View
import com.example.nbarandomizer.models.Player

interface IPlayerDetailsListener {
    fun showPlayerDetails(player: Player, playerCard: View)
}