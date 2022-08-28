package com.pbaltazar.blindo.utils.authentication

import android.accounts.AccountManager
import com.google.firebase.auth.FirebaseAuth
import com.pbaltazar.blindo.utils.BACKGROUND_DISPATCHER
import com.pbaltazar.blindo.utils.authentication.local.AuthenticationLocal
import com.pbaltazar.blindo.utils.authentication.local.BlindoAuthenticationLocal
import com.pbaltazar.blindo.utils.authentication.provider.AuthenticationProvider
import com.pbaltazar.blindo.utils.authentication.provider.FirebaseAuthenticationProvider
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticationViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val authenticationModule = module {

    single {
        AccountManager.get(
            get()
        )
    }

    single<AuthenticationLocal> {
        BlindoAuthenticationLocal(
            get(),
            get()
        )
    }

    single {
        FirebaseAuth.getInstance()
    }

    single<AuthenticationProvider> {
        FirebaseAuthenticationProvider(
            get(),
            get()
        )
    }

    viewModel {
        AuthenticationViewModel(
            get(named(BACKGROUND_DISPATCHER)),
        get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
}
