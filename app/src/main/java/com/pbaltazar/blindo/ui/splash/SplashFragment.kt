package com.pbaltazar.blindo.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.pbaltazar.blindo.MainNavigationDirections
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentSplashBinding
import com.pbaltazar.blindo.entities.Device
import com.pbaltazar.blindo.entities.responses.AdsResponse
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.ads.ui.AdsViewModel
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticationViewModel
import com.pbaltazar.blindo.utils.billing.ui.BilleableFragment
import com.pbaltazar.blindo.utils.billing.ui.BillingViewModel
import com.pbaltazar.blindo.utils.extensions.isActive
import com.pbaltazar.blindo.utils.log.BlindoLogger
import com.pbaltazar.blindo.utils.messaging.ui.MessagingViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : BilleableFragment<FragmentSplashBinding>() {

    private val splashViewModel: SplashViewModel by viewModel()
    private val messagingViewModel: MessagingViewModel by sharedViewModel()
    private val adsViewModel: AdsViewModel by sharedViewModel()

    private lateinit var loadingText: TextView

    private var isInitFlowInitialized: Boolean = false

    private var pendingMessagingToken: String = ""

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeUser()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        loadingText = binding!!.loadingText
        return binding!!.root
    }

    override fun onResume() {
        super.onResume()
if (isInitFlowInitialized.not()) {
            isInitFlowInitialized = true
            subscribeBillingConnection()
        }
    }

    override fun onBillingConnection(billingConnection: BillingViewModel.BillingConnection) {
        when (billingConnection) {
            is BillingViewModel.BillingConnection.Connected -> {
                subscribeAuthentication()
                authenticateUser()
            }
            is BillingViewModel.BillingConnection.Disconnected -> showErrorLoading(getString(R.string.membership__billing_disconnected))
            is BillingViewModel.BillingConnection.Error -> showErrorLoading(billingConnection.reason)
            is BillingViewModel.BillingConnection.ServiceUnavailable -> showErrorLoading(getString(R.string.membership__billing_service_unavailable))
            is BillingViewModel.BillingConnection.FeatureNotSupported -> showErrorLoading(getString(R.string.membership__billing_feature_not_supported))
        }
    }

    override fun onSubscribeAuthentication(userAuthentication: AuthenticationViewModel.UserAuthentication) {
        when (userAuthentication) {
            is AuthenticationViewModel.UserAuthentication.Success -> {
                subscribeMessagingToken()
                messagingViewModel.getDeviceMessagingToken()
            }
            else -> {
                subscribeAdsConsentStatus()
                adsViewModel.updateAdsConsentStatus()
            }
        }
    }

    private fun subscribeMessagingToken() = messagingViewModel.messagingToken.observe(this) {
        it?.also { messagingToken ->
            if (messagingToken != getLatestStoragedDeviceMessagingToken()) {
                pendingMessagingToken = messagingToken
                subscribeDeviceUpdates()
                updateDevice(Device(gcmToken = pendingMessagingToken))
            } else {
                subscribeMembership()
                getMembership()
            }
        } ?: showErrorLoading("Unable to get messaging token")
    }

    override fun onSubscribeDeviceUpdates(deviceUpdate: AuthenticationViewModel.DeviceUpdate) {
        when (deviceUpdate) {
            is AuthenticationViewModel.DeviceUpdate.Success -> {
                saveDeviceMessagingToken(pendingMessagingToken)
                pendingMessagingToken = ""
            }
            else -> BlindoLogger.e("Unable to push new messaging token to server. $deviceUpdate")
        }
        subscribeMembership()
        getMembership()
    }

    override fun onMembershipPurchased(purchasedMembership: BillingViewModel.PurchasedMembership) {
        when (purchasedMembership) {
            is BillingViewModel.PurchasedMembership.Success -> purchasedMembership.membership.also { membership ->
                if (membership.isActive()) {
                    setIsUserPremium(true)
                    verifyIsFirstRunOrRequiresShowUpdates()
                } else {
                    setIsUserPremium(false)
                    subscribeAdsConsentStatus()
                    adsViewModel.updateAdsConsentStatus()
                }
            }
            else -> {
                setIsUserPremium(false)
                subscribeAdsConsentStatus()
                adsViewModel.updateAdsConsentStatus()
            }
        }
    }

    private fun subscribeAdsConsentStatus() = adsViewModel.adsConsentStatus.observe(this) {
        when (it) {
            is AdsResponse.Success -> when (val status = it.data) {
                AdsManager.Companion.ConsentStatus.ADS_FREE -> getUser()?.also { currentUser ->
                    if (currentUser.isPremium.not()) {
                        findNavController().navigate(
                            MainNavigationDirections.actionGlobalToAdsSettings(status.name, true)
                        )
                    } else {
                        verifyIsFirstRunOrRequiresShowUpdates()
                    }
                } ?: run {
                    findNavController().navigate(
                        MainNavigationDirections.actionGlobalToAdsSettings(status.name, true)
                    )
                }
                AdsManager.Companion.ConsentStatus.UNKNOWN -> {
                    findNavController().navigate(
                        MainNavigationDirections.actionGlobalToAdsSettings(status.name, true)
                    )
                }
                else -> {
                    subscribeIsAdsClientInitialized()
                    adsViewModel.initializeAdsClient()
                }
            }
            is AdsResponse.Error -> showErrorLoading(it.error.localizedMessage ?: it.error.toString())
        }
    }

    private fun subscribeIsAdsClientInitialized() = adsViewModel.isAdsClientInitialized.observe(this) {
        if (it) {
            verifyIsFirstRunOrRequiresShowUpdates()
        } else {
            findNavController().navigate(
                MainNavigationDirections.actionGlobalToAdsSettings(
                    AdsManager.Companion.ConsentStatus.INTERNET_ERROR_OR_ADS_BLOCKER.name,
                    true
                )
            )
        }
    }

    private fun verifyIsFirstRunOrRequiresShowUpdates() {
        if (splashViewModel.isFirstRun.not()) {
            if (splashViewModel.isVisionIntroduced) {
                if (splashViewModel.isPrivacyPolicyAccepted) {
                    if (messagingViewModel.isPushNotificationsConfigured()) {
                        findNavController().navigate(
                            SplashFragmentDirections.actionFromSplashToHome()
                        )
                    } else {
                        findNavController().navigate(
                            SplashFragmentDirections.actionFromSplashToTutorial(9)
                        )
                    }
                } else {
                    findNavController().navigate(
                        SplashFragmentDirections.actionFromSplashToTutorial(8)
                    )
                }
            } else {
                splashViewModel.resetIsPrivacyPolicyAccepted()
                findNavController().navigate(
                    SplashFragmentDirections.actionFromSplashToTutorial(5)
                )
            }
        } else {
            findNavController().navigate(
                SplashFragmentDirections.actionFromSplashToTutorial()
            )
        }
    }

    private fun showErrorLoading(reason: String) {
        BlindoLogger.e(reason)
        loadingText.text = getString(
            R.string.ads__current_status,
            reason
        )
    }
}
