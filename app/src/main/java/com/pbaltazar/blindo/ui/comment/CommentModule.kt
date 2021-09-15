package com.pbaltazar.blindo.ui.comment

import com.pbaltazar.blindo.ui.comment.create.CommentCreatorViewModel
import com.pbaltazar.blindo.ui.comment.details.CommentDetailsViewModel
import com.pbaltazar.blindo.utils.BACKGROUND_DISPATCHER
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val commentModule = module {

    viewModel {
        CommentCreatorViewModel(
            get(named(BACKGROUND_DISPATCHER)),
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel {
        CommentDetailsViewModel()
    }
}
