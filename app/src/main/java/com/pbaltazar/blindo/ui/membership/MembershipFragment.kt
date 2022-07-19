package com.pbaltazar.blindo.ui.membership

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.components.subscriptions.SubscriptionInfo
import com.pbaltazar.blindo.databinding.FragmentMembershipBinding
import com.pbaltazar.blindo.entities.Membership
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.enums.MembershipCancellationContext
import com.pbaltazar.blindo.entities.enums.MembershipState
import com.pbaltazar.blindo.entities.purchases.enums.ProductType
import com.pbaltazar.blindo.entities.purchases.subscriptions.Subscription
import com.pbaltazar.blindo.entities.purchases.subscriptions.SubscriptionOffer
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import com.pbaltazar.blindo.utils.billing.ui.BilleableFragment
import com.pbaltazar.blindo.utils.billing.ui.BillingViewModel
import com.pbaltazar.blindo.utils.constants.AUTH_CANCELED_ON_DIALOG
import com.pbaltazar.blindo.utils.constants.MANAGE_SUBS_URI
import com.pbaltazar.blindo.utils.extensions.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MembershipFragment : BilleableFragment<FragmentMembershipBinding>(),
    SubscriptionInfo.OnOfferSelectedListener {

    private lateinit var currentSubscription: TextView
    private lateinit var changeSubscription: ImageButton
    private lateinit var currentSubscriptionHeader: Group
    private lateinit var subscriptionsContainer: RecyclerView
    private lateinit var subscriptionNotice: TextView
    private lateinit var manageSubscription: Button
    private lateinit var subscribeNow: Button

    private val membershipAdapter: MembershipAdapter = MembershipAdapter(this as SubscriptionInfo.OnOfferSelectedListener)
    private val membershipPurchasesAdapter: MembershipPurchasesAdapter = MembershipPurchasesAdapter()

    private var membership: Membership? = null
    set(value) {
        field = value
        field?.also { m ->
            currentSubscription.apply {
                val membershipName = if (membershipAdapter.itemCount > 0)
                    membershipAdapter.items.filter { it.id.equals(m.productId) }.takeUnless { it.isEmpty() }?.first()?.let { subscription ->
                        subscription.name
                    } ?: m.productId
                else m.productId
                text = getString(
                    R.string.membership__current_subscription,
                    membershipName,
                    m.state.toReadableString(requireContext())
                )
                tooltipText = m.state.getInfoString(requireContext())
                visible()
            }
            subscriptionsContainer.apply {
                if (membershipPurchasesAdapter.itemCount > 0) {
                    membershipPurchasesAdapter.clearItems()
                }
                m.purchases?.also { purchases ->
                    membershipPurchasesAdapter.appendItems(purchases)
                }
                adapter = membershipPurchasesAdapter
            }
            subscriptionNotice.apply {
                visible()
                when (m.state) {
                    MembershipState.SUBSCRIPTION_STATE_UNSPECIFIED -> gone()
                    MembershipState.SUBSCRIPTION_STATE_PENDING -> gone()
                    MembershipState.SUBSCRIPTION_STATE_ACTIVE -> {
                        text = getString(
                            R.string.membership__current_subscription_expiration,
                            m.expireAt.toUiFormat()
                        )
                    }
                    MembershipState.SUBSCRIPTION_STATE_PAUSED -> {
                        text = getString(
                            R.string.membership__current_subscription_auto_resume_time,
                            m.autoResumeTime?.toUiFormat()
                        )
                    }
                    MembershipState.SUBSCRIPTION_STATE_IN_GRACE_PERIOD -> {
                        text = getString(R.string.membership__current_subscription_in_grace_period)
                    }
                    MembershipState.SUBSCRIPTION_STATE_ON_HOLD -> gone()
                    MembershipState.SUBSCRIPTION_STATE_CANCELED -> m.cancellationContext?.also { membershipCancellationContext ->
                        when (membershipCancellationContext) {
                            MembershipCancellationContext.USER_INITIATED_CANCELLATION -> {
                                text = getString(
                                    R.string.membership__current_subscription_canceled_by_user,
                                    m.expireAt.toUiFormat()
                                )
                            }
                            MembershipCancellationContext.SYSTEM_INITIATED_CANCELLATION -> {
                                text = getString(R.string.membership__current_subscription_canceled_by_system)
                            }
                            MembershipCancellationContext.DEVELOPER_INITIATED_CANCELLATION -> {
                                text = getString(
                                    R.string.membership__current_subscription_canceled_by_developer,
                                    m.expireAt.toUiFormat()
                                )
                            }
                            MembershipCancellationContext.REPLACEMENT_CANCELLATION -> {
                                text = getString(R.string.membership__current_subscription_canceled_by_replacement)
                            }
                        }
                    }
                    else -> gone()
                }
            }
            manageSubscription.apply {
                visible()
                setOnClickListener {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(
                            MANAGE_SUBS_URI.format(
                                m.productId,
                                requireActivity().packageName
                            )
                        )
                    ).apply {
                        startActivity(this)
                    }
                }
            }
            subscribeNow.invisible()
        } ?: run {
            subscriptionNotice.apply {
                gone()
                text = ""
            }
            manageSubscription.invisible()
            subscribeNow.visible()
        }
    }

    override val isSearchable: Boolean
        get() = false

    override fun onOfferSelected(subscriptionInfo: SubscriptionInfo, selectedSubscriptionOffer: SubscriptionOffer?) {
        subscriptionNotice.apply {
            visible()
            text = getString(
                R.string.membership__subscription_notice,
                subscriptionInfo.getSelectedOfferFormatedPhases()
            )
        }
        subscribeNow.apply {
            isEnabled = selectedSubscriptionOffer != null
            selectedSubscriptionOffer?.also { subscriptionOffer ->
                subscriptionInfo.getSubscription()?.also { subscription ->
                    subscription.selectedOffer = subscriptionOffer
                    setOnClickListener { _ ->
                        launchPurchase(listOf(subscription))
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeAuth()
        subscribeSubscriptionsToPurchase()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentMembershipBinding.inflate(inflater, container, false)
        currentSubscription = binding!!.currentSubscription
        changeSubscription = binding!!.changeSubscription
        currentSubscriptionHeader = binding!!.currentSubscriptionHeader
        subscriptionsContainer = binding!!.subscriptionsContainer
        subscriptionNotice = binding!!.subscriptionNotice
        manageSubscription = binding!!.manageSubscription
        subscribeNow = binding!!.subscribeNow
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        subscribeUser()
    }

    override fun onResume() {
        super.onResume()
        if (membershipAdapter.itemCount < 1) {
            getSubscriptionsToPurchase()
        }
    }

    override fun getMenuResId(): Int = R.menu.membership
    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menuRestoreSubscriptions -> {
            askForNewSubscriptionPurchases()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onSubscribeUser(user: User?) {
        if (user == null) {
            findNavController().navigate(
                MembershipFragmentDirections.actionFromMembershipToRequiresAuth()
            )
        }
    }

    private fun subscribeAuth() = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
        AUTH_CANCELED_ON_DIALOG)?.observe(this, Observer {
        if (it.not()) {
            launchLoginScreen()
        } else {
            findNavController().popBackStack()
        }
    })

    override fun onSubscriptionsToPurchase(availableProducts: BillingViewModel.AvailableProducts) {
        when (availableProducts) {
            is BillingViewModel.AvailableProducts.Success -> availableProducts.products.mapNotNull { it as Subscription }.also { subscriptions ->
                if (membershipAdapter.itemCount > 0) {
                    membershipAdapter.clearItems()
                }
                membershipAdapter.appendItems(subscriptions)
                subscribeMembership()
            }
            is BillingViewModel.AvailableProducts.Empty -> processError(R.string.membership__billing_no_products)
            is BillingViewModel.AvailableProducts.FeatureNotSupported -> processError(R.string.membership__billing_feature_not_supported)
            is BillingViewModel.AvailableProducts.ServiceUnavailable -> processError(R.string.membership__billing_service_unavailable)
            is BillingViewModel.AvailableProducts.Error -> processError(availableProducts.reason)
        }
    }

    override fun onMembershipPurchased(purchasedMembership: BillingViewModel.PurchasedMembership) {
        when (purchasedMembership) {
            is BillingViewModel.PurchasedMembership.Success -> purchasedMembership.membership.also { m ->
                if (m.isExpired()) {
                    membership = null
                } else {
                    membership = m
                }
            }
            is BillingViewModel.PurchasedMembership.Empty -> {
                membership = null
            }
            is BillingViewModel.PurchasedMembership.Error -> {
                membership = null
                processError(purchasedMembership.reason)
            }
            is BillingViewModel.PurchasedMembership.NotSignedIn -> {
                membership = null
            }
            is BillingViewModel.PurchasedMembership.InvalidIdToken -> {
                membership = null
            }
        }
    }

    private fun processError(@StringRes errorMessage: Int) = processError(getString(errorMessage))

    private fun processError(errorMessage: String) {
        subscriptionNotice.apply {
            visible()
            text = errorMessage
            requestFocus()
        }
    }

    private fun setupUi() {
        subscribeNow.isEnabled = false
        subscriptionsContainer.adapter = membershipAdapter
    }
}
