package com.pbaltazar.blindo.ui.permissions

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val permissionsModule = module {
    viewModel { PermissionsViewModel() }
}
