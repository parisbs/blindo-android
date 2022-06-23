package com.pbaltazar.blindo.ui.vision

import com.pbaltazar.blindo.utils.BACKGROUND_DISPATCHER
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val visionResultsModule = module {

    viewModel {
        VisionResultsViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get(),
            get(),
            get()
        )
    }

}
