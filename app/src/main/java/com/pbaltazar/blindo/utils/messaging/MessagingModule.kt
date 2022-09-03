package com.pbaltazar.blindo.utils.messaging

import com.google.firebase.messaging.FirebaseMessaging
import com.pbaltazar.blindo.utils.messaging.ui.MessagingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val messagingModule = module {

    single {
        FirebaseMessaging.getInstance()
    }

    single {
        MessagingManager(
            get(),
            get()
        )
    }

    viewModel {
        MessagingViewModel(
            get()
        )
    }
}
