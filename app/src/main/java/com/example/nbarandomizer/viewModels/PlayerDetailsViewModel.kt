package com.example.nbarandomizer.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nbarandomizer.models.IPlayerBase
import com.example.nbarandomizer.models.PlayerDetails
import com.example.nbarandomizer.models.SearchPlayerResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class PlayerDetailsViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    companion object {
        const val ARG_PLAYER = "player"
        const val ARG_DETAILS = "details"
        const val ARG_NICKNAME = "nickname"
        private const val NICKNAME_DRAFT = "nicknameDraft"
    }

    val player: IPlayerBase = Json.decodeFromString<SearchPlayerResult>(
        checkNotNull(savedState[ARG_PLAYER]) { "Player argument is required" }
    )

    private val _details = MutableLiveData(
        savedState.get<String>(ARG_DETAILS)?.let { Json.decodeFromString<PlayerDetails>(it) }
            ?.apply { nickname = savedState[ARG_NICKNAME] }
    )
    val details: LiveData<PlayerDetails?> = _details

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    private var loadingJob: Job? = null

    var nicknameDraft: String
        get() = savedState[NICKNAME_DRAFT] ?: _details.value?.nickname.orEmpty()
        set(value) { savedState[NICKNAME_DRAFT] = value }

    fun loadDetails(loader: suspend (IPlayerBase) -> PlayerDetails) {
        if (_details.value != null || loadingJob != null) return
        loadingJob = viewModelScope.launch {
            try {
                _details.value = withContext(Dispatchers.IO) { loader(player) }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                _error.value = ex.message ?: "Не удалось получить данные об игроке"
            }
        }
    }

    fun consumeError() { _error.value = null }
}
