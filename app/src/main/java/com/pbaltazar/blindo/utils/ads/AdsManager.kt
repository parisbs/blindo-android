package com.pbaltazar.blindo.utils.ads

import android.content.Context
import android.os.Bundle
import com.google.ads.consent.*
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.pbaltazar.blindo.BuildConfig
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.entities.responses.AdsResponse
import com.pbaltazar.blindo.utils.constants.ADS_CLIENT_ID
import com.pbaltazar.blindo.utils.constants.NO_PERSONALIZED_ADS
import com.pbaltazar.blindo.utils.constants.NO_PERSONALIZED_ADS_NO
import com.pbaltazar.blindo.utils.constants.NO_PERSONALIZED_ADS_YES
import com.pbaltazar.blindo.utils.preferences.UserPreferences
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Suppress("unused")
class AdsManager(
    private val context: Context,
    private val userPreferences: UserPreferences
) {

    private val consentInformation: ConsentInformation = ConsentInformation.getInstance(context)
    private lateinit var consentForm: ConsentForm
    private var isInitialized: Boolean = false
    private var isAdsClientInitialized: Boolean = false

    private val consentStatusChannel: Channel<AdsResponse<ConsentStatus>> = Channel(Channel.UNLIMITED)
    val consentStatusFlow: Flow<AdsResponse<ConsentStatus>> get() = consentStatusChannel.receiveAsFlow()

    companion object {
        enum class ConsentStatus {
            PERSONALIZED,
            NON_PERSONALIZED,
            ADS_FREE,
            NON_REQUIRED,
            UNKNOWN,
            INTERNET_ERROR_OR_ADS_BLOCKER
        }
    }

    init {
        if (
            BuildConfig.DEBUG &&
            BuildConfig.TEST_DEVICE_ID.isEmpty().not() &&
            BuildConfig.DEBUG_GEOGRAPHY.isEmpty().not()
        ) {
            consentInformation.addTestDevice(BuildConfig.TEST_DEVICE_ID)
            consentInformation.debugGeography = DebugGeography.valueOf(BuildConfig.DEBUG_GEOGRAPHY)
        }
        isInitialized = true
    }

    fun isInitialized(): Boolean = isInitialized

    fun getConsentStatus(): ConsentStatus =
        userPreferences.getAdsConsentStatus()

    fun setConsentStatus(consentStatus: ConsentStatus, sendToChannel: Boolean = false) {
        if (isInitialized.not()) {
            consentStatusChannel.trySend(AdsResponse.Error(
                IllegalStateException("AdsManager should be initialized")
            ))
        }
        userPreferences.setAdsConsentStatus(consentStatus)
        when (consentStatus) {
            ConsentStatus.PERSONALIZED -> consentInformation.consentStatus = com.google.ads.consent.ConsentStatus.PERSONALIZED
            ConsentStatus.NON_PERSONALIZED -> consentInformation.consentStatus = com.google.ads.consent.ConsentStatus.NON_PERSONALIZED
            ConsentStatus.UNKNOWN -> consentInformation.consentStatus = com.google.ads.consent.ConsentStatus.UNKNOWN
            else -> Unit
        }
        if (sendToChannel) {
            consentStatusChannel.trySend(AdsResponse.Success(consentStatus))
        }
    }

    fun resetConsentStatus() =
        setConsentStatus(ConsentStatus.UNKNOWN, true)

    val isAccepted: Boolean get() {
        val consentStatus = getConsentStatus()
        return consentStatus != ConsentStatus.INTERNET_ERROR_OR_ADS_BLOCKER &&
            consentStatus != ConsentStatus.UNKNOWN ||
            consentStatus == ConsentStatus.NON_REQUIRED
    }

    fun updateConsentStatus() {
            if (isInitialized.not()) {
                consentStatusChannel.trySend(AdsResponse.Error(
                    IllegalStateException("AdsManager should be initialized")
                ))
            }
        consentInformation.requestConsentInfoUpdate(arrayOf(BuildConfig.PUBLISHER_ID), object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consentStatus: com.google.ads.consent.ConsentStatus) {
                val currentConsentStatus = if (consentInformation.isRequestLocationInEeaOrUnknown.not()) {
                    ConsentStatus.NON_REQUIRED
                } else {
                    when (consentStatus) {
                        com.google.ads.consent.ConsentStatus.UNKNOWN -> ConsentStatus.UNKNOWN
                        com.google.ads.consent.ConsentStatus.PERSONALIZED -> ConsentStatus.PERSONALIZED
                        com.google.ads.consent.ConsentStatus.NON_PERSONALIZED -> ConsentStatus.NON_PERSONALIZED
                    }
                }
                setConsentStatus(currentConsentStatus, true)
            }

            override fun onFailedToUpdateConsentInfo(reason: String?) {
                setConsentStatus(ConsentStatus.UNKNOWN)
                consentStatusChannel.trySend(AdsResponse.Error(
                    RuntimeException("Unable to get your consent status, please verify your internet connection and try again. $reason")
                ))
            }
        })
    }

    fun showConsentForm(context: Context) {
        if (consentInformation.isRequestLocationInEeaOrUnknown.not()) {
            setConsentStatus(ConsentStatus.NON_REQUIRED)
        } else {
            consentForm = ConsentForm.Builder(context, URL(context.getString(R.string.privacy_policy_link)))
                .withListener(object : ConsentFormListener() {
                    override fun onConsentFormLoaded() {
                        super.onConsentFormLoaded()
                        consentForm.show()
                    }

                    override fun onConsentFormError(reason: String) {
                        super.onConsentFormError(reason)
                        setConsentStatus(ConsentStatus.UNKNOWN)
                        consentStatusChannel.trySend(AdsResponse.Error(
                            RuntimeException("Unable to set ads consent. $reason")
                        ))
                    }

                    override fun onConsentFormClosed(consentStatus: com.google.ads.consent.ConsentStatus?, userPrefersAdFree: Boolean?) {
                        super.onConsentFormClosed(consentStatus, userPrefersAdFree)
                        val selectedConsentStatus = if (userPrefersAdFree == true) {
                            ConsentStatus.ADS_FREE
                        } else {
                            when (consentStatus!!) {
                                com.google.ads.consent.ConsentStatus.PERSONALIZED -> ConsentStatus.PERSONALIZED
                                com.google.ads.consent.ConsentStatus.NON_PERSONALIZED -> ConsentStatus.NON_PERSONALIZED
                                else -> ConsentStatus.UNKNOWN
                            }
                        }
                        setConsentStatus(selectedConsentStatus, true)
                    }
                })
                .withAdFreeOption()
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build()
            consentForm.load()
        }
    }

    suspend fun initializeAdsClient(): Boolean =
        suspendCoroutine { continuation ->
            if (BuildConfig.DEBUG && BuildConfig.TEST_DEVICE_ID.isEmpty().not()) {
                MobileAds.setRequestConfiguration(
                    RequestConfiguration.Builder()
                        .setTestDeviceIds(listOf(BuildConfig.TEST_DEVICE_ID))
                        .build()
                )
            }
            MobileAds.initialize(context) { initializationStatus ->
                isAdsClientInitialized = if (initializationStatus.adapterStatusMap.containsKey(ADS_CLIENT_ID)) {
                    initializationStatus.adapterStatusMap[ADS_CLIENT_ID]?.initializationState?.equals(AdapterStatus.State.READY)
                        ?: false
                } else {
                    false
                }
                continuation.resume(isAdsClientInitialized)
            }
        }

    fun isAdsClientInitialized(): Boolean = isAdsClientInitialized

    fun getAdRequest(): AdRequest =
        AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply {
            putString(
                NO_PERSONALIZED_ADS,
                if (getConsentStatus() == ConsentStatus.NON_PERSONALIZED)
                    NO_PERSONALIZED_ADS_YES
            else
                NO_PERSONALIZED_ADS_NO
            )
        }).build()

    suspend fun getInterstitialAd(
        context: Context,
        fullScreenContentCallback: FullScreenContentCallback
    ): InterstitialAd? {
        val interstitialAdChannel: Channel<InterstitialAd?> = Channel(Channel.UNLIMITED)
        InterstitialAd.load(
            context,
            context.getString(R.string.admob__install_pack_intersticial),
            getAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    super.onAdLoaded(ad)
                    ad.fullScreenContentCallback = fullScreenContentCallback
                    interstitialAdChannel.trySend(ad)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    interstitialAdChannel.trySend(null)
                }
            }
        )
        return interstitialAdChannel.receive()
    }

    fun getBannerAd(adView: AdView, adListener: AdListener): AdView {
        adView.loadAd(getAdRequest())
        adView.adListener = adListener
        return adView
    }
}
