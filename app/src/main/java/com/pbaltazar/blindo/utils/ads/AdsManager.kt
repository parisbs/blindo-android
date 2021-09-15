package com.pbaltazar.blindo.utils.ads

import android.content.Context
import android.os.Bundle
import com.google.ads.consent.*
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.pbaltazar.blindo.BuildConfig
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.utils.preferences.UserPreferences
import com.pbaltazar.blindo.utils.constants.*
import kotlinx.coroutines.channels.Channel
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object AdsManager {

    private lateinit var context: Context
    private lateinit var userPreferences: UserPreferences
    private lateinit var consentInformation: ConsentInformation
    private lateinit var consentForm: ConsentForm
    private var isInitialized: Boolean = false
    private var isAdsClientInitialized: Boolean = false

    private val consentChannel: Channel<ConsentStatus> = Channel(Channel.UNLIMITED)

    enum class ConsentStatus {
        PERSONALIZED,
        NON_PERSONALIZED,
        ADS_FREE,
        NON_REQUIRED,
        UNKNOWN,
        INTERNET_ERROR_OR_ADS_BLOCKER
    }

    @JvmStatic
    fun initialize(context: Context, userPreferences: UserPreferences) {
        this.context = context
        this.userPreferences = userPreferences
        consentInformation = ConsentInformation.getInstance(this.context)
        if (
            BuildConfig.DEBUG &&
                BuildConfig.TEST_DEVICE_ID.isNullOrEmpty().not() &&
                BuildConfig.DEBUG_GEOGRAPHY.isNullOrEmpty().not()
        ) {
            consentInformation.addTestDevice(BuildConfig.TEST_DEVICE_ID)
            consentInformation.debugGeography = DebugGeography.valueOf(BuildConfig.DEBUG_GEOGRAPHY)
        }
        isInitialized = true
    }

    @JvmStatic
    fun isInitialized(): Boolean = isInitialized

    @JvmStatic
    suspend fun initializeAdsClient(): Boolean =
        suspendCoroutine<Boolean> { continuation ->
        MobileAds.initialize(context, object : OnInitializationCompleteListener {
            override fun onInitializationComplete(initializationStatus: InitializationStatus) {
                if (initializationStatus.adapterStatusMap.containsKey(ADS_CLIENT_ID)) {
                    if (initializationStatus.adapterStatusMap[ADS_CLIENT_ID]?.initializationState?.equals(AdapterStatus.State.READY) ?: false) {
                        isAdsClientInitialized = true
                    } else {
                        isAdsClientInitialized = false
                    }
                } else {
                    isAdsClientInitialized = false
                }
                continuation.resume(isAdsClientInitialized)
            }
        })
    }

    @JvmStatic
    fun isAdsClientInitialized(): Boolean = isAdsClientInitialized

    @JvmStatic
    var consentStatus: ConsentStatus
        get() = userPreferences.getAdsConsentStatus()
    set(value) {
        if (isInitialized.not()) {
            throw IllegalStateException("AdsManager should be initialized")
        }
        userPreferences.setAdsConsentStatus(value)
        when (value) {
            ConsentStatus.PERSONALIZED -> consentInformation.consentStatus = com.google.ads.consent.ConsentStatus.PERSONALIZED
            ConsentStatus.NON_PERSONALIZED -> consentInformation.consentStatus = com.google.ads.consent.ConsentStatus.NON_PERSONALIZED
            else -> consentInformation.consentStatus = com.google.ads.consent.ConsentStatus.UNKNOWN
        }
    }

    @JvmStatic
    val isAccepted: Boolean get() =
        consentStatus.equals(ConsentStatus.INTERNET_ERROR_OR_ADS_BLOCKER) ||
            consentStatus.equals(ConsentStatus.UNKNOWN).not() ||
            consentStatus.equals(ConsentStatus.NON_REQUIRED)

    @JvmStatic
    suspend fun updateConsentStatus(): ConsentStatus {
            if (isInitialized.not()) {
                throw IllegalStateException("AdsManager should be initialized")
            }
        consentInformation.requestConsentInfoUpdate(arrayOf(BuildConfig.PUBLISHER_ID), object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consentStatus: com.google.ads.consent.ConsentStatus) {
                if (consentInformation.isRequestLocationInEeaOrUnknown.not()) {
                    userPreferences.setAdsConsentStatus(ConsentStatus.NON_REQUIRED)
                } else {
                    when (consentStatus) {
                        com.google.ads.consent.ConsentStatus.UNKNOWN -> userPreferences.setAdsConsentStatus(ConsentStatus.UNKNOWN)
                        com.google.ads.consent.ConsentStatus.PERSONALIZED -> userPreferences.setAdsConsentStatus(ConsentStatus.PERSONALIZED)
                        com.google.ads.consent.ConsentStatus.NON_PERSONALIZED -> userPreferences.setAdsConsentStatus(ConsentStatus.NON_PERSONALIZED)
                    }
                }
                consentChannel.offer(this@AdsManager.consentStatus)
            }

            override fun onFailedToUpdateConsentInfo(reason: String?) {
                userPreferences.setAdsConsentStatus(ConsentStatus.UNKNOWN)
                throw RuntimeException("Unable to get your consent status, please verify your internet connection and try again")
            }
        })
        return consentChannel.receive()
    }

    @JvmStatic
    suspend fun showConsentForm(): ConsentStatus =
        suspendCoroutine<ConsentStatus> { continuation ->
        if (consentInformation.isRequestLocationInEeaOrUnknown.not()) {
            userPreferences.setAdsConsentStatus(ConsentStatus.NON_REQUIRED)
            continuation.resume(consentStatus)
        } else {
            consentForm = ConsentForm.Builder(this.context, URL(context.getString(R.string.privacy_policy_link)))
                .withListener(object : ConsentFormListener() {
                    override fun onConsentFormLoaded() {
                        super.onConsentFormLoaded()
                        consentForm.show()
                    }

                    override fun onConsentFormError(reason: String) {
                        super.onConsentFormError(reason)
                        userPreferences.setAdsConsentStatus(ConsentStatus.UNKNOWN)
                        throw RuntimeException(reason)
                    }

                    override fun onConsentFormClosed(consentStatus: com.google.ads.consent.ConsentStatus?, userPrefersAdFree: Boolean?) {
                        super.onConsentFormClosed(consentStatus, userPrefersAdFree)
                        if (userPrefersAdFree ?: false) {
                            userPreferences.setAdsConsentStatus(ConsentStatus.ADS_FREE)
                        } else {
                            when (consentStatus!!) {
                                com.google.ads.consent.ConsentStatus.PERSONALIZED -> this@AdsManager.consentStatus = ConsentStatus.PERSONALIZED
                                com.google.ads.consent.ConsentStatus.NON_PERSONALIZED -> this@AdsManager.consentStatus = ConsentStatus.NON_PERSONALIZED
                                else -> Unit
                            }
                        }
                        continuation.resume(this@AdsManager.consentStatus)
                    }
                })
                .withAdFreeOption()
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build()
            consentForm.load()
        }
    }

    @JvmStatic
    suspend fun getConsentChannelResponse(): ConsentStatus = consentChannel.receive()

    @JvmStatic
    fun getAdRequest(): AdRequest =
        AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply {
            putString(
                NO_PERSONALIZED_ADS,
                if (consentStatus.equals(ConsentStatus.NON_PERSONALIZED))
                    NO_PERSONALIZED_ADS_YES
            else
                NO_PERSONALIZED_ADS_NO
            )
        }).build()

    @JvmStatic
    suspend fun getInterstitialAd(
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
                    val interstitialAd = ad
                    interstitialAd.fullScreenContentCallback = fullScreenContentCallback
                    interstitialAdChannel.offer(interstitialAd)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    interstitialAdChannel.offer(null)
                }
            }
        )
        return interstitialAdChannel.receive()
    }

    @JvmStatic
    fun getBannerAd(adView: AdView, adListener: AdListener): AdView {
        adView.loadAd(getAdRequest())
        adView.adListener = adListener
        return adView
    }
}
