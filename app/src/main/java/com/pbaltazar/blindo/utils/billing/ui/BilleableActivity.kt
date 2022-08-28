package com.pbaltazar.blindo.utils.billing.ui

import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.entities.Coin
import com.pbaltazar.blindo.entities.Membership
import com.pbaltazar.blindo.entities.purchases.Product
import com.pbaltazar.blindo.entities.purchases.enums.ProductType
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableActivity
import com.pbaltazar.blindo.utils.constants.PURCHASES_NOTIFICATION_CHANNEL
import com.pbaltazar.blindo.utils.extensions.isActive
import com.pbaltazar.blindo.utils.extensions.numberOfCoins
import com.pbaltazar.blindo.utils.log.BlindoLogger
import com.pbaltazar.blindo.utils.notifications.NotificationsManager
import org.koin.androidx.viewmodel.ext.android.viewModel

open class BilleableActivity : AuthenticableActivity(),
    BillingListener {

    private val billingViewModel: BillingViewModel by viewModel()

    companion object {
        const val SUBSCRIPTION_MANAGEMENT_URI = "https://play.google.com/store/account/subscriptions?sku=%s&package=%s"
    }

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

    fun registerPurchasesNotificationChannel() {
        if (NotificationsManager.isInitialized.not()) {
            NotificationsManager.initialize(this)
        }
        NotificationsManager.createNotificationChannel(
            PURCHASES_NOTIFICATION_CHANNEL,
            getString(R.string.notification__purchases_channel_title),
            getString(R.string.notification__purchases_channel_description),
            NotificationManager.IMPORTANCE_HIGH
        )
    }

    val isBillingConnected: Boolean get() {
        return billingViewModel.isServiceConnected()
    }

    fun startBillingConnection() = billingViewModel.startConnection()

    fun closeBillingConnection() = billingViewModel.closeConnection()

    override fun onBillingConnection(billingConnection: BillingViewModel.BillingConnection) {
        when (billingConnection) {
            is BillingViewModel.BillingConnection.Connected -> askForNewPurchases()
            is BillingViewModel.BillingConnection.Error -> BlindoLogger.e(billingConnection.reason)
            else -> BlindoLogger.e(billingConnection.toString())
        }
    }

    private fun subscribeBillingConnection() = billingViewModel.isConnected.observe(this) {
        onBillingConnection(it)
    }

    fun getInAppsToPurchase() = billingViewModel.getAvailableInApps()

    override fun onInAppsToPurchase(availableProducts: BillingViewModel.AvailableProducts) {
    }

    private fun subscribeInAppsToPurchase() = billingViewModel.inApps.observe(this) {
        onInAppsToPurchase(it)
    }

    fun getSubscriptionsToPurchase() = billingViewModel.getAvailableSubscriptions()

    override fun onSubscriptionsToPurchase(availableProducts: BillingViewModel.AvailableProducts) {
    }

    private fun subscribeSubscriptionsToPurchase() = billingViewModel.subscriptions.observe(this) {
        onSubscriptionsToPurchase(it)
    }

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
            is BillingViewModel.Purchases.Success -> purchases.purchases.onEach { purchase ->
                billingViewModel.sendPurchaseToApi(purchase)
            }
            is BillingViewModel.Purchases.Empty -> Unit
            is BillingViewModel.Purchases.CanceledByUser -> {}
            is BillingViewModel.Purchases.Error -> {}
            is BillingViewModel.Purchases.FeatureNotSupported -> {}
            is BillingViewModel.Purchases.ServiceUnavailable -> {}
            is BillingViewModel.Purchases.Disconnected -> {}
        }
    }

    private fun subscribePurchases() = billingViewModel.purchases.observe(this) {
        onNewPurchases(it)
    }

    override fun onProductConsumption(consumption: BillingViewModel.Consumption) {
        when (consumption) {
            is BillingViewModel.Consumption.Success -> Unit
            is BillingViewModel.Consumption.FeatureNotSupported -> {}
            is BillingViewModel.Consumption.Error -> {}
            is BillingViewModel.Consumption.ServiceUnavailable -> {}
            is BillingViewModel.Consumption.Disconnected -> {}
        }
    }

    private fun subscribeConsumption() = billingViewModel.consumption.observe(this) {
        onProductConsumption(it)
    }

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

    private fun subscribeCoins() = billingViewModel.coins.observe(this) {
        onCoinsPurchased(it)
    }

    fun getCoinsHistory() = billingViewModel.getCoinsHistory()

    override fun onCoinsHistory(coinsHistory: BillingViewModel.CoinsHistory) {
    }

    private fun subscribeCoinsHistory() = billingViewModel.coinsHistory.observe(this) {
        onCoinsHistory(it)
    }

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

    private fun subscribeMembership() = billingViewModel.membership.observe(this) {
        onMembershipPurchased(it)
    }

    fun launchSubscriptionManagementPage(productId: String) {
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                SUBSCRIPTION_MANAGEMENT_URI.format(
                    productId,
                    packageName
                )
            )
        ).apply {
            startActivity(this)
        }
    }

    fun showNewCoinsNotification(coin: Coin) {
        if (NotificationsManager.isInitialized.not()) {
            return
        }
        NavDeepLinkBuilder(this)
            .setGraph(R.navigation.main_navigation)
            .addDestination(R.id.navCoins)
            .createPendingIntent().also { pendingIntent ->
                NotificationsManager.createSimpleNotification(
                    icon = R.drawable.ic_blindo_192dp,
                    title = getString(R.string.notification__purchases_new_coins_title),
                    body = getString(
                        R.string.notification__purchases_new_coins_body,
                        coin.numberOfCoins()
                    ),
                    channelId = PURCHASES_NOTIFICATION_CHANNEL,
                    priority = NotificationCompat.PRIORITY_HIGH,
                    pendingIntent = pendingIntent
                ).also { notification ->
                    NotificationsManager.notify(coin.hashCode(), notification)
                }
            }
    }

    fun showNewMembershipNotification(membership: Membership) {
        if (NotificationsManager.isInitialized.not()) {
            return
        }
        NavDeepLinkBuilder(this)
            .setGraph(R.navigation.main_navigation)
            .addDestination(R.id.navMembership)
            .createPendingIntent().also { pendingIntent ->
                NotificationsManager.createSimpleNotification(
                    icon = R.drawable.ic_blindo_192dp,
                    title = getString(R.string.notification__purchases_new_membership_title),
                    getString(
                        R.string.notification__purchases_new_membership_body,
                        membership.productId.let { when (it) {
                            "blindo_monthly_subscription", "blindo_membership" -> getString(R.string.notification__purchases_new_membership_classic)
                            else -> getString(R.string.notification__purchases_new_membership_classic)
                        } }
                    )
                ,
                    channelId = PURCHASES_NOTIFICATION_CHANNEL,
                    priority = NotificationCompat.PRIORITY_HIGH,
                    pendingIntent = pendingIntent
                ).also { notification ->
                    NotificationsManager.notify(membership.hashCode(), notification)
                }
            }
    }
}
