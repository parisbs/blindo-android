package com.pbaltazar.blindo.utils.preferences

import androidx.preference.PreferenceManager
import org.koin.dsl.module

val preferencesModule = module {

    single {
        PreferenceManager.getDefaultSharedPreferences(
            get()
        )
    }

    single<UserPreferences> {
        BlindoPreferences(
            get()
        )
    }
}
