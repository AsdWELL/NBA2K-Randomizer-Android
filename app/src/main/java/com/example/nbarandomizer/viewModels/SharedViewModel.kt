package com.example.nbarandomizer.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nbarandomizer.models.Epoch
import com.example.nbarandomizer.models.FilterSettings
import com.example.nbarandomizer.models.History
import com.example.nbarandomizer.models.Player
import com.example.nbarandomizer.models.PlayerDetails
import com.example.nbarandomizer.models.PlayerNickname
import com.example.nbarandomizer.models.Version2K
import com.example.nbarandomizer.services.PlayersService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream

sealed class UiState {
    data object Idle : UiState()
    data object LoadingRoster : UiState()
    data object SuccessRoster : UiState()
    data object LoadingDetails : UiState()
    data object SuccessDetails : UiState()
    data class Error(val msg: String) : UiState()
}

class SharedViewModel : ViewModel() {
    private var downloadingJob: Job? = null

    private var downloadingDetailsJob: Job? = null

    private val _uiState = MutableLiveData<UiState>(UiState.Idle)
    val uiState: LiveData<UiState> get() = _uiState

    private lateinit var nicknames: MutableList<PlayerNickname>

    var epoch = Epoch.Current
        private set

    var version: Version2K = Version2K._2K25
        private set

    private val _filterSettingsBinding = MutableLiveData(FilterSettings())
    val filterSettingsBinding: LiveData<FilterSettings> get() = _filterSettingsBinding

    var filterSettings
        get() = _filterSettingsBinding.value!!
        set(value) {
            _filterSettingsBinding.value = value
        }

    private val _selectedRosterBinding: MutableLiveData<MutableList<Player>> = MutableLiveData(mutableListOf())
    val selectedRosterBinding: LiveData<MutableList<Player>> get() = _selectedRosterBinding

    var selectedRoster: MutableList<Player>
        get() = _selectedRosterBinding.value!!
        private set(value) {
            _selectedRosterBinding.value = value
        }

    var isRosterReplaced = false

    var playersDetails = mutableListOf<PlayerDetails>()

    val history = History()

    val nicknamesFile = "nicknames.json"

    private fun applyNickNamesToPlayersAndSetRoster(players: MutableList<Player>) {
        players.forEach { player ->
            player.nickname = nicknames.find { it.playerName == player.name }?.nickname
        }

        isRosterReplaced = true
        selectedRoster = players
    }

    private fun applyNickNamesToDetails(details: MutableList<PlayerDetails>) {
        details.forEach { detail ->
            detail.nickname = nicknames.find { it.playerName == detail.name }?.nickname
        }
    }

    private fun applyNickNamesToDetailsAndSetDetails(details: MutableList<PlayerDetails>) {
        applyNickNamesToDetails(details)

        playersDetails = details
    }

    private suspend fun findChanges(oldData: List<Player>, newData: List<Player>): MutableList<Player> {
        return withContext(Dispatchers.IO) {
            val changes = mutableListOf<Player>()

            for (i in oldData.indices)
                if (oldData[i] != newData[i])
                    changes.add(newData[i])

            changes
        }
    }

    private suspend fun updatePlayersDetails(playersService: PlayersService, updatedPlayers: List<Player>, epoch: Epoch) {
        withContext(Dispatchers.IO) {
            val newDetails = updatedPlayers.map {
                viewModelScope.async(Dispatchers.IO) { playersService.downloadLatest2KVersionPlayerDetails(it) }
            }.awaitAll().toMutableList()

            applyNickNamesToDetails(newDetails)

            newDetails.forEach {
                playersDetails[it.id] = it
            }

            playersService.cachePlayerDetails(playersDetails, epoch, Version2K.latest())

            withContext(Dispatchers.Main) {
                _uiState.value = UiState.SuccessDetails
            }
        }
    }

