package com.pbaltazar.blindo.ui.user

import com.pbaltazar.blindo.ui.user.backup.BackupViewModel
import com.pbaltazar.blindo.ui.user.comment.UserCommentsViewModel
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
        UserCommentsViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get()
        )
    }
}
