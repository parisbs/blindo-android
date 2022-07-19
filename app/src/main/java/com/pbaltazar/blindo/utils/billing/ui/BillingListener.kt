package com.pbaltazar.blindo.utils.billing.ui

interface BillingListener {
    fun onBillingConnection(billingConnection: BillingViewModel.BillingConnection)
    fun onInAppsToPurchase(availableProducts: BillingViewModel.AvailableProducts)
    fun onSubscriptionsToPurchase(availableProducts: BillingViewModel.AvailableProducts)
    fun onNewPurchases(purchases: BillingViewModel.Purchases)
    fun onProductConsumption(consumption: BillingViewModel.Consumption)
    fun onCoinsPurchased(purchasedCoin: BillingViewModel.PurchasedCoin)
    fun onCoinsHistory(coinsHistory: BillingViewModel.CoinsHistory)
    fun onMembershipPurchased(purchasedMembership: BillingViewModel.PurchasedMembership)
}
