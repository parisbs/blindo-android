package com.pbaltazar.blindo.utils.extensions

import com.android.billingclient.api.*
import com.pbaltazar.blindo.entities.BlindoPurchase
import com.pbaltazar.blindo.entities.Sku
import com.pbaltazar.blindo.entities.errors.BillingException
import com.pbaltazar.blindo.entities.responses.BillingResponse

fun SkuDetailsResult.toApiResponse(): BillingResponse<List<Sku>> = this.let { response ->
    when (response.billingResult.responseCode) {
        BillingClient.BillingResponseCode.OK -> response.skuDetailsList?.let { skuList ->
            skuList.mapNotNull { it.toApiModel() }.let {
                BillingResponse.Success(it)
            }
        } ?: BillingResponse.Error(BillingException.EmptyResponse)
        BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> BillingResponse.Error(BillingException.FeatureNotSupported)
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> BillingResponse.Error(BillingException.ServiceUnavailable)
        BillingClient.BillingResponseCode.DEVELOPER_ERROR -> BillingResponse.Error(BillingException.InstanceError(response.billingResult.debugMessage))
        else -> BillingResponse.Error(BillingException.UnknownError(response.billingResult.debugMessage))
    }
}

fun BillingResult.toApiResponse(): BillingResponse<Boolean> = this.let { response ->
    when (response.responseCode) {
        BillingClient.BillingResponseCode.OK -> BillingResponse.Success(true)
        BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> BillingResponse.Error(BillingException.FeatureNotSupported)
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> BillingResponse.Error(BillingException.ServiceUnavailable)
        BillingClient.BillingResponseCode.DEVELOPER_ERROR -> BillingResponse.Error(BillingException.InstanceError(response.debugMessage))
        else -> BillingResponse.Error(BillingException.UnknownError(response.debugMessage))
    }
}

fun Purchase.toApiModel(): BlindoPurchase = BlindoPurchase(
    orderId = orderId,
    state = purchaseState,
    isAcknowledged = isAcknowledged,
    originalJson = originalJson
)

fun SkuDetails.toApiModel(): Sku = Sku(
    id = sku,
    type = type,
    name = title,
    description = description,
    period = subscriptionPeriod,
    price = price,
    currency = priceCurrencyCode,
    originalJson = originalJson
)
