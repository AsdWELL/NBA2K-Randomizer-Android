package com.example.nbarandomizer.ui.models

import com.example.nbarandomizer.models.Player

/** A place in the generated lineup keeps its identity when its player is replaced. */
data class TeamSlot(val index: Int, val player: Player, val displayName: String = player.playerName)
