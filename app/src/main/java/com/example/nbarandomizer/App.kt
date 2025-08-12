package com.example.nbarandomizer

import android.app.Application
import androidx.core.content.ContextCompat
import com.example.nbarandomizer.services.PlayersService

class App : Application() {
    lateinit var playersService: PlayersService

    override fun onCreate() {
        super.onCreate()

        playersService = PlayersService(applicationContext)
    }

    override fun onTerminate() {
        playersService.close()

        super.onTerminate()
    }
}