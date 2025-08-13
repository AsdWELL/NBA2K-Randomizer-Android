package com.example.nbarandomizer.ui.playerDetails

import android.os.Bundle
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.nbarandomizer.R
import com.example.nbarandomizer.adapters.PlayerDetailsViewPagerAdapter
import com.example.nbarandomizer.databinding.FragmentPlayerDetailsBinding
import com.example.nbarandomizer.extensions.createEnterTransformation
import com.example.nbarandomizer.extensions.createReturnTransformation
import com.example.nbarandomizer.extensions.gone
import com.example.nbarandomizer.extensions.show
import com.example.nbarandomizer.listeners.IPageReadyListener
import com.example.nbarandomizer.models.IPlayerBase
import com.example.nbarandomizer.models.PlayerDetails
import com.example.nbarandomizer.ui.providers.CardOutlineProvider
import com.example.nbarandomizer.viewModels.SharedViewModel
import com.google.android.material.tabs.TabLayoutMediator

class PlayerDetailsFragment(
    private var playerBase: IPlayerBase,
    private val playerCard: View
) : Fragment() {
    private var _binding: FragmentPlayerDetailsBinding? = null

    private val binding get() = _binding!!

    private var loadedPages = 0

    private val totalPages = 2

    var playerDetails: PlayerDetails?
        get() = playerBase as? PlayerDetails
        set(value) {
            if (value != null) {
                playerBase = value

                setDetails()
            }
        }

    private lateinit var sharedViewModel: SharedViewModel

    private fun setupCard(card: CardView) {
        card.cardElevation = 5f
        card.preventCornerOverlap = false
        card.clipToOutline = false
        card.outlineProvider = CardOutlineProvider(card)
    }

    private fun setCardColor(card: CardView, color: Int) {
        card.setCardBackgroundColor(color)
        card.outlineSpotShadowColor = color
    }

    private fun setMainInfo() {
        with(binding) {
            Glide.with(root)
                .load(playerBase.photoUrl)
                .signature(ObjectKey("${playerBase.team}_${playerBase.photoUrl}"))
                .circleCrop()
                .into(photo)

            name.text = playerBase.name

            team.text = "Team: ${playerBase.team}"

            overallTextView.text = playerBase.overall.value.toString()
            setupCard(overallCardView)
            setCardColor(overallCardView, playerBase.overall.color)
        }
    }

    private fun setDetails() {
        with(binding) {
            height.text = "Height: ${playerDetails!!.height}cm"
            position.text = "Position: ${playerDetails!!.position}"

            initializeViewPager()
        }
    }

    private fun initializeViewPager() {
        with(binding.viewPager) {
            offscreenPageLimit = 2
            adapter = PlayerDetailsViewPagerAdapter(
                requireActivity(),
                playerDetails!!,
                object : IPageReadyListener {
                    override fun onPageLoad() {
                        loadedPages++

                        if (loadedPages == totalPages)
                            binding.progressBar.gone()
                    }
                })
        }

        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = "Attributes"
                1 -> tab.text = "Badges"
            }
        }.attach()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerDetailsBinding.inflate(inflater, container, false)

        postponeEnterTransition()

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        setMainInfo()

        if (playerDetails != null)
            setDetails()

        binding.backBtn.setOnClickListener { requireActivity().supportFragmentManager.popBackStack() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.transitionName = playerCard.transitionName

        view.doOnPreDraw {
            startPostponedEnterTransition()
        }

        enterTransition = TransitionSet().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
            addTransition(createEnterTransformation(playerCard, binding.root) {
                _binding?.viewPager?.show()
            })
            addTransition(createEnterTransformation(playerCard.findViewById(R.id.photo), binding.photoContainer))
        }

        returnTransition = TransitionSet().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
            addTransition(createReturnTransformation(binding.root, playerCard))
            addTransition(createReturnTransformation(binding.photoContainer, playerCard.findViewById(R.id.photo)))
        }
    }

    override fun onDestroy() {
        _binding = null

        super.onDestroy()
    }
}