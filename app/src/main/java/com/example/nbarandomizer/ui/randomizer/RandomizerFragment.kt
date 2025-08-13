package com.example.nbarandomizer.ui.randomizer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.nbarandomizer.R
import com.example.nbarandomizer.databinding.FragmentRandomizerBinding
import com.example.nbarandomizer.extensions.generateCompleteTeams
import com.example.nbarandomizer.extensions.generateTeams
import com.example.nbarandomizer.extensions.getNextPlayer
import com.example.nbarandomizer.extensions.getNextPlayerByPosition
import com.example.nbarandomizer.extensions.randomize
import com.example.nbarandomizer.models.Player
import com.example.nbarandomizer.models.Position
import com.example.nbarandomizer.viewModels.SharedViewModel
import com.google.android.material.chip.Chip

class RandomizerFragment : Fragment() {
    private var _binding: FragmentRandomizerBinding? = null

    private val binding get() = _binding!!

    private lateinit var sharedViewModel: SharedViewModel

    private var playersCount: Int = 1

    private val selectedPositions: MutableList<Position> = mutableListOf()

    private var shuffledPlayers: MutableLiveData<MutableList<Player>> = MutableLiveData(mutableListOf())

    private val history get() = sharedViewModel.history

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRandomizerBinding.inflate(inflater, container, false)

        postponeEnterTransition()

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        initializePositionsChips()
        setSeekBarListeners()
        setOnClickListeners()

        sharedViewModel.selectedRosterBinding.observe(viewLifecycleOwner) { newRoster ->
            if (sharedViewModel.isRosterReplaced) {
                sharedViewModel.isRosterReplaced = false

                reset()
            }
            else {
                shuffledPlayers.value = shuffledPlayers.value!!.map { newRoster[it.id] }.toMutableList()

                history.updatePlayers(newRoster)
            }
        }

        shuffledPlayers.observe(viewLifecycleOwner) { binding.remainingPlayersTextView.text = "Осталось игроков: ${it.size}" }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setOnClickListeners() {
        binding.randomizeButton.setOnClickListener { randomize() }
        binding.resetBtn.setOnClickListener { reset() }

        binding.historyCard.setOnClickListener { showHistory() }
    }

    private fun initializePositionsChips() {
        Position.entries.map { it.toString() }.forEach(::addChip)
    }

    private fun addChip(text: String) {
        val chip = layoutInflater.inflate(R.layout.chip, binding.positionsChips, false) as Chip
        chip.text = text

        chip.setOnCheckedChangeListener { _, isChecked ->
            val position = Position.valueOf(chip.text.toString())

            if (isChecked)
                 selectedPositions.add(position)
            else
                selectedPositions.remove(position)
        }

        binding.positionsChips.addView(chip)
    }

    private fun setSeekBarListeners() {
        binding.playersCountSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                playersCount = progress + 1
                binding.positionsChips.isSingleSelection = progress == 0
                binding.playersCountTextView.text = "Игроков: ${playersCount}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun toastMessage(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    private fun randomize() {
        if (sharedViewModel.isDownloadingRoster()) {
            toastMessage("Говно скачивается")
            return
        }

        if (selectedPositions.isNotEmpty() && selectedPositions.size != playersCount) {
            toastMessage("Хуйня")
            return
        }

        var players = shuffledPlayers.value!!

        if (players.isEmpty()) {
            players = sharedViewModel.selectedRoster.randomize()
            history.clear()
        }

        var randomizedPlayers = mutableListOf<Player>()
        val teamsCount = when (binding.twoTeamsSwitch.isChecked) {
            true -> 2
            false -> 1
        }

        try {
            if (playersCount == 1) {
                repeat(teamsCount) {
                    if (selectedPositions.size > 0)
                        randomizedPlayers.add(players.getNextPlayerByPosition(selectedPositions[0]))
                    else
                        randomizedPlayers.add(players.getNextPlayer())
                }
            }
            else {
                if (selectedPositions.size > 0)
                    randomizedPlayers.addAll(players.generateCompleteTeams(selectedPositions, teamsCount))
                else
                    randomizedPlayers.addAll(players.generateTeams(playersCount, teamsCount))
            }
        }
        catch (ex: Exception) {
            toastMessage(ex.message!!)
            return
        }

        history.addPlayers(randomizedPlayers, teamsCount)

        val teamFragment = TeamFragment(randomizedPlayers, teamsCount)  { position ->
            val buffer = shuffledPlayers.value!!

            val newPlayer = try {
                when(selectedPositions.size) {
                    0 -> buffer.getNextPlayer()
                    else -> buffer.getNextPlayerByPosition(selectedPositions[position / teamsCount])
                }
            }
            catch (ex: Exception) {
                toastMessage(ex.message!!)
                return@TeamFragment randomizedPlayers[position]
            }

            history.addPlayerReplacementToLastGame(position, newPlayer)

            randomizedPlayers = randomizedPlayers.toMutableList().apply {
                this[position] = newPlayer
            }

            shuffledPlayers.value = buffer

            newPlayer
        }

        requireActivity().supportFragmentManager
            .beginTransaction()
            .add(R.id.container, teamFragment, TeamFragment.TAG)
            .addToBackStack(TeamFragment.TAG)
            .commit()

        shuffledPlayers.value = players
    }

    private fun reset() {
        binding.positionsChips.clearCheck()
        binding.playersCountSeekBar.progress = 0
        shuffledPlayers.value = mutableListOf()
        history.clear()
    }

    private fun showHistory() {
        val historyFragment = HistoryFragment()

        requireActivity().supportFragmentManager
            .beginTransaction()
            .add(R.id.container, historyFragment, HistoryFragment.TAG)
            .addToBackStack(HistoryFragment.TAG)
            .commit()
    }

    override fun onDestroy() {
        _binding = null

        super.onDestroy()
    }
}