package com.pbaltazar.blindo.ui.tutorial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pbaltazar.blindo.utils.preferences.UserPreferences

class TutorialViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val currentStep = MutableLiveData<Int>()
    val step: LiveData<Int>
        get() = currentStep

    fun setStep(step: Int) {
        currentStep.postValue(step)
    }

    fun disableFirstRun() {
        userPreferences.disableFirstTime()
    }

    private val verifyPrivacyPolicy = MutableLiveData<Boolean>()
    val isPrivacyPolicyAccepted: LiveData<Boolean>
        get() = verifyPrivacyPolicy

    fun verifyIsPrivacyPolicyAccepted() {
        verifyPrivacyPolicy.postValue(userPreferences.isPrivacyPolicyAccepted())
    }

    fun acceptPrivacyPolicy() {
        userPreferences.acceptPrivacyPolicy()
    }
}
