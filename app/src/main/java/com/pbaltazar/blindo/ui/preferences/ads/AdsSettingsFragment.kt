package com.pbaltazar.blindo.ui.preferences.ads

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentAdsSettingsBinding
import com.pbaltazar.blindo.entities.responses.AdsResponse
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.ads.ui.AdsViewModel
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import com.pbaltazar.blindo.utils.constants.ARGUMENT_CONSENT_STATUS
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class AdsSettingsFragment : AuthenticableFragment<FragmentAdsSettingsBinding>() {

    private val adsViewModel: AdsViewModel by sharedViewModel()
    private val adsSettingsFragmentArgs: AdsSettingsFragmentArgs by navArgs()

    private lateinit var currentStatus: TextView
    private lateinit var changeConsent: Button
    private lateinit var purchaseSubscription: Button

    private var isFirstLaunch: Boolean = true
    private var adsConsentStatus: AdsManager.Companion.ConsentStatus? = null
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
        when (value) {
            AdsManager.Companion.ConsentStatus.PERSONALIZED -> currentStatus.text = getString(
                R.string.ads__current_status,
                getString(R.string.ads__personalized)
            )
            AdsManager.Companion.ConsentStatus.NON_PERSONALIZED -> currentStatus.text = getString(
                R.string.ads__current_status,
                getString(R.string.ads__non_personalized)
            )
            AdsManager.Companion.ConsentStatus.ADS_FREE -> currentStatus.text = getString(
                R.string.ads__current_status,
                getString(R.string.ads__ads_free_but_not_purchased)
            )
            AdsManager.Companion.ConsentStatus.NON_REQUIRED -> {
                currentStatus.text = getString(
                    R.string.ads__current_status,
                    getString(R.string.ads__non_required)
                )
                changeConsent.isEnabled = false
            }
            AdsManager.Companion.ConsentStatus.UNKNOWN -> currentStatus.text = getString(
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

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        closeAfterUpdate = adsSettingsFragmentArgs.closeAfterUpdate
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
            adsConsentStatus = AdsManager.Companion.ConsentStatus.valueOf(it)
            forceA11yFocus()
            isFirstLaunch = false
        }
    }

    override fun onResume() {
        super.onResume()
        if (adsConsentStatus == null && isFirstLaunch) {
            subscribeAdsConsentStatus()
            adsViewModel.updateAdsConsentStatus()
        }
    }

    private fun subscribeAdsConsentStatus() = adsViewModel.adsConsentStatus.observe(this) {
        when (it) {
            is AdsResponse.Success -> if (adsConsentStatus?.equals(it.data)?.not() == true) {
                adsConsentStatus = it.data
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
            is AdsResponse.Error -> {
                adsConsentStatus = AdsManager.Companion.ConsentStatus.INTERNET_ERROR_OR_ADS_BLOCKER
                currentStatus.text = it.error.localizedMessage ?: it.error.toString()
                forceA11yFocus()
            }
        }
    }

    private fun setupUi() {
        changeConsent.setOnClickListener {
            adsViewModel.showConsentForm(requireContext())
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