    fun stopDownloadingJob() {
        downloadingJob?.cancel()
        downloadingDetailsJob?.cancel()

        downloadingJob = null
        downloadingDetailsJob = null
    }

    fun downloadRosterAndDetails(playersService: PlayersService, epoch: Epoch) {
        stopDownloadingJob()

        this.epoch = epoch

        downloadingJob = viewModelScope.launch {
            _uiState.value = UiState.LoadingRoster

            val players: MutableList<Player>

            try {
                withContext(Dispatchers.IO) {
                    players = playersService.downloadAndCacheLatest2KVersionRosterByEpoch(epoch)
                }
            }
            catch (ex: Exception) {
                _uiState.value = UiState.Error(ex.message!!)

                return@launch
            }

            val updatedPlayers = findChanges(selectedRoster, players)

            if (updatedPlayers.size == 0) {
                _uiState.value = UiState.SuccessRoster
                return@launch
            }

            applyNickNamesToPlayersAndSetRoster(players)

            _uiState.value = UiState.LoadingDetails

            updatePlayersDetails(playersService, updatedPlayers, epoch)
        }
    }

    fun getRosterAndDetails(playersService: PlayersService, epoch: Epoch, version: Version2K) {
        stopDownloadingJob()

        if (selectedRoster.isNotEmpty() && this.epoch == epoch && this.version == version)
            return

        this.epoch = epoch
        this.version = version

        filterSettings = FilterSettings()

        downloadingJob = viewModelScope.launch {
             _uiState.value = UiState.LoadingRoster

            try {
                val players = withContext(Dispatchers.IO) {
                    playersService.getPlayersByEpochAnd2KVersion(epoch, version)
                }

                applyNickNamesToPlayersAndSetRoster(players)

                _uiState.value = UiState.SuccessRoster
            }
            catch (ex: Exception) {
                _uiState.value = UiState.Error(ex.message!!)

                return@launch
            }

            downloadingDetailsJob = viewModelScope.launch {
                _uiState.value = UiState.LoadingDetails

                try {
                    val details = withContext(Dispatchers.IO) {
                        playersService.getPlayersDetailsByEpochAnd2KVersion(selectedRoster, epoch, version)
                    }

                    applyNickNamesToDetailsAndSetDetails(details)

                    _uiState.value = UiState.SuccessDetails
                }
                catch (ex: Exception) {
                    _uiState.value = UiState.Error(ex.message!!)

                    playersDetails.clear()

                    return@launch
                }
            }
        }
    }

    fun isDownloadingRoster(): Boolean {
        return downloadingJob?.isActive == true
    }

    fun isDownloadingDetails(): Boolean {
        return downloadingDetailsJob?.isActive == true
    }

    fun setNickname(playerName: String, nickname: String?) {
        playersDetails.find { it.name == playerName }?.nickname = nickname

        if (nickname == null)
            nicknames.remove( nicknames.find { it.playerName == playerName } )
        else {
            val nick = nicknames.find { it.playerName == playerName }

            if (nick == null)
                nicknames.add(PlayerNickname(playerName, nickname))
            else
                nick.nickname = nickname
        }

        viewModelScope.launch(Dispatchers.IO) {
            val updatedRoster = selectedRoster.map { item ->
                if (item.name == playerName)
                    item.copy().apply {
                        this.nickname = nickname
                    }
                else
                    item
            }.toMutableList()

            withContext(Dispatchers.Main) {
                selectedRoster = updatedRoster
            }
        }
    }

    fun getNicknames(file: File) = viewModelScope.launch(Dispatchers.IO) {
        nicknames = Json.decodeFromString<MutableList<PlayerNickname>>(file.readText())
    }

    fun saveNicknames(file: File) = viewModelScope.launch(Dispatchers.IO) {
        FileOutputStream(file, false).use {
            it.write(Json.encodeToString(nicknames).toByteArray())
        }
    }

    override fun onCleared() {
        super.onCleared()

        stopDownloadingJob()
    }
}