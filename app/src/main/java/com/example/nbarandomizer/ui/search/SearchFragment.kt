package com.example.nbarandomizer.ui.search

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.transition.doOnEnd
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nbarandomizer.App
import com.example.nbarandomizer.R
import com.example.nbarandomizer.adapters.SearchResultsAdapter
import com.example.nbarandomizer.databinding.FragmentSearchBinding
import com.example.nbarandomizer.extensions.createEnterTransformation
import com.example.nbarandomizer.extensions.createReturnTransformation
import com.example.nbarandomizer.listeners.IPlayerDetailsListener
import com.example.nbarandomizer.models.IPlayerBase
import com.example.nbarandomizer.services.PlayersService
import com.example.nbarandomizer.ui.playerDetails.PlayerDetailsFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SearchFragment : Fragment(), IPlayerDetailsListener {
    private var _binding: FragmentSearchBinding? = null

    private val binding get() = _binding!!

    private val playersService: PlayersService
        get() = (requireActivity().applicationContext as App).playersService

    private lateinit var adapter: SearchResultsAdapter

    private fun toastMessage(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        binding.searchView.clearFocus()

        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE)
                as? InputMethodManager
                ?: return

        imm.hideSoftInputFromWindow(binding.searchView.windowToken, 0)
    }

    private fun showKeyboard() {
        binding.searchView.requestFocus()

        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE)
                as InputMethodManager?
                ?: return

        imm.showSoftInput(binding.searchView, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun bindSearchViewToRecyclerView() {
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val searchResults = playersService.searchPlayers(s.toString())

                    withContext(Dispatchers.Main) {
                        adapter.submitList(searchResults)
                    }
                }
            }
        })
    }

    private fun initializeSearchRecyclerView() {
        adapter = SearchResultsAdapter(this)

        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SearchFragment.adapter
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        binding.root.setOnClickListener {}

        binding.backBtn.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        initializeSearchRecyclerView()
        bindSearchViewToRecyclerView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchBar = requireActivity().findViewById<View>(R.id.search_bar)

        enterTransition = TransitionSet().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
            addTransition(createEnterTransformation(searchBar, binding.root))
            addTransition(createEnterTransformation(searchBar, binding.searchView))
            doOnEnd { showKeyboard() }
        }

        returnTransition = TransitionSet().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
            addTransition(createReturnTransformation(binding.root, searchBar))
            addTransition(createReturnTransformation(binding.searchView, searchBar))
        }
    }

    override fun showPlayerDetails(player: IPlayerBase, playerCard: View) {
        hideKeyboard()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val playerDetailsFragment = PlayerDetailsFragment(player, playerCard)

                withContext(Dispatchers.Main) {
                    requireActivity().supportFragmentManager
                        .beginTransaction()
                        .add(R.id.container, playerDetailsFragment, "details")
                        .addToBackStack("details")
                        .commit()
                }

                val details = playersService.downloadLatest2KVersionPlayerDetails(player)

                withContext(Dispatchers.Main) {
                    playerDetailsFragment.playerDetails = details
                }
            }
            catch(ex: Exception) {
                withContext(Dispatchers.Main) {
                    if (ex.message != null)
                        toastMessage(ex.message!!)

                    requireActivity().supportFragmentManager.popBackStack()
                }
            }
        }
    }
}