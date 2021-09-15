package com.pbaltazar.blindo.ui.splash

import androidx.lifecycle.ViewModel
import com.pbaltazar.blindo.utils.preferences.UserPreferences

class SplashViewModel(
    private val userPreferences: UserPreferences,
) : ViewModel() {

    val isFirstRun: Boolean get() = userPreferences.isFirstTime()

    val isPrivacyPolicyAccepted: Boolean get() = userPreferences.isPrivacyPolicyAccepted()
}
