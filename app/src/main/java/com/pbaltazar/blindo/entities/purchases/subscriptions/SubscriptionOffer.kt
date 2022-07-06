package com.pbaltazar.blindo.entities.purchases.subscriptions

import com.pbaltazar.blindo.entities.purchases.Offer

class SubscriptionOffer(
    val offerTags: List<String>,
    val offerToken: String,
    pricingPhases: List<SubscriptionPricingPhase>
) : Offer(
    pricingPhases
) {
    fun getSubscriptionPricingPhases(): List<SubscriptionPricingPhase> =
        super.pricingPhases.mapNotNull { it as SubscriptionPricingPhase }
}
