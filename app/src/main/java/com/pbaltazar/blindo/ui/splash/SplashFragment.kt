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
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.ads.ui.AdsViewModel
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticationViewModel
import com.pbaltazar.blindo.utils.billing.ui.BilleableFragment
import com.pbaltazar.blindo.utils.billing.ui.BillingViewModel
import com.pbaltazar.blindo.utils.constants.ARGUMENT_CONSENT_STATUS
import com.pbaltazar.blindo.utils.extensions.isActive
import com.pbaltazar.blindo.utils.log.BlindoLogger
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : BilleableFragment<FragmentSplashBinding>() {

    private val splashViewModel: SplashViewModel by viewModel()
    private val adsViewModel: AdsViewModel by sharedViewModel()

    private lateinit var loadingText: TextView

    private var isInitFlowInitialized: Boolean = false
    private var isAdsFlowInitialized: Boolean = false

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
            is BillingViewModel.BillingConnection.Connected -> if (getUser() == null) {
                subscribeAuthentication()
                authenticateUser()
            } else {
                subscribeMembership()
                getMembership()
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
                subscribeMembership()
                getMembership()
            }
            else -> {
                subscribeAdsConsentStatus()
                adsViewModel.updateAdsConsentStatus()
            }
        }
    }

    override fun onMembershipPurchased(purchasedMembership: BillingViewModel.PurchasedMembership) {
        when (purchasedMembership) {
            is BillingViewModel.PurchasedMembership.Success -> purchasedMembership.membership.also { membership ->
                if (membership.isActive()) {
                    setIsUserPremium(true)
                    verifyIsFirstRunAndPrivacyPolicyAccepted()
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
            is AdsViewModel.AdsConsentStatus.Success -> when (val status = it.status) {
                AdsManager.ConsentStatus.ADS_FREE -> getUser()?.also { currentUser ->
                    if (currentUser.isPremium.not()) {
                        if (isAdsFlowInitialized.not()) {
                            isAdsFlowInitialized = true
                            subscribeAdsSettings()
                        }
                        findNavController().navigate(
                            MainNavigationDirections.actionGlobalToAdsSettings(status.name, true)
                        )
                    } else {
                        verifyIsFirstRunAndPrivacyPolicyAccepted()
                    }
                } ?: run {
                    if (isAdsFlowInitialized.not()) {
                        isAdsFlowInitialized = true
                        subscribeAdsSettings()
                    }
                    findNavController().navigate(
                        MainNavigationDirections.actionGlobalToAdsSettings(status.name, true)
                    )
                }
                AdsManager.ConsentStatus.UNKNOWN -> {
                    if (isAdsFlowInitialized.not()) {
                        isAdsFlowInitialized = true
                        subscribeAdsSettings()
                    }
                    findNavController().navigate(
                        MainNavigationDirections.actionGlobalToAdsSettings(status.name, true)
                    )
                }
                else -> {
                    if (isAdsFlowInitialized.not()) {
                        isAdsFlowInitialized = true
                        subscribeIsAdsClientInitialized()
                    }
                    adsViewModel.initializeAdsClient()
                }
            }
            is AdsViewModel.AdsConsentStatus.Failure -> showErrorLoading(it.reason)
        }
    }

    private fun subscribeAdsSettings() =
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<AdsManager.ConsentStatus>(
            ARGUMENT_CONSENT_STATUS)?.observe(this) {
            adsViewModel.setConsentStatus(AdsViewModel.AdsConsentStatus.Success(it))
        }

    private fun subscribeIsAdsClientInitialized() = adsViewModel.isAdsClientInitialized.observe(this) {
        if (it) {
            verifyIsFirstRunAndPrivacyPolicyAccepted()
        } else {
            findNavController().navigate(
                MainNavigationDirections.actionGlobalToAdsSettings(
                    AdsManager.ConsentStatus.INTERNET_ERROR_OR_ADS_BLOCKER.name,
                    true
                )
            )
        }
    }

    private fun verifyIsFirstRunAndPrivacyPolicyAccepted() {
        if (splashViewModel.isFirstRun.not()) {
            if (splashViewModel.isVisionIntroduced) {
                if (splashViewModel.isPrivacyPolicyAccepted) {
                    findNavController().navigate(
                        SplashFragmentDirections.actionFromSplashToHome()
                    )
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
