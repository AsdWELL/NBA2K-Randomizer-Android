package com.example.nbarandomizer.listeners

import android.view.View
import com.example.nbarandomizer.models.IPlayerBase

interface IPlayerDetailsListener {
    fun showPlayerDetails(player: IPlayerBase, playerCard: View)
}