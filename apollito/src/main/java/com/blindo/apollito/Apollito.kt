package com.blindo.apollito

import android.app.Application
import com.blindo.apollito.utils.ApollitoDebugger
import timber.log.Timber

class Apollito(application: Application) {

    companion object {
        private lateinit var instance: Apollito

        fun initialize(application: Application) {
            instance = Apollito(application)
        }
    }

    init {
        initializeLogger()
    }

    private fun initializeLogger() {
        Timber.plant(ApollitoDebugger())
    }
}
