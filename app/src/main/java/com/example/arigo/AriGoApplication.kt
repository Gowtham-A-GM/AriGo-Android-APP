package com.example.arigo

import android.app.Application
import com.example.arigo.di.AppContainer

class AriGoApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
