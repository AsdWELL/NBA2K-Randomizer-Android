package com.example.nbarandomizer.callbacks

import androidx.recyclerview.widget.DiffUtil
import com.example.nbarandomizer.ui.models.TeamSlot

class TeamSlotDiffCallback : DiffUtil.ItemCallback<TeamSlot>() {
    override fun areItemsTheSame(oldItem: TeamSlot, newItem: TeamSlot) = oldItem.index == newItem.index

    override fun areContentsTheSame(oldItem: TeamSlot, newItem: TeamSlot) =
        oldItem.player == newItem.player && oldItem.displayName == newItem.displayName
}
