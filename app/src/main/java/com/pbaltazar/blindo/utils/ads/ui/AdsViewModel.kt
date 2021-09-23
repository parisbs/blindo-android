package com.pbaltazar.blindo.utils.ads.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.preferences.UserPreferences
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AdsViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val userPreferences: UserPreferences
) : ViewModel() {

    fun initializeAdsManager(context: Context) =
        AdsManager.initialize(context, userPreferences)

    private val currentAdsConsent = MutableLiveData<AdsConsentStatus>()
    val adsConsentStatus: LiveData<AdsConsentStatus> get() = currentAdsConsent

    fun setConsentStatus(adsConsentStatus: AdsConsentStatus) {
        currentAdsConsent.postValue(adsConsentStatus)
    }

    fun resetConsentStatus() {
        AdsManager.consentStatus = AdsManager.ConsentStatus.UNKNOWN
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

    private val adsClientInitializationStatus = MutableLiveData<Boolean>()
    val isAdsClientInitialized: LiveData<Boolean> get() = adsClientInitializationStatus

    fun initializeAdsClient() = viewModelScope.launch {
        adsClientInitializationStatus.postValue(AdsManager.initializeAdsClient())
    }

    fun getAdRequestWithAdsPreferences(): AdRequest =
        AdsManager.getAdRequest()

    private val loadedInterstitialAd = MutableLiveData<InterstitialAd?>()
    val interstitialAd: LiveData<InterstitialAd?> get() = loadedInterstitialAd

    fun getInterstitialAd(fullScreenContentCallback: FullScreenContentCallback) = viewModelScope.launch {
        loadedInterstitialAd.postValue(AdsManager.getInterstitialAd(fullScreenContentCallback))
    }

    fun getBannerAd(adView: AdView, adListener: AdListener): AdView =
        AdsManager.getBannerAd(adView, adListener)
}
