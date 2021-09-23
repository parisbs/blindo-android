package com.pbaltazar.blindo.ui.user

import com.pbaltazar.blindo.ui.user.backup.BackupViewModel
import com.pbaltazar.blindo.ui.user.rating.UserRatingsViewModel
import com.pbaltazar.blindo.utils.BACKGROUND_DISPATCHER
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val userModule = module {

    viewModel {
        BackupViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get(),
            get(),
            get()
        )
    }

    viewModel {
        UserRatingsViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get()
        )
    }
}
