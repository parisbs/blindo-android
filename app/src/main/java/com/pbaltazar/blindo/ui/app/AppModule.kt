package com.pbaltazar.blindo.ui.app

import com.pbaltazar.blindo.ui.app.details.AppViewModel
import com.pbaltazar.blindo.ui.app.details.pages.packs.AppPacksViewModel
import com.pbaltazar.blindo.ui.app.details.pages.ratings.AppRatingsViewModel
import com.pbaltazar.blindo.ui.app.details.pages.statistics.AppStatisticsViewModel
import com.pbaltazar.blindo.ui.app.local.LocalAppsViewModel
import com.pbaltazar.blindo.utils.BACKGROUND_DISPATCHER
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val appModule = module {

    viewModel {
        AppViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel {
        AppStatisticsViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get()
        )
    }

    viewModel {
        AppPacksViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get(),
            get()
        )
    }

    viewModel {
        AppRatingsViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get(),
            get()
        )
    }

    viewModel {
        LocalAppsViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get()
        )
    }
}
