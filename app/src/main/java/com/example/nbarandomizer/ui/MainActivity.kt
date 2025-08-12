package com.example.nbarandomizer.ui

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.nbarandomizer.App
import com.example.nbarandomizer.R
import com.example.nbarandomizer.adapters.MainViewPagerAdapter
import com.example.nbarandomizer.databinding.ActivityMainBinding
import com.example.nbarandomizer.extensions.gone
import com.example.nbarandomizer.extensions.show
import com.example.nbarandomizer.models.Epoch
import com.example.nbarandomizer.models.Version2K
import com.example.nbarandomizer.services.PlayersService
import com.example.nbarandomizer.ui.search.SearchFragment
import com.example.nbarandomizer.viewModels.SharedViewModel
import com.example.nbarandomizer.viewModels.UiState
import com.google.android.material.tabs.TabLayoutMediator
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var sharedViewModel: SharedViewModel

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
            insets
        }

        initializeViewPager()

        observeViewModelStates()

        binding.refreshBtn.setOnClickListener { downloadRoster() }

        setSearchListener()

        playersService.notifyProgressBar = { binding.progressBar.progress++ }

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

        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = "Randomizer"
                1 -> tab.text = "Roster"
            }
        }.attach()
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
                    binding.progressBar.progress = 0
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
        sharedViewModel.stopDownloadingJob()

        super.onDestroy()
    }
}