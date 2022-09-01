package com.pbaltazar.blindo.utils.ads.ui

import android.content.Context
import androidx.lifecycle.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.pbaltazar.blindo.entities.responses.AdsResponse
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.preferences.UserPreferences
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@Suppress("unused")
class AdsViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val adsManager: AdsManager,
    private val userPreferences: UserPreferences
) : ViewModel() {

    val adsConsentStatus: LiveData<AdsResponse<AdsManager.Companion.ConsentStatus>> = adsManager.consentStatusFlow.asLiveData()

    fun resetConsentStatus() =
        adsManager.resetConsentStatus()

    fun updateAdsConsentStatus() {
        adsManager.updateConsentStatus()
    }

    fun showConsentForm(context: Context) =
        adsManager.showConsentForm(context)

    private val _isAdsClientInitialized = MutableLiveData<Boolean>()
    val isAdsClientInitialized: LiveData<Boolean> get() = _isAdsClientInitialized

    private val maxRetryIterations: Int = 3
    private var retryIteration: Int = 0

    fun initializeAdsClient() = viewModelScope.launch {
        if (adsManager.isAdsClientInitialized().not()) {
            _isAdsClientInitialized.postValue(adsManager.initializeAdsClient())
        } else _isAdsClientInitialized.postValue(true)
    }

    fun isAdsClientInitialized(): Boolean = adsManager.isAdsClientInitialized()

    private fun canRetryToLoadAd(): Boolean =
        retryIteration < maxRetryIterations

    fun retryToLoadAds(loader: () -> Unit) {
        if (isAdsClientInitialized() && canRetryToLoadAd()) {
            retryIteration++
            loader()
        }
    }

    fun getAdRequestWithAdsPreferences(): AdRequest =
        adsManager.getAdRequest()

    fun getBannerAd(adView: AdView, adListener: AdListener): AdView =
        adsManager.getBannerAd(adView, adListener)
}
