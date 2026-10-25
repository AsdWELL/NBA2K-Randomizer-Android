package com.example.nbarandomizer.adapters

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.nbarandomizer.adapters.viewholders.PlayerRowViewHolder
import com.example.nbarandomizer.callbacks.PlayerDiffCallback
import com.example.nbarandomizer.callbacks.SearchPlayerResultsDiffCallback
import com.example.nbarandomizer.listeners.IPlayerDetailsListener
import com.example.nbarandomizer.models.IPlayerBase
import com.example.nbarandomizer.models.Player
import com.example.nbarandomizer.models.SearchPlayerResult
import com.example.nbarandomizer.ui.components.PlayerRowView

class PlayerRowAdapter<T : IPlayerBase> private constructor(
    private val createRow: (ViewGroup) -> PlayerRowView<T>,
    diffCallback: DiffUtil.ItemCallback<T>,
    private val onClick: ((T, View) -> Unit)? = null,
    private val onLongClick: ((T, View) -> Unit)? = null
) : ListAdapter<T, PlayerRowViewHolder<T>>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PlayerRowViewHolder(createRow(parent))

    override fun onBindViewHolder(holder: PlayerRowViewHolder<T>, position: Int) {
        holder.bind(getItem(position), onClick, onLongClick)
    }

    companion object {
        fun roster(listener: IPlayerDetailsListener): PlayerRowAdapter<Player> = PlayerRowAdapter(
            PlayerRowView.Companion::roster, PlayerDiffCallback(),
            onLongClick = { player, view -> listener.showPlayerDetails(player, view) }
        )

        fun search(listener: IPlayerDetailsListener): PlayerRowAdapter<SearchPlayerResult> = PlayerRowAdapter(
            PlayerRowView.Companion::search, SearchPlayerResultsDiffCallback(),
            onClick = { player, view -> listener.showPlayerDetails(player, view) }
        )

        fun history(): PlayerRowAdapter<Player> = PlayerRowAdapter(PlayerRowView.Companion::history, PlayerDiffCallback())
    }
}
