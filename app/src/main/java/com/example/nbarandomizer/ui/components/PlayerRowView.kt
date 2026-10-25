package com.example.nbarandomizer.ui.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.nbarandomizer.databinding.ItemHistoryPlayerBinding
import com.example.nbarandomizer.databinding.ItemPlayerBinding
import com.example.nbarandomizer.databinding.ItemSearchPlayerBinding
import com.example.nbarandomizer.databinding.ViewPlayerInfoHorizontalBinding
import com.example.nbarandomizer.models.IPlayerBase
import com.example.nbarandomizer.models.Player
import com.example.nbarandomizer.models.SearchPlayerResult

class PlayerRowView<T : IPlayerBase> private constructor(val root: View, private val bindPlayer: (T) -> Unit) {
    fun bind(player: T) = bindPlayer(player)

    companion object {
        fun roster(parent: ViewGroup): PlayerRowView<Player> {
            val binding = ItemPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val info = PlayerInfoView(ViewPlayerInfoHorizontalBinding.bind(binding.root))
            val ratings = PlayerRatingsView(
                binding.overallCardView,
                binding.threePtCardView,
                binding.dunkCardView
            )
            return PlayerRowView(binding.root) { player: Player ->
                binding.root.transitionName = "roster_player_${player.id}"
                info.bind(player)
                ratings.bind(player)
            }
        }

        fun search(parent: ViewGroup): PlayerRowView<SearchPlayerResult> {
            val binding = ItemSearchPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val info = PlayerInfoView(ViewPlayerInfoHorizontalBinding.bind(binding.root))
            val overall = binding.overallCardView
            return PlayerRowView(binding.root) { player: SearchPlayerResult ->
                binding.root.transitionName = "search_player_${player.url}"
                info.bind(player)
                overall.bind(player.overall)
            }
        }

        fun history(parent: ViewGroup): PlayerRowView<Player> {
            val binding = ItemHistoryPlayerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val info = PlayerInfoView(binding.photo, binding.name.root, binding.details.root)
            return PlayerRowView(binding.root) { player: Player -> info.bind(player) }
        }
    }
}
