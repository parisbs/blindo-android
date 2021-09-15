package com.pbaltazar.blindo.ui.tutorial

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val tutorialModule = module {

    viewModel {
        TutorialViewModel(
            get()
        )
    }

}
