package com.example.nbarandomizer.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.example.nbarandomizer.adapters.viewholders.TeamViewHolder
import com.example.nbarandomizer.callbacks.TeamSlotDiffCallback
import com.example.nbarandomizer.databinding.PlayerCardBinding
import com.example.nbarandomizer.listeners.IPlayerCardListener
import com.example.nbarandomizer.ui.models.TeamSlot

class TeamAdapter(private val listener: IPlayerCardListener) : ListAdapter<TeamSlot, TeamViewHolder>(TeamSlotDiffCallback()) {
    init { setHasStableIds(true) }

    override fun getItemId(position: Int) = getItem(position).index.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val binding = PlayerCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TeamViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }
}
