package com.pbaltazar.blindo.utils.extensions

import com.android.billingclient.api.*
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.entities.errors.BillingException
import com.pbaltazar.blindo.entities.purchases.PricingPhase
import com.pbaltazar.blindo.entities.purchases.Product
import com.pbaltazar.blindo.entities.purchases.Purchase
import com.pbaltazar.blindo.entities.purchases.enums.ProductType
import com.pbaltazar.blindo.entities.purchases.inapp.InApp
import com.pbaltazar.blindo.entities.purchases.inapp.InAppOffer
import com.pbaltazar.blindo.entities.purchases.subscriptions.Subscription
import com.pbaltazar.blindo.entities.purchases.subscriptions.SubscriptionOffer
import com.pbaltazar.blindo.entities.purchases.subscriptions.SubscriptionPricingPhase
import com.pbaltazar.blindo.entities.purchases.subscriptions.SubscriptionRecurrenceMode
import com.pbaltazar.blindo.entities.responses.BillingResponse

fun BillingResult.toBlindoModel(): BillingResponse<Boolean> = when (responseCode) {
    BillingClient.BillingResponseCode.OK -> BillingResponse.Success(true)
    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> BillingResponse.Error(BillingException.FeatureNotSupported)
    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> BillingResponse.Error(BillingException.ServiceUnavailable)
    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> BillingResponse.Error(BillingException.InstanceError(debugMessage))
    else -> BillingResponse.Error(BillingException.UnknownError(debugMessage))
}

fun com.android.billingclient.api.Purchase.toBlindoModel(): Purchase = Purchase(
    type = getProductType(),
    productId = products.first(),
    token = purchaseToken,
    isAcknowledged = isAcknowledged
)

fun com.android.billingclient.api.Purchase.getProductType(): ProductType {
    return if (products.size >= 1) {
        when (products.first()) {
            "blindo_membership" -> ProductType.SUBSCRIPTION
            "blindo_coins_500" -> ProductType.INAPP
            else -> ProductType.INAPP
        }
    } else {
        ProductType.INAPP
    }
}

fun List<Product>.toQueryProductDetailsParams(): QueryProductDetailsParams =
    QueryProductDetailsParams.newBuilder()
        .setProductList(mapNotNull { it.toPlayStoreProduct() })
        .build()

fun Product.toPlayStoreProduct(): QueryProductDetailsParams.Product =
    QueryProductDetailsParams.Product.newBuilder()
        .setProductId(id)
        .setProductType(
            when (type) {
                ProductType.INAPP -> BillingClient.ProductType.INAPP
                ProductType.SUBSCRIPTION -> BillingClient.ProductType.SUBS
            }
        )
        .build()

fun ProductDetails.toBlindoModel(): Product {
    return when (productType) {
        BillingClient.ProductType.INAPP -> InApp(
            id = productId,
            name = name,
            description = description,
            offer = oneTimePurchaseOfferDetails?.toBlindoModel(),
            originalProductDetailsObject = this
        )
        BillingClient.ProductType.SUBS -> Subscription(
            id = productId,
            name = name,
            description = description,
            offers = subscriptionOfferDetails?.mapNotNull { it.toBlindoModel() },
            originalProductDetailsObject = this,
            benefitsResourceId = when (productId) {
                "blindo_membership" -> R.array.membership__benefits_classic_membership
                else -> R.array.membership__benefits_classic_membership
            }
        )
        else -> throw IllegalArgumentException("Unknown product type from billing source.")
    }
}

fun ProductDetails.OneTimePurchaseOfferDetails.toBlindoModel(): InAppOffer = InAppOffer(
    PricingPhase(
        formatedPrice = formattedPrice,
        priceAmountMicros = priceAmountMicros,
        priceCurrencyCode = priceCurrencyCode
    )
)

fun ProductDetails.SubscriptionOfferDetails.toBlindoModel(): SubscriptionOffer = SubscriptionOffer(
    offerTags = offerTags,
    offerToken = offerToken,
    pricingPhases = pricingPhases.toBlindoModel()
)

fun ProductDetails.PricingPhases.toBlindoModel(): List<SubscriptionPricingPhase> =
    pricingPhaseList.mapNotNull { it.toBlindoModel() }

fun ProductDetails.PricingPhase.toBlindoModel(): SubscriptionPricingPhase = SubscriptionPricingPhase(
    billingCycleCount = billingCycleCount,
    billingPeriod = billingPeriod,
    formatedPrice = formattedPrice,
    priceAmountMicros = priceAmountMicros,
    priceCurrencyCode = priceCurrencyCode,
    recurrenceMode = when (recurrenceMode) {
        1 -> SubscriptionRecurrenceMode.INFINITE_RECURRING
        2 -> SubscriptionRecurrenceMode.FINITE_RECURRING
        3 -> SubscriptionRecurrenceMode.NON_RECURRING
        else -> SubscriptionRecurrenceMode.INFINITE_RECURRING
    }
)

fun List<Product>.toBillingFlowParams(): BillingFlowParams =
    BillingFlowParams.newBuilder()
        .setProductDetailsParamsList(mapNotNull { it.toProductDetailsParams() })
        .build()

fun Product.toProductDetailsParams(): BillingFlowParams.ProductDetailsParams {
    val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
    originalProductDetailsObject?.apply {
        productDetailsParams.setProductDetails(this)
    } ?: throw NullPointerException("ProductDetails object cannot be null.")
    if (this is Subscription) {
        selectedOffer?.apply {
            productDetailsParams.setOfferToken(this.offerToken)
        } ?: getSubscriptionOffers()?.first()?.offerToken ?: throw NullPointerException("Subscription offer token cannot be null.")
    }
    return productDetailsParams.build()
}

fun ProductType.toQueryPurchasesParams(): QueryPurchasesParams = QueryPurchasesParams.newBuilder()
    .setProductType(
        when (this) {
            ProductType.INAPP -> BillingClient.ProductType.INAPP
            ProductType.SUBSCRIPTION -> BillingClient.ProductType.SUBS
        }
    )
    .build()
