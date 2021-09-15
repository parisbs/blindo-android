package com.pbaltazar.blindo.utils.ads

import com.pbaltazar.blindo.utils.ads.settings.AdsSettingsViewModel
import com.pbaltazar.blindo.utils.BACKGROUND_DISPATCHER
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val adsModule = module {
    viewModel {
        AdsViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get()
        )
    }

    viewModel {
        AdsSettingsViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get()
        )
    }
}
