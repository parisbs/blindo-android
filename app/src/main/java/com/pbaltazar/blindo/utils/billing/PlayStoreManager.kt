package com.pbaltazar.blindo.utils.billing

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.pbaltazar.blindo.entities.BlindoPurchase
import com.pbaltazar.blindo.entities.Sku
import com.pbaltazar.blindo.entities.errors.BillingException
import com.pbaltazar.blindo.entities.responses.BillingResponse
import com.pbaltazar.blindo.utils.extensions.toApiModel
import com.pbaltazar.blindo.utils.extensions.toApiResponse
import kotlinx.coroutines.channels.Channel
import org.json.JSONObject
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PlayStoreManager(
    private val context: Context
) : BillingManager {

    private var billingClient: BillingClient

    private var isConnected: Boolean = false

    private val connectionChannel: Channel<BillingResponse<Boolean>> = Channel(Channel.UNLIMITED)
    private val billingChannel: Channel<BillingResponse<List<BlindoPurchase>>> = Channel(Channel.UNLIMITED)

    init {
        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener { billingResult, purchases ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> purchases?.also { purchasesList ->
                        purchasesList.mapNotNull { it.toApiModel() }.apply {
                            billingChannel.trySend(BillingResponse.Success(this))
                        }
                    } ?: billingChannel.trySend(
                        BillingResponse.Error(
                            BillingException.EmptyResponse
                        )
                    )
                    BillingClient.BillingResponseCode.USER_CANCELED -> billingChannel.trySend(
                        BillingResponse.Error(
                            BillingException.CanceledByUser
                        )
                    )
                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> billingChannel.trySend(
                        BillingResponse.Error(
                            BillingException.ServiceUnavailable
                        )
                    )
                    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> billingChannel.trySend(
                        BillingResponse.Error(
                            BillingException.FeatureNotSupported
                        )
                    )
                    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> billingChannel.trySend(
                        BillingResponse.Error(
                            BillingException.InstanceError(billingResult.debugMessage)
                        )
                    )
                    else -> billingChannel.trySend(
                        BillingResponse.Error(
                            BillingException.UnknownError(billingResult.debugMessage)
                        )
                    )
                }
            }
            .build()
    }

    override fun isConnected(): Boolean = isConnected

    override suspend fun startConnection(): BillingResponse<Boolean> {
            billingClient.startConnection(object: BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    when (val responseCode = billingResult.responseCode) {
                        BillingClient.BillingResponseCode.OK -> {
                            isConnected = true
                            connectionChannel.trySend(BillingResponse.Success(true))
                        }
                        else -> {
                            isConnected = false
                            when (responseCode) {
                                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> connectionChannel.trySend(BillingResponse.Error(BillingException.FeatureNotSupported))
                                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> connectionChannel.trySend(BillingResponse.Error(BillingException.ServiceUnavailable))
                                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> connectionChannel.trySend(
                                    BillingResponse.Error(
                                        BillingException.InstanceError(
                                            billingResult.debugMessage
                                        )
                                    )
                                )
                                else -> connectionChannel.trySend(
                                    BillingResponse.Error(
                                        BillingException.UnknownError(billingResult.debugMessage)
                                    )
                                )
                            }
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    isConnected = false
                    connectionChannel.trySend(BillingResponse.Error(BillingException.Disconnected))
                }
            })
        return connectionChannel.receive()
    }

    override fun closeConnection() {
        Timber.e("Billing service disconnected by scope")
        isConnected = false
        billingClient.endConnection()
    }

    override suspend fun getSkus(skuList: List<String>): BillingResponse<List<Sku>> = billingClient.querySkuDetails(
                SkuDetailsParams.newBuilder()
                    .setType(BillingClient.SkuType.SUBS)
                    .setSkusList(skuList)
                    .build()
            ).toApiResponse()

    override suspend fun launchBilling(
        activity: AppCompatActivity,
        sku: Sku
    ): BillingResponse<List<BlindoPurchase>> {
            billingClient.launchBillingFlow(
                activity,
                BillingFlowParams.newBuilder()
                    .setSkuDetails(SkuDetails(sku.originalJson))
                    .build()
            )
            return billingChannel.receive()
    }

    override suspend fun acknowledgeBilling(blindoPurchase: BlindoPurchase): BillingResponse<Boolean> = billingClient.acknowledgePurchase(
                AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(
                        JSONObject(blindoPurchase.originalJson)
                            .optString("purchaseToken")
                    )
                    .build()
            ).toApiResponse()

    override suspend fun getBillings(): BillingResponse<List<BlindoPurchase>> =
        suspendCoroutine { continuation ->
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.SkuType.SUBS)
                    .build()
            ) { billingResult, purchasesList ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> purchasesList?.also { purchases ->
                        purchases.mapNotNull { it.toApiModel() }.apply {
                            continuation.resume(BillingResponse.Success(this))
                        }
                    } ?: continuation.resume(BillingResponse.Error(BillingException.EmptyResponse))
                    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> continuation.resume(
                        BillingResponse.Error(
                            BillingException.FeatureNotSupported
                        )
                    )
                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> continuation.resume(
                        BillingResponse.Error(
                            BillingException.ServiceUnavailable
                        )
                    )
                    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> continuation.resume(
                        BillingResponse.Error(
                            BillingException.InstanceError(billingResult.debugMessage)
                        )
                    )
                    else -> continuation.resume(
                        BillingResponse.Error(
                            BillingException.UnknownError(billingResult.debugMessage)
                        )
                    )
                }
            }
    }
}
