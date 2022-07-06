package com.pbaltazar.blindo.entities.purchases.inapp

import com.pbaltazar.blindo.entities.purchases.Offer
import com.pbaltazar.blindo.entities.purchases.PricingPhase

class InAppOffer(
    val pricingPhase: PricingPhase
) : Offer(
    listOf(pricingPhase)
)
