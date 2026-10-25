package com.example.nbarandomizer.ui.playerDetails

import android.os.Bundle
import android.transition.TransitionSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import com.example.nbarandomizer.App
import com.example.nbarandomizer.R
import com.example.nbarandomizer.adapters.PlayerDetailsViewPagerAdapter
import com.example.nbarandomizer.databinding.FragmentPlayerDetailsBinding
import com.example.nbarandomizer.extensions.createEnterTransformation
import com.example.nbarandomizer.extensions.createReturnTransformation
import com.example.nbarandomizer.extensions.findByTransitionName
import com.example.nbarandomizer.extensions.gone
import com.example.nbarandomizer.extensions.show
import com.example.nbarandomizer.extensions.submitOnEnter
import com.example.nbarandomizer.models.IPlayerBase
import com.example.nbarandomizer.models.PlayerDetails
import com.example.nbarandomizer.models.SearchPlayerResult
import com.example.nbarandomizer.viewModels.PlayerDetailsViewModel
import com.example.nbarandomizer.viewModels.SharedViewModel
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.serialization.json.Json
import java.io.File
import android.widget.Toast
import kotlin.math.abs

class PlayerDetailsFragment : Fragment() {
    companion object {
        const val TAG = "details"
        private const val ARG_TRANSITION_NAME = "sourceTransitionName"

        fun newInstance(
            player: IPlayerBase,
            transitionName: String?,
            details: PlayerDetails? = null,
            nicknameEditable: Boolean = true
        ) = PlayerDetailsFragment().apply {
            val preview = SearchPlayerResult(
                player.id, player.name, player.team, player.overall, player.url, player.photoUrl
            )
            arguments = bundleOf(
                PlayerDetailsViewModel.ARG_PLAYER to Json.encodeToString(preview),
                PlayerDetailsViewModel.ARG_DETAILS to details?.let { Json.encodeToString(it) },
                PlayerDetailsViewModel.ARG_NICKNAME to details?.nickname,
                PlayerDetailsViewModel.ARG_NICKNAME_EDITABLE to nicknameEditable,
                ARG_TRANSITION_NAME to transitionName
            )
        }
    }

    private var _binding: FragmentPlayerDetailsBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val detailsViewModel: PlayerDetailsViewModel by viewModels()
    private var tabMediator: TabLayoutMediator? = null
    private var enterFinished = false
    private val readyPages = mutableSetOf<Class<out Fragment>>()

    private fun setMainInfo() {
        val player = detailsViewModel.player
        with(binding) {
            listOf(photo, smallPhoto).forEach {
                Glide.with(root)
                    .load(player.photoUrl)
                    .signature(ObjectKey("${player.team}_${player.photoUrl}"))
                    .circleCrop()
                    .into(it)
            }
            name.text = player.name
            smallName.text = detailsViewModel.nicknameDraft.ifBlank { player.name }
            team.text = "Team: ${player.team}"
            overallCardView.bind(player.overall)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        readyPages.clear()
        _binding = FragmentPlayerDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        enterFinished = savedInstanceState != null
        setMainInfo()
        if (!detailsViewModel.nicknameEditable) binding.nickname.gone()
        binding.nickname.setText(detailsViewModel.nicknameDraft)
        binding.nickname.doAfterTextChanged {
            detailsViewModel.nicknameDraft = it?.toString().orEmpty()
            binding.smallName.text = detailsViewModel.nicknameDraft.ifBlank { detailsViewModel.player.name }
        }
        binding.nickname.submitOnEnter(EditorInfo.IME_ACTION_DONE)
        binding.backBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.appBarLayout.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val total = appBarLayout.totalScrollRange
            val percentage = if (total == 0) 0f else abs(verticalOffset).toFloat() / total
            binding.expandedHeader.alpha = (1f - percentage * 1.5f).coerceIn(0f, 1f)
            binding.collapsedHeader.alpha = percentage
        }

        detailsViewModel.details.observe(viewLifecycleOwner) { details ->
            if (details == null) {
                binding.nickname.gone()
                return@observe
            }
            if (detailsViewModel.nicknameEditable) binding.nickname.show()
            if (binding.nickname.text.toString() != detailsViewModel.nicknameDraft) {
                binding.nickname.setText(detailsViewModel.nicknameDraft)
            }
            binding.height.text = "Height: ${details.height}cm"
            binding.position.text = "Position: ${details.position}"
            binding.height.show()
            binding.position.show()
            if (binding.viewPager.adapter == null) {
                binding.viewPager.offscreenPageLimit = 2
                binding.viewPager.adapter = PlayerDetailsViewPagerAdapter(this)
                tabMediator = TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
                    tab.text = if (position == 0) "Attributes" else "Badges"
                }.also { it.attach() }
            }
            if (enterFinished) binding.viewPager.show()
        }
        detailsViewModel.error.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                detailsViewModel.consumeError()
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                if (!parentFragmentManager.isStateSaved &&
                    parentFragmentManager.fragments.lastOrNull { it.isVisible } === this) {
                    parentFragmentManager.popBackStack()
                }
            }
        }
        val service = (requireActivity().application as App).playersService
        detailsViewModel.loadDetails(service::downloadLatest2KVersionPlayerDetails)

        configureTransitions()
        view.doOnPreDraw { startPostponedEnterTransition() }
    }

    internal fun onPageReady(page: Fragment) {
        val binding = _binding ?: return
        readyPages.add(page.javaClass)
        if (readyPages.size == binding.viewPager.adapter?.itemCount) {
            binding.progressBar.gone()
        }
    }

    private fun configureTransitions() {
        val transitionName = arguments?.getString(ARG_TRANSITION_NAME)
        binding.root.transitionName = transitionName
        val source = transitionName?.let {
            requireActivity().findViewById<View>(R.id.container).findByTransitionName(it, binding.root)
        }
        if (source == null) {
            enterFinished = true
            if (detailsViewModel.details.value != null) binding.viewPager.show()
            return
        }
        val sourcePhoto = source.findViewById<View>(R.id.photo)
        enterTransition = TransitionSet().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
            addTransition(createEnterTransformation(source, binding.root) {
                enterFinished = true
                if (detailsViewModel.details.value != null) _binding?.viewPager?.show()
            })
            if (sourcePhoto != null) {
                addTransition(createEnterTransformation(sourcePhoto, binding.photoContainer))
            }
        }
        returnTransition = TransitionSet().apply {
            ordering = TransitionSet.ORDERING_TOGETHER
            addTransition(createReturnTransformation(binding.root, source))
            if (sourcePhoto != null) {
                addTransition(createReturnTransformation(binding.photoContainer, sourcePhoto))
            }
        }
    }

    private fun saveNickname() {
        if (!detailsViewModel.nicknameEditable) return
        val details = detailsViewModel.details.value ?: return
        val nickname = detailsViewModel.nicknameDraft.trim().ifEmpty { null }
        if (details.nickname != nickname) {
            sharedViewModel.setNickname(details.name, nickname)
            sharedViewModel.saveNicknames(File(requireContext().filesDir, sharedViewModel.nicknamesFile))
        }
    }

    override fun onDestroyView() {
        if (isRemoving || activity?.isFinishing == true) saveNickname()
        tabMediator?.detach()
        tabMediator = null
        binding.viewPager.adapter = null
        readyPages.clear()
        super.onDestroyView()
        _binding = null
    }
}
