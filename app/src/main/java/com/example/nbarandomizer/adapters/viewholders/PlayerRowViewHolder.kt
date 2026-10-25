package com.example.nbarandomizer.adapters.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.nbarandomizer.models.IPlayerBase
import com.example.nbarandomizer.ui.components.PlayerRowView

class PlayerRowViewHolder<T : IPlayerBase>(private val row: PlayerRowView<T>) : RecyclerView.ViewHolder(row.root) {
    fun bind(player: T, onClick: ((T, View) -> Unit)?, onLongClick: ((T, View) -> Unit)?) {
        row.bind(player)
        if (onClick != null) itemView.setOnClickListener { onClick(player, it) }
        if (onLongClick != null) itemView.setOnLongClickListener {
            onLongClick(player, it)
            true
        }
    }
}
