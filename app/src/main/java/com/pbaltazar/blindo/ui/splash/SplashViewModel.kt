package com.pbaltazar.blindo.ui.splash

import androidx.lifecycle.ViewModel
import com.pbaltazar.blindo.utils.constants.IS_PRIVACY_POLICY_ACCEPTED
import com.pbaltazar.blindo.utils.constants.IS_VISION_INTRODUCED
import com.pbaltazar.blindo.utils.constants.REQUIRES_USER_DATA_UPDATE_BUILD_129
import com.pbaltazar.blindo.utils.preferences.UserPreferences

class SplashViewModel(
    private val userPreferences: UserPreferences,
) : ViewModel() {

    val isFirstRun: Boolean get() {
        return userPreferences.isFirstTime()
    }

    val requiresUserDataUpdateBuild129: Boolean get() {
        return userPreferences.getBoolean(REQUIRES_USER_DATA_UPDATE_BUILD_129, true)
    }

    val isPrivacyPolicyAccepted: Boolean get() {
        return userPreferences.isPrivacyPolicyAccepted()
    }

    val isVisionIntroduced: Boolean get() {
        return userPreferences.getBoolean(IS_VISION_INTRODUCED, false)
    }

    fun resetIsPrivacyPolicyAccepted() = userPreferences.setBoolean(IS_PRIVACY_POLICY_ACCEPTED, false)
}
