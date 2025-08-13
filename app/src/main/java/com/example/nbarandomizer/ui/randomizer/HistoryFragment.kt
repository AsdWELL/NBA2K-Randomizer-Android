package com.example.nbarandomizer.ui.randomizer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nbarandomizer.R
import com.example.nbarandomizer.adapters.HistoryAdapter
import com.example.nbarandomizer.databinding.FragmentHistoryBinding
import com.example.nbarandomizer.extensions.createEnterTransformation
import com.example.nbarandomizer.extensions.createReturnTransformation
import com.example.nbarandomizer.extensions.gone
import com.example.nbarandomizer.extensions.hide
import com.example.nbarandomizer.extensions.show
import com.example.nbarandomizer.viewModels.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HistoryFragment : Fragment() {
    companion object {
        const val TAG = "history"
    }

    private var _binding: FragmentHistoryBinding? = null

    private val binding get() = _binding!!

    private lateinit var sharedViewModel: SharedViewModel

    private val history get() = sharedViewModel.history

    private lateinit var adapter: HistoryAdapter

    private fun bindHistory() {
        with(binding) {
            remainingPlayersTextView.text = history.remainingPlayersCount.toString()
            usedPlayersTextView.text = history.usedPlayersCount.toString()
            gamesCountTextView.text = history.gamesCount.toString()
        }

        initAndAttachHistoryAdapter()
    }

    private fun initAndAttachHistoryAdapter() {
        if (history.games.isEmpty()) {
            binding.statsCard.hide()
            binding.emptyHistoryMsg.show()

            return
        }

        binding.progressBar.show()

        lifecycleScope.launch(Dispatchers.IO) {
            adapter = HistoryAdapter()
            adapter.games = sharedViewModel.history.games

            binding.recyclerView.layoutManager = LinearLayoutManager(context)

            withContext(Dispatchers.Main) {
                binding.recyclerView.adapter = adapter
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        binding.root.setOnClickListener {}
        binding.backBtn.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        bindHistory()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val historyCard = requireActivity().findViewById<View>(R.id.history_card)

        enterTransition = createEnterTransformation(historyCard, binding.historyContainer) {
            historyCard.hide()
            binding.progressBar.gone()
            binding.recyclerView.show()
        }

        returnTransition = createReturnTransformation(binding.historyContainer, historyCard) {
            historyCard.show()
        }
    }
}