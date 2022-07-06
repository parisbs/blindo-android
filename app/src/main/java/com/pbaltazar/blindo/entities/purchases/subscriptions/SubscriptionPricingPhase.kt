package com.pbaltazar.blindo.entities.purchases.subscriptions

import com.pbaltazar.blindo.entities.purchases.PricingPhase

class SubscriptionPricingPhase(
    val billingCycleCount: Int,
    val billingPeriod: String,
    formatedPrice: String,
    priceAmountMicros: Long,
    priceCurrencyCode: String,
    val recurrenceMode: SubscriptionRecurrenceMode
) : PricingPhase(
    formatedPrice,
    priceAmountMicros,
    priceCurrencyCode
)
