package com.example.nbarandomizer.ui.search

import android.content.Context
import android.os.Bundle
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.transition.doOnEnd
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nbarandomizer.App
import com.example.nbarandomizer.R
import com.example.nbarandomizer.adapters.SearchResultsAdapter
import com.example.nbarandomizer.databinding.FragmentSearchBinding
import com.example.nbarandomizer.extensions.createEnterTransformation
import com.example.nbarandomizer.extensions.createReturnTransformation
import com.example.nbarandomizer.listeners.IPlayerDetailsListener
import com.example.nbarandomizer.models.IPlayerBase
import com.example.nbarandomizer.ui.playerDetails.PlayerDetailsFragment
import com.example.nbarandomizer.viewModels.SearchViewModel

class SearchFragment : Fragment(), IPlayerDetailsListener {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val searchViewModel: SearchViewModel by viewModels()
    private lateinit var adapter: SearchResultsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.root.setOnClickListener {}
        binding.backBtn.setOnClickListener { parentFragmentManager.popBackStack() }
        adapter = SearchResultsAdapter(this)
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        val service = (requireActivity().application as App).playersService
        binding.searchView.setText(searchViewModel.query)
        binding.searchView.doAfterTextChanged {
            searchViewModel.search(it?.toString().orEmpty(), service::searchPlayers)
        }
        searchViewModel.results.observe(viewLifecycleOwner) { adapter.submitList(it) }
        searchViewModel.error.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                searchViewModel.consumeError()
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
        searchViewModel.search(searchViewModel.query, service::searchPlayers)

        val searchBar = requireActivity().findViewById<View>(R.id.search_bar)
        if (searchBar != null) {
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
    }

    private fun showKeyboard() {
        val searchView = _binding?.searchView ?: return
        if (!isAdded) return
        searchView.requestFocus()
        val keyboard = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        keyboard?.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun showPlayerDetails(player: IPlayerBase, playerCard: View) {
        binding.searchView.clearFocus()
        val keyboard = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        keyboard?.hideSoftInputFromWindow(binding.searchView.windowToken, 0)

        parentFragmentManager.beginTransaction()
            .add(
                R.id.container,
                PlayerDetailsFragment.newInstance(player, playerCard.transitionName),
                PlayerDetailsFragment.TAG
            )
            .addToBackStack(PlayerDetailsFragment.TAG)
            .commit()
    }

    override fun onDestroyView() {
        binding.recyclerView.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
