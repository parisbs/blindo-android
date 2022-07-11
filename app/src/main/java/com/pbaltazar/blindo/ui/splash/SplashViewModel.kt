package com.pbaltazar.blindo.ui.splash

import androidx.lifecycle.ViewModel
import com.pbaltazar.blindo.utils.constants.IS_PRIVACY_POLICY_ACCEPTED
import com.pbaltazar.blindo.utils.constants.IS_VISION_INTRODUCED
import com.pbaltazar.blindo.utils.preferences.UserPreferences

class SplashViewModel(
    private val userPreferences: UserPreferences,
) : ViewModel() {

    val isFirstRun: Boolean get() = userPreferences.isFirstTime()

    val isPrivacyPolicyAccepted: Boolean get() = userPreferences.isPrivacyPolicyAccepted()

    val isVisionIntroduced: Boolean = userPreferences.getBoolean(IS_VISION_INTRODUCED, false)

    fun resetIsPrivacyPolicyAccepted() = userPreferences.setBoolean(IS_PRIVACY_POLICY_ACCEPTED, false)
}
