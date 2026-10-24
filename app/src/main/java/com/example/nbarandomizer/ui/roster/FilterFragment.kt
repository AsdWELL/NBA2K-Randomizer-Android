package com.example.nbarandomizer.ui.roster

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.nbarandomizer.R
import com.example.nbarandomizer.databinding.FragmentFilterBinding
import com.example.nbarandomizer.extensions.createEnterTransformation
import com.example.nbarandomizer.extensions.createReturnTransformation
import com.example.nbarandomizer.extensions.hide
import com.example.nbarandomizer.extensions.makeBlurBackground
import com.example.nbarandomizer.extensions.show
import com.example.nbarandomizer.models.FilterSettings
import com.example.nbarandomizer.models.Position
import com.example.nbarandomizer.models.SortingAttrs
import com.example.nbarandomizer.viewModels.SharedViewModel
import com.example.nbarandomizer.viewModels.FilterViewModel

class FilterFragment : Fragment() {
    companion object {
        const val TAG = "filter"
    }

    private var _binding: FragmentFilterBinding? = null

    private val binding get() = _binding!!

    private var _sortAscending = false

    private lateinit var sharedViewModel: SharedViewModel
    private val filterViewModel: FilterViewModel by viewModels()

    private fun animateAndChangeSortDirection() {
        _sortAscending = !_sortAscending

        with(binding.sortDirectionView) {
            isClickable = false

            animate()
                .rotationBy(180f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { isClickable = true }
                .start()
        }
    }

    private fun initializeSpinner(textView: AutoCompleteTextView, values: List<String>, addNoneValue: Boolean = true) {
        val adapter = ArrayAdapter(requireContext(),
            com.google.android.material.R.layout.support_simple_spinner_dropdown_item,
            if (addNoneValue)
                listOf(FilterSettings.FILTER_NONE_VALUE) + values
            else
                values)

        textView.setAdapter(adapter)
    }

    private fun initializeFilters() {
        val settings = filterViewModel.draft ?: sharedViewModel.filterSettings.copy().also {
            filterViewModel.draft = it
        }
        with(binding.filterNameView) {
            setText(settings.name)
        }

        with(binding.filterPositionView) {
            initializeSpinner(this, Position.entries.map { it.toString() })
            setText(settings.position, false)
        }

        with(binding.filterTeamView) {
            initializeSpinner(this, sharedViewModel.selectedRoster.map { it.team }.distinct().sorted())
            setText(settings.team, false)
        }

        with(binding.sortingView) {
            initializeSpinner(this, SortingAttrs.entries.map { it.title }, false)
            setText(settings.sortingAttr, false)
        }

        with(binding.sortDirectionView) {
            if (settings.sortAscending)
                this.rotation = 90f
            else
                this.rotation = -90f

            _sortAscending = settings.sortAscending
        }
    }

    private fun setOnClickListeners() {
        binding.sortDirectionView.setOnClickListener {
            animateAndChangeSortDirection()
        }

        binding.resetBtn.setOnClickListener {
            with(binding) {
                filterNameView.text?.clear()
                filterPositionView.setText(FilterSettings.FILTER_NONE_VALUE, false)
                filterTeamView.setText(FilterSettings.FILTER_NONE_VALUE, false)
                sortingView.setText(SortingAttrs.Overall.title, false)

                if (_sortAscending)
                    animateAndChangeSortDirection()
            }
        }

        binding.saveFiltersBtn.setOnClickListener {
            saveDraft()
            val draft = filterViewModel.draft!!
            sharedViewModel.filterSettings = draft.copy(name = draft.name.trim())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)

        postponeEnterTransition()

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        binding.root.setOnClickListener {}

        setOnClickListeners()
        
        initializeFilters()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.doOnPreDraw {
            startPostponedEnterTransition()
        }

        val searchButton = requireActivity().findViewById<View>(R.id.filter_button)

        if (searchButton != null) {
            enterTransition = createEnterTransformation(searchButton, binding.filterContainer) {
                searchButton.hide()
            }
            returnTransition = createReturnTransformation(binding.filterContainer, searchButton) {
                searchButton.show()
            }
        }

        makeBlurBackground()
    }

    private fun saveDraft() {
        filterViewModel.draft = FilterSettings(
            name = binding.filterNameView.text.toString(),
            team = binding.filterTeamView.text.toString(),
            position = binding.filterPositionView.text.toString(),
            sortingAttr = binding.sortingView.text.toString(),
            sortAscending = _sortAscending
        )
    }

    override fun onDestroyView() {
        saveDraft()
        binding.sortDirectionView.animate().cancel()
        super.onDestroyView()
        _binding = null
    }
}
