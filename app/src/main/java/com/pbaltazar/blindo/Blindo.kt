package com.pbaltazar.blindo

import android.app.Application
import com.pbaltazar.blindo.utils.ads.adsModule
import com.pbaltazar.blindo.ui.app.appModule
import com.pbaltazar.blindo.utils.billing.billingModule
import com.pbaltazar.blindo.ui.comment.commentModule
import com.pbaltazar.blindo.data.dataModule
import com.pbaltazar.blindo.ui.filter.filterModule
import com.pbaltazar.blindo.ui.home.homeModule
import com.pbaltazar.blindo.ui.pack.packModule
import com.pbaltazar.blindo.utils.preferences.preferencesModule
import com.pbaltazar.blindo.ui.sli.sliModule
import com.pbaltazar.blindo.ui.splash.splashModule
import com.pbaltazar.blindo.ui.tutorial.tutorialModule
import com.pbaltazar.blindo.usecases.useCasesModule
import com.pbaltazar.blindo.utils.authentication.authenticationModule
import com.pbaltazar.blindo.ui.user.userModule
import com.pbaltazar.blindo.utils.log.BlindoDebuger
import com.pbaltazar.blindo.utils.mainModule
import com.pbaltazar.blindo.utils.messaging.messagingModule
import com.wizeline.simpleapollo.SimpleApollo
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber


class Blindo : Application() {

    override fun onCreate() {
        super.onCreate()
        initializePlugins(this)
        initializeLogger()
        initializeDependencyInjection(this)
    }

    private fun initializePlugins(application: Application) {
        SimpleApollo.initialize(application)
    }

    private fun initializeLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(BlindoDebuger())
        }
    }

    private fun initializeDependencyInjection(application: Application) {
        startKoin {
            androidContext(application)
            androidLogger()
            androidFileProperties()
            modules(
                listOf(
                    mainModule,
                    adsModule,
                    messagingModule,
                    preferencesModule,
                    dataModule,
                    useCasesModule,
                    billingModule,
                    authenticationModule,
                    splashModule,
                    tutorialModule,
                    userModule,
                    filterModule,
                    homeModule,
                    appModule,
                    sliModule,
                    packModule,
                    commentModule
                )
            )
        }
    }
}
