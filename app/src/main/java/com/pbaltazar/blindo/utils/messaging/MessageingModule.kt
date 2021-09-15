package com.pbaltazar.blindo.utils.messaging

import com.pbaltazar.blindo.utils.BACKGROUND_DISPATCHER
import com.pbaltazar.blindo.utils.messaging.ui.MessagingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val messagingModule = module {
    viewModel {
        MessagingViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get()
        )
    }
}
