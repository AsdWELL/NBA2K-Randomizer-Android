package com.example.nbarandomizer.ui.roster

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nbarandomizer.R
import com.example.nbarandomizer.adapters.PlayerAdapter
import com.example.nbarandomizer.databinding.FragmentRosterBinding
import com.example.nbarandomizer.extensions.applyFilterSettingsAndSort
import com.example.nbarandomizer.listeners.IPlayerDetailsListener
import com.example.nbarandomizer.models.IPlayerBase
import com.example.nbarandomizer.models.FilterSettings
import com.example.nbarandomizer.ui.playerDetails.PlayerDetailsFragment
import com.example.nbarandomizer.viewModels.SharedViewModel

class RosterFragment : Fragment(), IPlayerDetailsListener {
    private var _binding: FragmentRosterBinding? = null

    private val binding get() = _binding!!

    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var adapter: PlayerAdapter
    private var lastFilterSettings: FilterSettings? = null

    private fun toastMessage(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRosterBinding.inflate(inflater, container, false)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        adapter = PlayerAdapter(this)

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        binding.filterButton.setOnClickListener {
            if (sharedViewModel.isDownloadingRoster()) {
                toastMessage("Погоди ща скачается")

                return@setOnClickListener
            }

            requireActivity().supportFragmentManager
               .beginTransaction()
               .add(R.id.container, FilterFragment(), FilterFragment.TAG)
               .addToBackStack(FilterFragment.TAG)
               .commit()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        bindRoster()
    }

    private fun showPlayersCount(count: Int) {
        binding.playersCountTextView.text = "Показано $count из 100 игроков"
    }

    private fun setFilteredRoster(scrollToTop: Boolean) {
        val filteredPlayers = sharedViewModel.selectedRoster.applyFilterSettingsAndSort(sharedViewModel.filterSettings)
        adapter.submitList(filteredPlayers) {
            if (scrollToTop) _binding?.recyclerView?.scrollToPosition(0)
        }
        showPlayersCount(filteredPlayers.size)
    }

    private fun bindRoster() {
        sharedViewModel.selectedRosterBinding.observe(viewLifecycleOwner) {
            setFilteredRoster(false)
        }

        sharedViewModel.filterSettingsBinding.observe(viewLifecycleOwner) { settings ->
            setFilteredRoster(lastFilterSettings != null && lastFilterSettings != settings)
            lastFilterSettings = settings.copy()
        }
    }

    override fun onDestroyView() {
        binding.recyclerView.adapter = null
        lastFilterSettings = null
        super.onDestroyView()
        _binding = null
    }
}
