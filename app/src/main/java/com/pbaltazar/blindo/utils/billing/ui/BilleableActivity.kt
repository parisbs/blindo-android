package com.pbaltazar.blindo.utils.billing.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.pbaltazar.blindo.entities.purchases.Product
import com.pbaltazar.blindo.entities.purchases.enums.ProductType
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableActivity
import com.pbaltazar.blindo.utils.extensions.isActive
import com.pbaltazar.blindo.utils.log.BlindoLogger
import org.koin.androidx.viewmodel.ext.android.viewModel

open class BilleableActivity : AuthenticableActivity(),
    BillingListener {

    private val billingViewModel: BillingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeCoinsHistory()
        subscribeCoins()
        subscribeMembership()
        subscribeConsumption()
        subscribePurchases()
        subscribeSubscriptionsToPurchase()
        subscribeInAppsToPurchase()
        subscribeBillingConnection()
    }

    override fun onResume() {
        super.onResume()
        if (isBillingConnected.not()) {
            startBillingConnection()
        } else {
            askForNewPurchases()
        }
    }

    override fun onDestroy() {
        if (isBillingConnected) {
            closeBillingConnection()
        }
        super.onDestroy()
    }

    val isBillingConnected: Boolean get() {
        return billingViewModel.isServiceConnected()
    }

    fun startBillingConnection() = billingViewModel.startConnection()

    fun closeBillingConnection() = billingViewModel.closeConnection()

    override fun onBillingConnection(billingConnection: BillingViewModel.BillingConnection) {
        when (billingConnection) {
            is BillingViewModel.BillingConnection.Connected -> askForNewPurchases()
            is BillingViewModel.BillingConnection.Error -> BlindoLogger.log.e(billingConnection.reason)
            else -> BlindoLogger.log.e(billingConnection.toString())
        }
    }

    private fun subscribeBillingConnection() = billingViewModel.isConnected.observe(this, Observer {
        onBillingConnection(it)
    })

    fun getInAppsToPurchase() = billingViewModel.getAvailableInApps()

    override fun onInAppsToPurchase(availableProducts: BillingViewModel.AvailableProducts) {
    }

    private fun subscribeInAppsToPurchase() = billingViewModel.inApps.observe(this, Observer {
        onInAppsToPurchase(it)
    })

    fun getSubscriptionsToPurchase() = billingViewModel.getAvailableSubscriptions()

    override fun onSubscriptionsToPurchase(availableProducts: BillingViewModel.AvailableProducts) {
    }

    private fun subscribeSubscriptionsToPurchase() = billingViewModel.subscriptions.observe(this, Observer {
        onSubscriptionsToPurchase(it)
    })

    fun askForNewInAppPurchases() = billingViewModel.askForPurchases(ProductType.INAPP)

    fun askForNewSubscriptionPurchases() = billingViewModel.askForPurchases(ProductType.SUBSCRIPTION)

    fun askForNewPurchases() {
        askForNewSubscriptionPurchases()
        askForNewInAppPurchases()
    }

    fun launchPurchase(products: List<Product>) = billingViewModel.launchPurchase(
        this as AppCompatActivity,
        products
    )

    override fun onNewPurchases(purchases: BillingViewModel.Purchases) {
        when (purchases) {
            is BillingViewModel.Purchases.Success -> purchases.purchases.also { newPurchases ->
                newPurchases.forEach { purchase ->
                    billingViewModel.sendPurchaseToApi(purchase)
                }
            }
            is BillingViewModel.Purchases.Empty -> Unit
            is BillingViewModel.Purchases.CanceledByUser -> {}
            is BillingViewModel.Purchases.Error -> {}
            is BillingViewModel.Purchases.FeatureNotSupported -> {}
            is BillingViewModel.Purchases.ServiceUnavailable -> {}
            is BillingViewModel.Purchases.Disconnected -> {}
        }
    }

    private fun subscribePurchases() = billingViewModel.purchases.observe(this, Observer {
        onNewPurchases(it)
    })

    override fun onProductConsumption(consumption: BillingViewModel.Consumption) {
        when (consumption) {
            is BillingViewModel.Consumption.Success -> Unit
            is BillingViewModel.Consumption.FeatureNotSupported -> {}
            is BillingViewModel.Consumption.Error -> {}
            is BillingViewModel.Consumption.ServiceUnavailable -> {}
            is BillingViewModel.Consumption.Disconnected -> {}
        }
    }

    private fun subscribeConsumption() = billingViewModel.consumption.observe(this, Observer {
        onProductConsumption(it)
    })

    override fun onCoinsPurchased(purchasedCoin: BillingViewModel.PurchasedCoin) {
        when (purchasedCoin) {
            is BillingViewModel.PurchasedCoin.Success -> purchasedCoin.coin.also { coin ->
                if (coin.isConsumed.not() && coin.latestPurchase.isAcknowledged) {
                    billingViewModel.consumePurchase(coin.token)
                    updateUserCoinsBalance()
                }
            }
            is BillingViewModel.PurchasedCoin.Error -> {}
            else -> {}
        }
    }

    private fun subscribeCoins() = billingViewModel.coins.observe(this, Observer {
        onCoinsPurchased(it)
    })

    fun getCoinsHistory() = billingViewModel.getCoinsHistory()

    override fun onCoinsHistory(coinsHistory: BillingViewModel.CoinsHistory) {
    }

    private fun subscribeCoinsHistory() = billingViewModel.coinsHistory.observe(this, Observer {
        onCoinsHistory(it)
    })

    fun getMembership() = billingViewModel.getMembership()

    override fun onMembershipPurchased(purchasedMembership: BillingViewModel.PurchasedMembership) {
        when (purchasedMembership) {
            is BillingViewModel.PurchasedMembership.Success -> purchasedMembership.membership.also { membership ->
                setIsUserPremium(membership.isActive())
            }
            is BillingViewModel.PurchasedMembership.Error -> {
                setIsUserPremium(false)
            }
            else -> {
                setIsUserPremium(false)
            }
        }
    }

    private fun subscribeMembership() = billingViewModel.membership.observe(this, Observer {
        onMembershipPurchased(it)
    })
}
