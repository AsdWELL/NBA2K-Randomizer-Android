package com.example.nbarandomizer.ui.randomizer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.nbarandomizer.R
import com.example.nbarandomizer.animators.PlayerCardAnimator
import com.example.nbarandomizer.adapters.TeamAdapter
import com.example.nbarandomizer.databinding.FragmentTeamBinding
import com.example.nbarandomizer.extensions.createEnterTransformation
import com.example.nbarandomizer.extensions.createReturnTransformation
import com.example.nbarandomizer.extensions.hide
import com.example.nbarandomizer.extensions.show
import com.example.nbarandomizer.listeners.IPlayerCardListener
import com.example.nbarandomizer.models.IPlayerBase
import com.example.nbarandomizer.models.Player
import com.example.nbarandomizer.ui.playerDetails.PlayerDetailsFragment
import com.example.nbarandomizer.viewModels.SharedViewModel

class TeamFragment(
    private val players: MutableList<Player>,
    private val teamsCount: Int,
    private val randomizeNewPlayer: (position: Int) -> Player
) : Fragment(), IPlayerCardListener {
    companion object {
        const val TAG = "team"
    }

    private var _binding: FragmentTeamBinding? = null

    private val binding get() = _binding!!

    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var adapter: TeamAdapter

    private fun toastMessage(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onPlayerCardClick(position: Int) {
        val newData = adapter.currentList.toMutableList().apply {
            this[position] = randomizeNewPlayer(position)
        }

        adapter.submitList(newData)
    }

    override fun showPlayerDetails(player: IPlayerBase, playerCard: View) {
        if (sharedViewModel.isDownloadingDetails()) {
            toastMessage("Погоди ща скачается")
            return
        }

        if (sharedViewModel.playersDetails.isEmpty()) {
            toastMessage("Нихуя")
            return
        }

        val playerDetailsFragment = PlayerDetailsFragment(sharedViewModel.playersDetails[player.id], playerCard)

        playerDetailsFragment.onNicknameUpdate = { playerName, nickname ->
            adapter.submitList(adapter.currentList.map { item ->
                if (item.name == playerName)
                    item.copy().apply { this.nickname = nickname}
                else
                    item
            })
        }

        requireActivity().supportFragmentManager
            .beginTransaction()
            .add(R.id.container, playerDetailsFragment, PlayerDetailsFragment.TAG)
            .addToBackStack(PlayerDetailsFragment.TAG)
            .commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeamBinding.inflate(inflater, container, false)

        postponeEnterTransition()

        binding.root.setOnClickListener {}

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        adapter = TeamAdapter(this@TeamFragment)
        adapter.submitList(players)

        binding.recyclerView.itemAnimator = PlayerCardAnimator()
        binding.recyclerView.layoutManager = GridLayoutManager(context, teamsCount)
        binding.recyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.doOnPreDraw {
            startPostponedEnterTransition()
        }

        val randomizeButton = requireActivity().findViewById<View>(R.id.randomizeButton)

        enterTransition = createEnterTransformation(randomizeButton, binding.teamContainer) {
            randomizeButton.hide()
        }

        returnTransition = createReturnTransformation(binding.teamContainer, randomizeButton) {
            randomizeButton.show()
        }
    }

    override fun onDestroy() {
        _binding = null

        super.onDestroy()
    }
}