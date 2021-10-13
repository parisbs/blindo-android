package com.pbaltazar.blindo.ui.home

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    viewModel {
        HomeViewModel(
            get(),
            get(),
            get()
        )
    }
}
