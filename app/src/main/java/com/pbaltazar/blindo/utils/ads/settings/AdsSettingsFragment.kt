package com.pbaltazar.blindo.utils.ads.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentAdsSettingsBinding
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import com.pbaltazar.blindo.utils.constants.ARGUMENT_CONSENT_STATUS
import org.koin.androidx.viewmodel.ext.android.viewModel

class AdsSettingsFragment : AuthenticableFragment() {

    private val adsSettingsViewModel: AdsSettingsViewModel by viewModel()
    private val adsSettingsFragmentArgs: AdsSettingsFragmentArgs by navArgs()
    private var binding: FragmentAdsSettingsBinding? = null

    private lateinit var currentStatus: TextView
    private lateinit var changeConsent: Button
    private lateinit var purchaseSubscription: Button

    private var isFirstLaunch: Boolean = true
    private var adsConsentStatus: AdsManager.ConsentStatus? = null
    set(value) {
        field = value
        if (getUser() != null) {
            if (getUser()!!.isPremium) {
                currentStatus.text = getString(
                    R.string.ads__current_status,
                    getString(R.string.ads__ads_free)
                )
                changeConsent.isEnabled = false
                purchaseSubscription.isEnabled = false
            } else {
                changeConsent.isEnabled = true
                purchaseSubscription.isEnabled = true
            }
        }
        when (adsConsentStatus) {
            AdsManager.ConsentStatus.PERSONALIZED -> currentStatus.text = getString(
                R.string.ads__current_status,
                getString(R.string.ads__personalized)
            )
            AdsManager.ConsentStatus.NON_PERSONALIZED -> currentStatus.text = getString(
                R.string.ads__current_status,
                getString(R.string.ads__non_personalized)
            )
            AdsManager.ConsentStatus.ADS_FREE -> currentStatus.text = getString(
                R.string.ads__current_status,
                getString(R.string.ads__ads_free_but_not_purchased)
            )
            AdsManager.ConsentStatus.NON_REQUIRED -> {
                currentStatus.text = getString(
                    R.string.ads__current_status,
                    getString(R.string.ads__non_required)
                )
                changeConsent.isEnabled = false
            }
            AdsManager.ConsentStatus.UNKNOWN -> currentStatus.text = getString(
                R.string.ads__current_status,
                getString(R.string.ads__unknown_consent)
            )
            else -> {
                currentStatus.text = getString(
                    R.string.ads__current_status,
                    getString(R.string.ads__ads_blocker_or_internet_error)
                )
                changeConsent.isEnabled = false
            }
        }
    }
    private var closeAfterUpdate: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        closeAfterUpdate = adsSettingsFragmentArgs.closeAfterUpdate
        subscribeAdsConsentStatus()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAdsSettingsBinding.inflate(inflater, container, false)
        currentStatus = binding!!.currentStatus
        changeConsent = binding!!.changeConsent
        purchaseSubscription = binding!!.purchaseSubscription
        setupUi()
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adsSettingsFragmentArgs.consentStatus?.also {
            adsConsentStatus = AdsManager.ConsentStatus.valueOf(it)
            forceA11yFocus()
            isFirstLaunch = false
        }
    }

    override fun onResume() {
        super.onResume()
        if (adsConsentStatus == null && isFirstLaunch) {
            adsSettingsViewModel.updateAdsConsentStatus()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun subscribeAdsConsentStatus() = adsSettingsViewModel.adsConsentStatus.observe(this, Observer {
        when (it) {
            is AdsSettingsViewModel.AdsConsentStatus.Success -> if (adsConsentStatus?.equals(it.status)?.not() ?: true) {
                adsConsentStatus = it.status
                if (isFirstLaunch) {
                    isFirstLaunch = false
                    forceA11yFocus()
                } else {
                    if (closeAfterUpdate) {
                        findNavController().previousBackStackEntry?.savedStateHandle?.set(
                            ARGUMENT_CONSENT_STATUS,
                            adsConsentStatus
                        )
                        findNavController().popBackStack()
                    } else {
                        forceA11yFocus()
                    }
                }
            }
            is AdsSettingsViewModel.AdsConsentStatus.Failure -> {
                adsConsentStatus = AdsManager.ConsentStatus.INTERNET_ERROR_OR_ADS_BLOCKER
                currentStatus.text = it.reason
                forceA11yFocus()
            }
        }
    })

    private fun setupUi() {
        changeConsent.setOnClickListener {
            adsSettingsViewModel.showConsentForm()
        }
        purchaseSubscription.setOnClickListener {
            findNavController().navigate(
                AdsSettingsFragmentDirections.actionFromAdsSettingsToPremium()
            )
        }
    }

    private fun forceA11yFocus() {
        currentStatus.apply {
            requestFocus()
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }
    }
}
