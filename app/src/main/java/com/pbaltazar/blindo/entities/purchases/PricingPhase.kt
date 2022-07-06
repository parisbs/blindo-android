package com.pbaltazar.blindo.entities.purchases

open class PricingPhase(
    val formatedPrice: String,
    val priceAmountMicros: Long,
    val priceCurrencyCode: String
) {
    fun getFormatedPriceWithCurrencyCode(): String =
        "$formatedPrice $priceCurrencyCode"
}
