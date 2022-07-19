package com.pbaltazar.blindo.utils.billing.ui

import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.pbaltazar.blindo.entities.purchases.Product
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class BilleableFragment<VB : ViewBinding> : AuthenticableFragment<VB>(),
    BillingListener {

    private val billingViewModel: BillingViewModel by sharedViewModel()

    val billeableActivity: BilleableActivity? get() {
        return if (requireActivity() is BilleableActivity) {
            requireActivity() as BilleableActivity
        } else null
    }

    val requireBilleableActivity: BilleableActivity get() {
        return billeableActivity ?: throw NullPointerException("Parent BilleableActivity is null.")
    }

    fun startBillingConnection() = requireBilleableActivity.startBillingConnection()

    fun closeBillingConnection() = requireBilleableActivity.closeBillingConnection()

    override fun onBillingConnection(billingConnection: BillingViewModel.BillingConnection) {
    }

    fun subscribeBillingConnection() = billingViewModel.isConnected.observe(this, Observer {
        onBillingConnection(it)
    })

    fun getInAppsToPurchase() = requireBilleableActivity.getInAppsToPurchase()

    override fun onInAppsToPurchase(availableProducts: BillingViewModel.AvailableProducts) {
    }

    fun subscribeInAppsToPurchase() = billingViewModel.inApps.observe(this, Observer {
        onInAppsToPurchase(it)
    })

    fun getSubscriptionsToPurchase() = requireBilleableActivity.getSubscriptionsToPurchase()

    override fun onSubscriptionsToPurchase(availableProducts: BillingViewModel.AvailableProducts) {
    }

    fun subscribeSubscriptionsToPurchase() = billingViewModel.subscriptions.observe(this, Observer {
        onSubscriptionsToPurchase(it)
    })

    fun askForNewInAppPurchases() = requireBilleableActivity.askForNewInAppPurchases()

    fun askForNewSubscriptionPurchases() = requireBilleableActivity.askForNewSubscriptionPurchases()

    fun askForNewPurchases() = requireBilleableActivity.askForNewPurchases()

    fun launchPurchase(products: List<Product>) = requireBilleableActivity.launchPurchase(products)

    override fun onNewPurchases(purchases: BillingViewModel.Purchases) {
    }

    fun subscribePurchases() = billingViewModel.purchases.observe(this, Observer {
        onNewPurchases(it)
    })

    override fun onProductConsumption(consumption: BillingViewModel.Consumption) {
    }

    fun subscribeConsumption() = billingViewModel.consumption.observe(this, Observer {
        onProductConsumption(it)
    })

    override fun onCoinsPurchased(purchasedCoin: BillingViewModel.PurchasedCoin) {
    }

    fun subscribeCoins() = billingViewModel.coins.observe(this, Observer {
        onCoinsPurchased(it)
    })

    fun getCoinsHistory() = requireBilleableActivity.getCoinsHistory()

    override fun onCoinsHistory(coinsHistory: BillingViewModel.CoinsHistory) {
    }

    fun subscribeCoinsHistory() = billingViewModel.coinsHistory.observe(this, Observer {
        onCoinsHistory(it)
    })

    fun getMembership() = requireBilleableActivity.getMembership()

    override fun onMembershipPurchased(purchasedMembership: BillingViewModel.PurchasedMembership) {
    }

    fun subscribeMembership() = billingViewModel.membership.observe(this, Observer {
        onMembershipPurchased(it)
    })
    }
