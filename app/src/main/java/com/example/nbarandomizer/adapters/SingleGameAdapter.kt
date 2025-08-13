package com.example.nbarandomizer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.nbarandomizer.callbacks.PlayerDiffCallback
import com.example.nbarandomizer.databinding.ItemHistoryPlayerBinding
import com.example.nbarandomizer.models.Player

class SingleGameViewHolder(private val binding: ItemHistoryPlayerBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(player: Player) {
        with(binding) {
            name.text = player.name
            details.text = "${player.position} | ${player.height}cm | ${player.team}"

            Glide.with(binding.root)
                .load(player.photoUrl)
                .signature(ObjectKey("${player.team}_${player.photoUrl}"))
                .into(photo)
        }
    }
}

class SingleGameAdapter : ListAdapter<Player, SingleGameViewHolder>(PlayerDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingleGameViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemHistoryPlayerBinding.inflate(inflater, parent, false)

        return SingleGameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SingleGameViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}