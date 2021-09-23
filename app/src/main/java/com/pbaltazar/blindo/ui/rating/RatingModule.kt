package com.pbaltazar.blindo.ui.rating

import com.pbaltazar.blindo.ui.rating.create.RatingCreatorViewModel
import com.pbaltazar.blindo.ui.rating.details.RatingDetailsViewModel
import com.pbaltazar.blindo.utils.BACKGROUND_DISPATCHER
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val ratingModule = module {

    viewModel {
        RatingCreatorViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel {
        RatingDetailsViewModel()
    }
}
