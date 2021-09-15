package com.pbaltazar.blindo.ui.sli

import com.pbaltazar.blindo.utils.BACKGROUND_DISPATCHER
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val sliModule = module {

    viewModel {
        SliViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get(),
            get(),
            get()
        )
    }
}
