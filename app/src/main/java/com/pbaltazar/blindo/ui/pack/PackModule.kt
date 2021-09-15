package com.pbaltazar.blindo.ui.pack

import com.pbaltazar.blindo.ui.pack.details.PackDetailsViewModel
import com.pbaltazar.blindo.ui.pack.upload.UploadPackViewModel
import com.pbaltazar.blindo.utils.BACKGROUND_DISPATCHER
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val packModule = module {

    viewModel {
        PackDetailsViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get(),
            get()
        )
    }

    viewModel {
        UploadPackViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get(),
            get(),
            get()
        )
    }
}
