package com.pbaltazar.blindo.ui.filter

import com.pbaltazar.blindo.ui.filter.apps.AppsFilterViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val filterModule = module {

    viewModel {
        AppsFilterViewModel(
            get()
        )
    }
}
