package com.example.nbarandomizer.ui.playerDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.nbarandomizer.adapters.AttributesAdapter
import com.example.nbarandomizer.databinding.FragmentAttributesBinding
import com.example.nbarandomizer.viewModels.PlayerDetailsViewModel

class AttributesFragment : Fragment() {
    private var _binding: FragmentAttributesBinding? = null
    private val binding get() = _binding!!
    private val detailsViewModel: PlayerDetailsViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAttributesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detailsViewModel.details.observe(viewLifecycleOwner) { details ->
            if (details != null) {
                binding.attributesRecyclerView.layoutManager = GridLayoutManager(context, 2)
                binding.attributesRecyclerView.adapter = AttributesAdapter().apply {
                    attributesCollections = details.attributes
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.attributesRecyclerView.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
