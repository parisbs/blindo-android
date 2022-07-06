package com.pbaltazar.blindo.utils.billing

import com.pbaltazar.blindo.utils.billing.ui.BillingViewModel
import com.pbaltazar.blindo.utils.BACKGROUND_DISPATCHER
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val billingModule = module {

    single<BillingManager> {
        PlayStoreBillingManager(
            get()
        )
    }

    viewModel {
        BillingViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get(),
            get(),
            get(),
            get()
        )
    }
}
