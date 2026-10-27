package com.example.nbarandomizer.ui

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.children
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.nbarandomizer.App
import com.example.nbarandomizer.R
import com.example.nbarandomizer.adapters.MainViewPagerAdapter
import com.example.nbarandomizer.databinding.ActivityMainBinding
import com.example.nbarandomizer.extensions.addNavigationIconGlow
import com.example.nbarandomizer.extensions.gone
import com.example.nbarandomizer.extensions.show
import com.example.nbarandomizer.models.Epoch
import com.example.nbarandomizer.models.Version2K
import com.example.nbarandomizer.services.PlayersService
import com.example.nbarandomizer.ui.search.SearchFragment
import com.example.nbarandomizer.viewModels.SharedViewModel
import com.example.nbarandomizer.viewModels.RandomizerViewModel
import com.example.nbarandomizer.viewModels.UiState
import com.google.android.material.color.MaterialColors
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var sharedViewModel: SharedViewModel
    private var navigationPosition = 0f
    private lateinit var randomizerGlow: Drawable
    private lateinit var rosterGlow: Drawable

    private val navigationPageCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            updateNavigationIndicator(position + positionOffset)
        }

        override fun onPageSelected(position: Int) {
            updateNavigation(position)
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (binding.randomizerNavigation.width == 0) return
            if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                binding.bottomNavigationIndicator.beginDrag(navigationIndicatorLeft(navigationPosition))
            } else {
                binding.bottomNavigationIndicator.animateTo(
                    navigationIndicatorLeft(binding.viewPager.currentItem.toFloat())
                )
                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    updateNavigationIndicator(binding.viewPager.currentItem.toFloat())
                }
            }
        }
    }

    private val navigationActiveColor by lazy {
        MaterialColors.getColor(binding.bottomNavigation, com.google.android.material.R.attr.colorPrimary)
    }

    private val navigationInactiveColor by lazy {
        MaterialColors.getColor(binding.bottomNavigation, com.google.android.material.R.attr.colorOnSurfaceVariant)
    }

    private val playersService: PlayersService
        get() = (applicationContext as App).playersService

    private val downloadingAnimator by lazy {
        createDownloadingAnimation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            binding.bottomNavigation.updateLayoutParams<ConstraintLayout.LayoutParams> {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.bottom_navigation_bottom_margin) + systemBars.bottom
            }
            insets
        }

        initializeViewPager()

        observeViewModelStates()

        binding.refreshBtn.setOnClickListener { downloadRoster() }

        setSearchListener()

        playersService.notifyProgressBar = sharedViewModel::onDetailsDownloaded

        val randomizerViewModel = ViewModelProvider(this)[RandomizerViewModel::class.java]
        sharedViewModel.selectedRosterBinding.observe(this) {
            randomizerViewModel.onRosterChanged(it, sharedViewModel.rosterRevision)
        }
        sharedViewModel.detailsProgress.observe(this) { binding.progressBar.progress = it }

        getNicknames()

        loadRosterAndDetailsIntoCache()
    }

    override fun onResume() {
        super.onResume()

        initializeSpinners()

        if (sharedViewModel.uiState.value is UiState.Idle)
            getRoster()
    }

    private fun setSearchListener() {
        binding.searchBar.setOnClickListener {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, SearchFragment(), "search")
                .addToBackStack("search")
                .commit()
        }
    }

    private fun tryLoadResourceFileIntoCache(fileName: String, resourceName: String) {
        val file = File(applicationContext.filesDir, fileName)

        if (!file.exists()) {
            val resourceId = applicationContext.resources.getIdentifier(
                resourceName,
                "raw",
                applicationContext.packageName
            )

            val inputStream = applicationContext.resources.openRawResource(resourceId)

            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    private fun loadRosterAndDetailsIntoCache() {
        Version2K.entries.forEach { version ->
            if (version == Version2K.latest())
                return@forEach

            Epoch.entries.forEach { epoch ->
                val rosterFileName = playersService.getRosterFileName(epoch, version)
                val detailsFileName = playersService.getDetailsFileName(epoch, version)

                tryLoadResourceFileIntoCache(rosterFileName, rosterFileName.substringBeforeLast("."))
                tryLoadResourceFileIntoCache(detailsFileName, detailsFileName.substringBeforeLast("."))
            }
        }
    }

    private fun getNicknames() {
        val file = File(applicationContext.filesDir, sharedViewModel.nicknamesFile)

        if (!file.exists()) {
            val inputStream = applicationContext.resources.openRawResource(R.raw.nicknames)

            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        sharedViewModel.getNicknames(file)
    }

    private fun initializeSpinners() {
        initializeSpinner(binding.epochSpinner, Epoch.entries.map { it.toString() })
        binding.epochSpinner.setText(sharedViewModel.epoch.toString(), false)

        initializeSpinner(binding.versionSpinner, Version2K.entries.map { it.toString() })
        binding.versionSpinner.setText(sharedViewModel.version.toString(), false)
    }

    private fun initializeSpinner(textView: AutoCompleteTextView, values: List<String>) {
        val adapter = ArrayAdapter(this, com.google.android.material.R.layout.support_simple_spinner_dropdown_item, values)

        textView.setAdapter(adapter)

        textView.setOnItemClickListener { _, _, _, _ -> getRoster() }
    }

    private fun initializeViewPager() {
        with(binding.viewPager) {
            offscreenPageLimit = 2
            adapter = MainViewPagerAdapter(this@MainActivity)
        }

        binding.bottomNavigation.setBlurSource(binding.viewPager)
        binding.bottomNavigationItems.children.forEach {
            it.isEnabled = it === binding.randomizerNavigation || it === binding.rosterNavigation
        }
        randomizerGlow = binding.randomizerNavigation.addNavigationIconGlow()
        rosterGlow = binding.rosterNavigation.addNavigationIconGlow()
        binding.bottomNavigationIndicator.onPositionChanged = ::updateNavigationColors
        binding.randomizerNavigation.setOnClickListener { binding.viewPager.currentItem = 0 }
        binding.rosterNavigation.setOnClickListener { binding.viewPager.currentItem = 1 }
        binding.viewPager.registerOnPageChangeCallback(navigationPageCallback)
        updateNavigation(binding.viewPager.currentItem)
        binding.bottomNavigation.doOnLayout {
            binding.bottomNavigationIndicator.snapTo(
                navigationIndicatorLeft(binding.viewPager.currentItem.toFloat())
            )
            updateNavigationIndicator(binding.viewPager.currentItem.toFloat())
        }
    }

    private fun updateNavigation(position: Int) {
        binding.randomizerNavigation.isSelected = position == 0
        binding.rosterNavigation.isSelected = position == 1
        if (binding.randomizerNavigation.width == 0) return
        if (binding.viewPager.scrollState == ViewPager2.SCROLL_STATE_IDLE) {
            binding.bottomNavigationIndicator.snapTo(navigationIndicatorLeft(position.toFloat()))
            updateNavigationIndicator(position.toFloat())
        } else if (binding.viewPager.scrollState == ViewPager2.SCROLL_STATE_SETTLING) {
            binding.bottomNavigationIndicator.animateTo(navigationIndicatorLeft(position.toFloat()))
        }
    }

    private fun navigationIndicatorLeft(position: Float): Float {
        val start = binding.randomizerNavigation.left
        val end = binding.rosterNavigation.left
        return start + (end - start) * position.coerceIn(0f, 1f)
    }

    private fun updateNavigationIndicator(position: Float) {
        navigationPosition = position.coerceIn(0f, 1f)
        if (binding.randomizerNavigation.width == 0) return

        val progress = navigationPosition
        if (binding.viewPager.scrollState == ViewPager2.SCROLL_STATE_DRAGGING) {
            binding.bottomNavigationIndicator.followDrag(
                navigationIndicatorLeft(progress),
                binding.randomizerNavigation.left.toFloat(),
                binding.rosterNavigation.left.toFloat()
            )
        }
    }

    private fun calculateGlowAlpha(progress: Float): Int {
        return (180 * (1f - progress)).toInt()
    }

    private fun updateNavigationColors(left: Float) {
        val start = binding.randomizerNavigation.left
        val end = binding.rosterNavigation.left
        val progress = if (start == end) 0f else ((left - start) / (end - start)).coerceIn(0f, 1f)
        binding.randomizerNavigation.imageTintList = ColorStateList.valueOf(
            ColorUtils.blendARGB(navigationActiveColor, navigationInactiveColor, progress)
        )
        binding.rosterNavigation.imageTintList = ColorStateList.valueOf(
            ColorUtils.blendARGB(navigationInactiveColor, navigationActiveColor, progress)
        )
        randomizerGlow.alpha = calculateGlowAlpha(progress)
        rosterGlow.alpha = calculateGlowAlpha(1f - progress)
    }

    private fun createDownloadingAnimation(): ObjectAnimator {
        return ObjectAnimator.ofFloat(binding.refreshBtn, View.ROTATION, 0f, 360f).apply {
            duration = 800
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
        }
    }

    private fun startDownloadingAnimation() {
        if (!downloadingAnimator.isRunning)
            downloadingAnimator.start()
    }

    private fun getSelectedEpoch(): Epoch {
        return Epoch.valueOf(binding.epochSpinner.text.toString())
    }

    private fun getSelectedVersion(): Version2K {
        return Version2K.valueOf("_${binding.versionSpinner.text}")
    }

    private fun setStatusText(text: String) {
        with(binding.statusTextView) {
            this.text = text
            show()
        }
    }

    private fun observeViewModelStates() {
        sharedViewModel.uiState.observe(this) {
            when(it) {
                is UiState.LoadingRoster -> {
                    startDownloadingAnimation()

                    setStatusText("Загрузка ростера...")
                    binding.progressBar.hide()
                }

                is UiState.SuccessRoster -> {
                    binding.statusTextView.gone()

                    downloadingAnimator.end()
                }

                is UiState.LoadingDetails -> {
                    startDownloadingAnimation()

                    setStatusText("Загрузка атрибутов игроков...")
                    binding.progressBar.show()
                }

                is UiState.SuccessDetails -> {
                    downloadingAnimator.end()

                    binding.progressBar.hide()
                    binding.statusTextView.gone()
                }

                is UiState.Error -> {
                    setStatusText(it.msg)

                    downloadingAnimator.end()

                    binding.progressBar.hide()
                }

                else -> Unit
            }
        }
    }

    private fun downloadRoster() {
        if (sharedViewModel.version == Version2K.latest())
            sharedViewModel.downloadRosterAndDetails(playersService, getSelectedEpoch())
    }

    private fun getRoster() {
        sharedViewModel.getRosterAndDetails(playersService, getSelectedEpoch(), getSelectedVersion())
    }

    override fun onDestroy() {
        binding.bottomNavigationIndicator.onPositionChanged = null
        binding.viewPager.unregisterOnPageChangeCallback(navigationPageCallback)
        downloadingAnimator.cancel()
        if (!isChangingConfigurations) playersService.notifyProgressBar = null

        super.onDestroy()
    }
}
