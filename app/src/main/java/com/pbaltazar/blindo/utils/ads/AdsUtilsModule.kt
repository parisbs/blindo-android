package com.pbaltazar.blindo.utils.ads

import com.pbaltazar.blindo.utils.BACKGROUND_DISPATCHER
import com.pbaltazar.blindo.utils.ads.ui.AdsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val adsUtilsModule = module {

    single {
        AdsManager(
            get(),
            get()
        )
    }

    viewModel {
        AdsViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get(),
            get()
        )
    }
}
