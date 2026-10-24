package com.example.nbarandomizer.ui.randomizer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.nbarandomizer.R
import com.example.nbarandomizer.databinding.FragmentRandomizerBinding
import com.example.nbarandomizer.models.Position
import com.example.nbarandomizer.viewModels.RandomizerViewModel
import com.example.nbarandomizer.viewModels.SharedViewModel
import com.google.android.material.chip.Chip

class RandomizerFragment : Fragment() {
    private var _binding: FragmentRandomizerBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val randomizerViewModel: RandomizerViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRandomizerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        binding.playersCountSeekBar.progress = randomizerViewModel.playersCount - 1
        binding.playersCountTextView.text = "Игроков: ${randomizerViewModel.playersCount}"
        binding.twoTeamsSwitch.isChecked = randomizerViewModel.twoTeams
        binding.positionsChips.isSingleSelection = randomizerViewModel.playersCount == 1

        Position.entries.forEach { position ->
            val chip = layoutInflater.inflate(R.layout.chip, binding.positionsChips, false) as Chip
            chip.text = position.name
            chip.isSaveEnabled = false
            binding.positionsChips.addView(chip)
            chip.isChecked = position in randomizerViewModel.selectedPositions
            chip.setOnCheckedChangeListener { _, checked ->
                randomizerViewModel.selectPosition(position, checked)
            }
        }

        binding.playersCountSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                randomizerViewModel.playersCount = progress + 1
                binding.positionsChips.isSingleSelection = progress == 0
                binding.playersCountTextView.text = "Игроков: ${randomizerViewModel.playersCount}"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        binding.twoTeamsSwitch.setOnCheckedChangeListener { _, checked ->
            randomizerViewModel.twoTeams = checked
        }

        binding.randomizeButton.setOnClickListener { randomize() }
        binding.resetBtn.setOnClickListener { randomizerViewModel.reset() }
        binding.historyCard.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .add(R.id.container, HistoryFragment(), HistoryFragment.TAG)
                .addToBackStack(HistoryFragment.TAG)
                .commit()
        }
        randomizerViewModel.remainingPlayers.observe(viewLifecycleOwner) {
            binding.remainingPlayersTextView.text = "Осталось игроков: ${it.size}"
        }

        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun randomize() {
        if (sharedViewModel.isDownloadingRoster()) {
            Toast.makeText(context, "Говно скачивается", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            randomizerViewModel.generate(sharedViewModel.selectedRoster)
        } catch (ex: Exception) {
            Toast.makeText(context, ex.message ?: "Не удалось сформировать состав", Toast.LENGTH_SHORT).show()
            return
        }

        parentFragmentManager.beginTransaction()
            .add(R.id.container, TeamFragment(), TeamFragment.TAG)
            .addToBackStack(TeamFragment.TAG)
            .commit()
    }

    override fun onDestroyView() {
        binding.playersCountSeekBar.setOnSeekBarChangeListener(null)
        binding.twoTeamsSwitch.setOnCheckedChangeListener(null)
        super.onDestroyView()
        _binding = null
    }
}
