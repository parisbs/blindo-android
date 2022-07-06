package com.pbaltazar.blindo.entities.purchases.subscriptions

import com.android.billingclient.api.ProductDetails
import com.pbaltazar.blindo.entities.purchases.Product
import com.pbaltazar.blindo.entities.purchases.enums.ProductType

class Subscription(
    id: String,
    name: String = "",
    description: String = "",
    offers: List<SubscriptionOffer>? = null,
    originalProductDetailsObject: ProductDetails? = null,
    val benefitsResourceId: Int = -1,
    var selectedOffer: SubscriptionOffer? = null
) : Product(
    id,
    ProductType.SUBSCRIPTION,
    name,
    description,
    offers,
    originalProductDetailsObject
) {
    fun getSubscriptionOffers(): List<SubscriptionOffer>? =
        super.offers?.mapNotNull { it as SubscriptionOffer }
}
