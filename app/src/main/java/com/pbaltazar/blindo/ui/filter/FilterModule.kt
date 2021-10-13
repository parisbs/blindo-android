package com.pbaltazar.blindo.ui.filter

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val filterModule = module {
    viewModel {
        FiltersViewModel(
            get()
        )
    }
}
