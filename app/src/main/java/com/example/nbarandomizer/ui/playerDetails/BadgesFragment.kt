package com.example.nbarandomizer.ui.playerDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nbarandomizer.adapters.BadgesAdapter
import com.example.nbarandomizer.databinding.FragmentBadgesBinding
import com.example.nbarandomizer.viewModels.PlayerDetailsViewModel

class BadgesFragment : Fragment() {
    private var _binding: FragmentBadgesBinding? = null
    private val binding get() = _binding!!
    private val detailsViewModel: PlayerDetailsViewModel by viewModels({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBadgesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        detailsViewModel.details.observe(viewLifecycleOwner) { details ->
            if (details != null) {
                binding.badgesRecyclerView.layoutManager = LinearLayoutManager(context)
                binding.badgesRecyclerView.adapter = BadgesAdapter().apply {
                    badgeCollection = details.badges
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.badgesRecyclerView.adapter = null
        super.onDestroyView()
        _binding = null
    }
}
