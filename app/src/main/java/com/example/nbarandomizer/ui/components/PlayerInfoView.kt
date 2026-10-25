package com.example.nbarandomizer.ui.components

import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.nbarandomizer.databinding.ViewPlayerInfoHorizontalBinding
import com.example.nbarandomizer.models.IPlayerBase
import com.example.nbarandomizer.models.Player
import com.example.nbarandomizer.models.SearchPlayerResult

class PlayerInfoView(
    private val photo: ImageView,
    private val name: TextView,
    private val details: TextView
) {
    constructor(binding: ViewPlayerInfoHorizontalBinding) : this(binding.photo, binding.name, binding.details)

    fun bind(player: Player, circleCrop: Boolean = false, displayName: String = player.playerName) {
        bind(player, displayName, "${player.position} | ${player.height}cm | ${player.team}", circleCrop)
    }

    fun bind(player: SearchPlayerResult) {
        bind(player, player.name, player.team, circleCrop = false)
    }

    private fun bind(player: IPlayerBase, displayName: String, description: String, circleCrop: Boolean) {
        name.text = displayName
        details.text = description
        val request = Glide.with(photo)
            .load(player.photoUrl)
            .signature(ObjectKey("${player.team}_${player.photoUrl}"))
        (if (circleCrop) request.circleCrop() else request).into(photo)
    }
}
