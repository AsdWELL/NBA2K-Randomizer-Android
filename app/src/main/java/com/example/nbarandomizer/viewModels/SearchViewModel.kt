package com.example.nbarandomizer.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nbarandomizer.models.SearchPlayerResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val query: String get() = savedState["query"] ?: ""

    private val _results = MutableLiveData<List<SearchPlayerResult>>(emptyList())
    val results: LiveData<List<SearchPlayerResult>> = _results
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var searchJob: Job? = null
    private var lastRequestedQuery: String? = null

    fun search(query: String, loader: suspend (String) -> List<SearchPlayerResult>) {
        savedState["query"] = query
        if (lastRequestedQuery == query) return
        lastRequestedQuery = query
        searchJob?.cancel()
        _error.value = null
        if (query.length < 2) {
            _isLoading.value = false
            _results.value = emptyList()
            return
        }
        _isLoading.value = true
        searchJob = viewModelScope.launch {
            try {
                _results.value = withContext(Dispatchers.IO) { loader(query) }
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                _error.value = ex.message ?: "Отсутствует подключение к интернету"
            } finally {
                if (isActive) _isLoading.value = false
            }
        }
    }

    fun consumeError() { _error.value = null }
}
