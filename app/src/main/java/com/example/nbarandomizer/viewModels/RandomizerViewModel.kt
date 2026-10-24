package com.example.nbarandomizer.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nbarandomizer.extensions.generateCompleteTeams
import com.example.nbarandomizer.extensions.generateTeams
import com.example.nbarandomizer.extensions.getNextPlayer
import com.example.nbarandomizer.extensions.getNextPlayerByPosition
import com.example.nbarandomizer.extensions.randomize
import com.example.nbarandomizer.models.History
import com.example.nbarandomizer.models.Player
import com.example.nbarandomizer.models.Position

class RandomizerViewModel : ViewModel() {
    var playersCount = 1
    var twoTeams = false

    private val _selectedPositions = mutableListOf<Position>()
    val selectedPositions: List<Position> get() = _selectedPositions.toList()

    private val _remainingPlayers = MutableLiveData<List<Player>>(emptyList())
    val remainingPlayers: LiveData<List<Player>> = _remainingPlayers

    private val _currentTeam = MutableLiveData<List<Player>>(emptyList())
    val currentTeam: LiveData<List<Player>> = _currentTeam

    var teamsCount = 1
        private set

    val history = History()

    private var rosterRevision: Int? = null
    private var teamPositions: List<Position> = emptyList()

    fun selectPosition(position: Position, selected: Boolean) {
        if (selected && position !in _selectedPositions) _selectedPositions.add(position)
        if (!selected) _selectedPositions.remove(position)
    }

    fun onRosterChanged(roster: List<Player>, revision: Int) {
        if (rosterRevision != revision) {
            rosterRevision = revision
            reset()
        } else {
            _remainingPlayers.value = _remainingPlayers.value.orEmpty().map { roster[it.id] }
            _currentTeam.value = _currentTeam.value.orEmpty().map { roster[it.id] }
            history.updatePlayers(roster)
        }
    }

    fun generate(roster: MutableList<Player>) {
        require(_selectedPositions.isEmpty() || _selectedPositions.size == playersCount) {
            "Хуйня"
        }

        val startNewSession = _remainingPlayers.value.orEmpty().isEmpty()
        val pool = if (startNewSession) roster.randomize()
            else _remainingPlayers.value.orEmpty().toMutableList()
        val count = if (twoTeams) 2 else 1
        val positions = selectedPositions

        val players = when {
            playersCount == 1 -> MutableList(count) {
                if (positions.isEmpty()) pool.getNextPlayer()
                else pool.getNextPlayerByPosition(positions.first())
            }
            positions.isNotEmpty() -> pool.generateCompleteTeams(positions, count)
            else -> pool.generateTeams(playersCount, count).toMutableList()
        }

        if (startNewSession) history.clear()
        history.addPlayers(players, count)
        teamsCount = count
        teamPositions = positions
        _remainingPlayers.value = pool
        _currentTeam.value = players.toList()
    }

    fun replacePlayer(position: Int) {
        val team = _currentTeam.value.orEmpty().toMutableList()
        require(position in team.indices) { "Игрок не найден в текущем составе" }
        val pool = _remainingPlayers.value.orEmpty().toMutableList()
        val player = if (teamPositions.isEmpty()) pool.getNextPlayer()
            else pool.getNextPlayerByPosition(teamPositions[position / teamsCount])

        history.addPlayerReplacementToLastGame(position, player)
        team[position] = player
        _remainingPlayers.value = pool
        _currentTeam.value = team
    }

    fun reset() {
        _remainingPlayers.value = emptyList()
        _currentTeam.value = emptyList()
        teamPositions = emptyList()
        history.clear()
    }
}
