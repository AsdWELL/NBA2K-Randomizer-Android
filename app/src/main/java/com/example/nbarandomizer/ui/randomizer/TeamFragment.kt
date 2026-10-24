package com.example.nbarandomizer.ui.randomizer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.nbarandomizer.R
import com.example.nbarandomizer.animators.PlayerCardAnimator
import com.example.nbarandomizer.adapters.TeamAdapter
import com.example.nbarandomizer.databinding.FragmentTeamBinding
import com.example.nbarandomizer.extensions.createEnterTransformation
import com.example.nbarandomizer.extensions.createReturnTransformation
import com.example.nbarandomizer.extensions.hide
import com.example.nbarandomizer.extensions.makeBlurBackground
import com.example.nbarandomizer.extensions.show
import com.example.nbarandomizer.listeners.IPlayerCardListener
import com.example.nbarandomizer.models.IPlayerBase
import com.example.nbarandomizer.ui.playerDetails.PlayerDetailsFragment
import com.example.nbarandomizer.viewModels.SharedViewModel
import com.example.nbarandomizer.viewModels.RandomizerViewModel

class TeamFragment : Fragment(), IPlayerCardListener {
    companion object {
        const val TAG = "team"
    }

    private var _binding: FragmentTeamBinding? = null

    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val randomizerViewModel: RandomizerViewModel by activityViewModels()

    private lateinit var adapter: TeamAdapter

    private fun toastMessage(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onPlayerCardClick(position: Int) {
        try {
            randomizerViewModel.replacePlayer(position)
        } catch (ex: Exception) {
            toastMessage(ex.message ?: "Не удалось заменить игрока")
        }
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

        val playerDetailsFragment = PlayerDetailsFragment.newInstance(
            player, playerCard.transitionName, sharedViewModel.playersDetails[player.id]
        )

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

        adapter = TeamAdapter(this@TeamFragment)

        binding.recyclerView.itemAnimator = PlayerCardAnimator()
        binding.recyclerView.layoutManager = GridLayoutManager(context, randomizerViewModel.teamsCount)
        binding.recyclerView.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        randomizerViewModel.currentTeam.observe(viewLifecycleOwner) { adapter.submitList(it) }

        view.doOnPreDraw {
            startPostponedEnterTransition()
        }

        val randomizeButton = requireActivity().findViewById<View>(R.id.randomizeButton)

        if (randomizeButton != null) {
            enterTransition = createEnterTransformation(randomizeButton, binding.teamContainer) {
                randomizeButton.hide()
            }
            returnTransition = createReturnTransformation(binding.teamContainer, randomizeButton) {
                randomizeButton.show()
            }
        }

        makeBlurBackground()
    }

    override fun onDestroyView() {
        binding.recyclerView.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
