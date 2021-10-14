package com.pbaltazar.blindo.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.pbaltazar.blindo.MainNavigationDirections
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentSplashBinding
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.ads.ui.AdsViewModel
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticationViewModel
import com.pbaltazar.blindo.utils.billing.ui.BillingViewModel
import com.pbaltazar.blindo.utils.constants.ARGUMENT_CONSENT_STATUS
import com.pbaltazar.blindo.utils.extensions.isExpired
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashFragment : AuthenticableFragment<FragmentSplashBinding>() {

    private val splashViewModel: SplashViewModel by viewModel()
    private val adsViewModel: AdsViewModel by sharedViewModel()
    private val billingViewModel: BillingViewModel by sharedViewModel()

    private lateinit var loadingText: TextView

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeBillingConnection()
        subscribeAuthentication()
        subscribeMembership()
        subscribeAdsConsentStatus()
        subscribeAdsSettings()
        subscribeIsAdsClientInitialized()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSplashBinding.inflate(inflater, container, false)
        loadingText = binding!!.loadingText
        return binding!!.root
    }

    override fun onSubscribeAuthentication(userAuthentication: AuthenticationViewModel.UserAuthentication) {
        when (userAuthentication) {
            is AuthenticationViewModel.UserAuthentication.Success -> if (userAuthentication.user.isPremium) {
                setIsUserPremium(true)
                verifyIsFirstRunAndPrivacyPolicyAccepted()
            } else {
                billingViewModel.getMembership(true)
            }
            else -> adsViewModel.updateAdsConsentStatus()
        }
    }

    private fun subscribeBillingConnection() = billingViewModel.isConnected.observe(this, Observer {
        when (val connection = it) {
            is BillingViewModel.BillingConnection.Connected -> if (getUser() == null) {
                authenticateUser()
            } else {
                if (getUser()?.isPremium ?: false) {
                    verifyIsFirstRunAndPrivacyPolicyAccepted()
                } else {
                    adsViewModel.updateAdsConsentStatus()
                }
            }
            is BillingViewModel.BillingConnection.Disconnected -> showErrorLoading(getString(R.string.premium__disconnected))
            is BillingViewModel.BillingConnection.BadRequest -> showErrorLoading(connection.reason)
            is BillingViewModel.BillingConnection.ServiceUnavailable -> showErrorLoading(getString(R.string.premium__service_unavailable))
            is BillingViewModel.BillingConnection.FeatureNotSupported -> showErrorLoading(getString(R.string.premium__feature_not_supported))
            is BillingViewModel.BillingConnection.UnknownError -> showErrorLoading(getString(R.string.premium__unknown_error))
        }
    })

    private fun subscribeMembership() = billingViewModel.membership.observe(this, Observer {
        when (val response = it) {
            is BillingViewModel.ActiveMembership.Success -> response.membership.also { membership ->
                if (membership.isExpired().not()) {
                    setIsUserPremium(true)
                    verifyIsFirstRunAndPrivacyPolicyAccepted()
                } else {
                    setIsUserPremium(false)
                    adsViewModel.updateAdsConsentStatus()
                }
            }
            else -> {
                setIsUserPremium(false)
                adsViewModel.updateAdsConsentStatus()
            }
        }
    })

    private fun subscribeAdsConsentStatus() = adsViewModel.adsConsentStatus.observe(this, Observer {
        when (it) {
            is AdsViewModel.AdsConsentStatus.Success -> when (val status = it.status) {
                AdsManager.ConsentStatus.ADS_FREE -> getUser()?.also {
                    if (it.isPremium.not()) {
                        findNavController().navigate(
                            MainNavigationDirections.actionGlobalToAdsSettings(status.name, true)
                        )
                    } else {
                        verifyIsFirstRunAndPrivacyPolicyAccepted()
                    }
                } ?: findNavController().navigate(
                    MainNavigationDirections.actionGlobalToAdsSettings(status.name, true)
                )
                AdsManager.ConsentStatus.UNKNOWN -> findNavController().navigate(
                    MainNavigationDirections.actionGlobalToAdsSettings(status.name, true)
                )
                else -> adsViewModel.initializeAdsClient()
            }
            is AdsViewModel.AdsConsentStatus.Failure -> showErrorLoading(it.reason)
        }
    })

    private fun subscribeAdsSettings() =
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<AdsManager.ConsentStatus>(
            ARGUMENT_CONSENT_STATUS)?.observe(this, Observer {
                adsViewModel.setConsentStatus(AdsViewModel.AdsConsentStatus.Success(it))
        })

    private fun subscribeIsAdsClientInitialized() = adsViewModel.isAdsClientInitialized.observe(this, Observer {
        if (it) {
            verifyIsFirstRunAndPrivacyPolicyAccepted()
        } else {
            findNavController().navigate(
                MainNavigationDirections.actionGlobalToAdsSettings(AdsManager.ConsentStatus.INTERNET_ERROR_OR_ADS_BLOCKER.name, true)
            )
        }
    })

    private fun verifyIsFirstRunAndPrivacyPolicyAccepted() {
        if (splashViewModel.isFirstRun.not()) {
            if (splashViewModel.isPrivacyPolicyAccepted) {
                findNavController().navigate(
                    SplashFragmentDirections.actionFromSplashToHome()
                )
            } else {
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
        loadingText.text = getString(
            R.string.ads__current_status,
            reason
        )
    }
}
