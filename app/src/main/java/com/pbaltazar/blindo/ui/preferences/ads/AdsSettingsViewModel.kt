package com.pbaltazar.blindo.ui.preferences.ads

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.preferences.UserPreferences
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AdsSettingsViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val currentAdsConsent = MutableLiveData<AdsConsentStatus>()
    val adsConsentStatus: LiveData<AdsConsentStatus> get() = currentAdsConsent

    fun setConsentStatus(adsConsentStatus: AdsConsentStatus) {
        currentAdsConsent.postValue(adsConsentStatus)
    }

    fun updateAdsConsentStatus() = viewModelScope.launch(backgroundDispatcher) {
        try {
            setConsentStatus(AdsConsentStatus.Success(AdsManager.updateConsentStatus()))
        } catch (e: IllegalStateException) {
            setConsentStatus(AdsConsentStatus.Failure(e.localizedMessage ?: e.toString()))
        } catch (e: RuntimeException) {
            setConsentStatus(AdsConsentStatus.Failure(e.localizedMessage ?: e.toString()))
        }
    }

    fun showConsentForm() = viewModelScope.launch {
        try {
            setConsentStatus(AdsConsentStatus.Success(AdsManager.showConsentForm()))
        } catch (e: IllegalStateException) {
            setConsentStatus(AdsConsentStatus.Failure(e.localizedMessage ?: e.toString()))
        } catch (e: RuntimeException) {
            setConsentStatus(AdsConsentStatus.Failure(e.localizedMessage ?: e.toString()))
        }
    }

    fun getConsentChannelResponse() = viewModelScope.launch(backgroundDispatcher) {
        setConsentStatus(AdsConsentStatus.Success(AdsManager.getConsentChannelResponse()))
    }

    sealed class AdsConsentStatus {
        class Success(val status: AdsManager.ConsentStatus): AdsConsentStatus()
        class Failure(val reason: String): AdsConsentStatus()
    }
}
