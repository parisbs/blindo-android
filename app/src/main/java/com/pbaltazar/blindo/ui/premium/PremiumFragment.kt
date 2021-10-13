package com.pbaltazar.blindo.ui.premium

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.utils.billing.ui.BillingViewModel
import com.pbaltazar.blindo.databinding.FragmentPremiumBinding
import com.pbaltazar.blindo.entities.Membership
import com.pbaltazar.blindo.entities.Sku
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticationViewModel
import com.pbaltazar.blindo.utils.constants.AUTH_CANCELED_ON_DIALOG
import com.pbaltazar.blindo.utils.constants.MANAGE_SUBS_URI
import com.pbaltazar.blindo.utils.constants.MONTHLY_SUBSCRIPTION_SKU
import com.pbaltazar.blindo.utils.extensions.isExpired
import com.pbaltazar.blindo.utils.extensions.toLocalMembership
import com.pbaltazar.blindo.utils.extensions.toUiFormat
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class PremiumFragment : AuthenticableFragment<FragmentPremiumBinding>() {

    private val billingViewModel: BillingViewModel by sharedViewModel()

    private var isRequestedMembershipOnce: Boolean = false

    private lateinit var paymentInfo: TextView
    private lateinit var paymentPeriod: TextView
    private lateinit var subsButton: Button

    private lateinit var skus: Map<String, Sku>
    private var membership: Membership? = null
    set(value) {
        field = value
        value?.also { membership ->
            paymentInfo.text = getString(
                R.string.premium__payment_info_purchased,
                if (membership.isCanceled.not())
                    getString(R.string.premium__status_active)
                else
                    getString(R.string.premium__status_canceled)
            )
            paymentPeriod.text = getString(
                R.string.premium__payment_period_purchased,
                membership.expireAt.toUiFormat()
            )
            subsButton.apply {
                isEnabled = true
                text = getString(R.string.premium__manage_subs)
                setOnClickListener {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            MANAGE_SUBS_URI.format(
                                MONTHLY_SUBSCRIPTION_SKU,
                                requireActivity().packageName
                            )
                        )
                    ).apply {
                        startActivity(this)
                    }
                }
            }
        }
    }

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeUser()
        subscribeBillingConnection()
        subscribeSkus()
        subscribeMembership()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPremiumBinding.inflate(inflater, container, false)
        paymentInfo = binding!!.paymentInfo
        paymentPeriod = binding!!.paymentPeriod
        subsButton = binding!!.subsButton
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        subscribeAuth()
    }

    override fun onSubscribeUser() {
        if (getUser() != null) {
            if (isRequestedMembershipOnce.not()) {
                isRequestedMembershipOnce = true
                billingViewModel.getMembership()
            }
        } else {
            findNavController().navigate(
                PremiumFragmentDirections.actionFromPremiumToRequiresAuth()
            )
        }
    }

    private fun subscribeAuth() = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
        AUTH_CANCELED_ON_DIALOG)?.observe(this, Observer {
        if (it.not()) {
            loginScreen.launch(Unit)
        } else {
            findNavController().popBackStack()
        }
    })

    private fun subscribeBillingConnection() = billingViewModel.isConnected.observe(this, Observer {
        when (val response = it) {
            is BillingViewModel.BillingConnection.Connected -> billingViewModel.getSkus()
            is BillingViewModel.BillingConnection.Disconnected -> processError(getString(R.string.premium__disconnected))
            is  BillingViewModel.BillingConnection.BadRequest -> processError(response.reason)
            is BillingViewModel.BillingConnection.FeatureNotSupported -> processError(getString(R.string.premium__feature_not_supported))
            is BillingViewModel.BillingConnection.ServiceUnavailable -> processError(getString(R.string.premium__service_unavailable))
            is  BillingViewModel.BillingConnection.UnknownError -> processError(getString(R.string.premium__unknown_error))
        }
    })

    private fun subscribeSkus() = billingViewModel.skus.observe(this, Observer {
        when (val response = it) {
            is BillingViewModel.AvailableSkus.Success -> {
                skus = response.skus
                subsButton.isEnabled = true
                if (membership == null) {
                    if (skus.containsKey(MONTHLY_SUBSCRIPTION_SKU)) {
                        skus[MONTHLY_SUBSCRIPTION_SKU]?.also { sku ->
                            paymentInfo.text = getString(
                                R.string.premium__payment_info,
                                "${sku.price} ${sku.currency}"
                            )
                            paymentPeriod.text = getString(
                                R.string.premium__payment_period,
                                "${sku.price} ${sku.currency}"
                            )
                        } ?: processError(getString(R.string.premium__empty_skus))
                    } else {
                        processError(getString(R.string.premium__empty_skus))
                    }
                }
            }
            is BillingViewModel.AvailableSkus.Empty -> processError(getString(R.string.premium__empty_skus))
            is BillingViewModel.AvailableSkus.FeatureNotSupported -> processError(getString(R.string.premium__feature_not_supported))
            is BillingViewModel.AvailableSkus.ServiceUnavailable -> processError(getString(R.string.premium__service_unavailable))
            is BillingViewModel.AvailableSkus.BadRequest -> processError(response.reason)
            is BillingViewModel.AvailableSkus.UnknownError -> processError(getString(R.string.premium__unknown_error))
        }
    })

    private fun subscribeMembership() = billingViewModel.membership.observe(this, Observer {
        when (val response = it) {
            is BillingViewModel.ActiveMembership.Success -> {
                membership = response.membership
                updateUserMembershipStatus()
            }
            is BillingViewModel.ActiveMembership.PurchasedButNotAcknowledged -> {
                membership = response.blindoPurchase.toLocalMembership()
                updateUserMembershipStatus()
                showBillingStatus(getString(R.string.premium__purchased_but_not_processed))
            }
            is BillingViewModel.ActiveMembership.CanceledByUser -> showBillingStatus(getString(R.string.premium__canceled_by_user))
            is BillingViewModel.ActiveMembership.FeatureNotSupported -> processError(getString(R.string.premium__feature_not_supported))
            is BillingViewModel.ActiveMembership.ServiceUnavailable -> processError(getString(R.string.premium__service_unavailable))
            is BillingViewModel.ActiveMembership.BadRequest  -> processError(response.errors.joinToString(", "))
        }
    })

    private fun updateUserMembershipStatus() {
        getUser()?.also { user ->
            membership?.also { currentMembership ->
                when {
                    currentMembership.isExpired() && user.isPremium -> setIsUserPremium(false)
                    currentMembership.isExpired().not() && user.isPremium.not() -> setIsUserPremium(true)
                }
            }
        }
    }

    private fun showBillingStatus(message: String) = Snackbar.make(
        subsButton,
        message,
        Snackbar.LENGTH_LONG
    ).show()

    private fun processError(errorMessage: String) {
        paymentInfo.text = errorMessage
        paymentPeriod.text = ""
        subsButton.isEnabled = false
    }

    private fun setupUi() {
        subsButton.apply {
            setOnClickListener {
                billingViewModel.launchPurchase(
                    requireActivity() as AppCompatActivity,
                    skus
                        .withDefault { Sku() }
                        .getValue(MONTHLY_SUBSCRIPTION_SKU)
                )
            }
            isEnabled = false
        }
    }
}
