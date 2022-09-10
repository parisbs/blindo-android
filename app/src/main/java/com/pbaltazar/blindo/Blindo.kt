package com.pbaltazar.blindo

import android.app.Application
import com.pbaltazar.blindo.data.dataModule
import com.pbaltazar.blindo.ui.app.appModule
import com.pbaltazar.blindo.ui.filter.filterModule
import com.pbaltazar.blindo.ui.home.homeModule
import com.pbaltazar.blindo.ui.pack.packModule
import com.pbaltazar.blindo.ui.permissions.permissionsModule
import com.pbaltazar.blindo.ui.rating.ratingModule
import com.pbaltazar.blindo.ui.search.searchModule
import com.pbaltazar.blindo.ui.sli.sliModule
import com.pbaltazar.blindo.ui.splash.splashModule
import com.pbaltazar.blindo.ui.tutorial.tutorialModule
import com.pbaltazar.blindo.ui.user.userModule
import com.pbaltazar.blindo.ui.vision.visionResultsModule
import com.pbaltazar.blindo.usecases.useCasesModule
import com.pbaltazar.blindo.utils.ads.adsUtilsModule
import com.pbaltazar.blindo.utils.authentication.authenticationModule
import com.pbaltazar.blindo.utils.billing.billingModule
import com.pbaltazar.blindo.utils.log.BlindoDebugger
import com.pbaltazar.blindo.utils.mainModule
import com.pbaltazar.blindo.utils.messaging.messagingModule
import com.pbaltazar.blindo.utils.preferences.preferencesModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber


class Blindo : Application() {

    override fun onCreate() {
        super.onCreate()
        initializeLogger()
        initializeDependencyInjection(this)
    }

    private fun initializeLogger() {
        Timber.plant(BlindoDebugger())
    }

    private fun initializeDependencyInjection(application: Application) {
        startKoin {
            androidContext(application)
            androidLogger(
                if (BuildConfig.DEBUG) Level.ERROR else Level.NONE
            )
            androidFileProperties()
            modules(
                listOf(
                    mainModule,
                    adsUtilsModule,
                    messagingModule,
                    preferencesModule,
                    dataModule,
                    useCasesModule,
                    billingModule,
                    authenticationModule,
                    searchModule,
                    permissionsModule,
                    visionResultsModule,
                    splashModule,
                    tutorialModule,
                    userModule,
                    filterModule,
                    homeModule,
                    appModule,
                    sliModule,
                    packModule,
                    ratingModule
                )
            )
        }
    }
}
