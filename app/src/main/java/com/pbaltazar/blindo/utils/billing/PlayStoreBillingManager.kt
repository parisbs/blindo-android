package com.pbaltazar.blindo.utils.billing

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.queryProductDetails
import com.pbaltazar.blindo.entities.errors.BillingException
import com.pbaltazar.blindo.entities.purchases.Product
import com.pbaltazar.blindo.entities.purchases.Purchase
import com.pbaltazar.blindo.entities.purchases.enums.ProductType
import com.pbaltazar.blindo.entities.responses.BillingResponse
import com.pbaltazar.blindo.utils.extensions.toBillingFlowParams
import com.pbaltazar.blindo.utils.extensions.toBlindoModel
import com.pbaltazar.blindo.utils.extensions.toQueryProductDetailsParams
import com.pbaltazar.blindo.utils.extensions.toQueryPurchasesParams
import com.pbaltazar.blindo.utils.log.BlindoLogger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class PlayStoreBillingManager(
    private val context: Context
) : BillingManager {

    private var billingClient: BillingClient

    private var isConnected: Boolean = false

    private val connectionChannel: Channel<BillingResponse<Boolean>> = Channel(Channel.UNLIMITED)
    private val purchasesChannel: Channel<BillingResponse<List<Purchase>>> = Channel(Channel.UNLIMITED)

    init {
        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener { billingResult, purchases ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> purchases?.takeUnless { it.isEmpty() }?.also { purchasesList ->
                        purchasesChannel.trySend(BillingResponse.Success(purchasesList.mapNotNull { it.toBlindoModel() }))
                    } ?: purchasesChannel.trySend(BillingResponse.Error(BillingException.EmptyResponse))
                    BillingClient.BillingResponseCode.USER_CANCELED -> purchasesChannel.trySend(BillingResponse.Error(BillingException.CanceledByUser))
                    BillingClient.BillingResponseCode.DEVELOPER_ERROR,
                        BillingClient.BillingResponseCode.ERROR -> purchasesChannel.trySend(BillingResponse.Error(BillingException.InstanceError(billingResult.debugMessage)))
                    BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> purchasesChannel.trySend(BillingResponse.Error(BillingException.FeatureNotSupported))
                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
                        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                        BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> purchasesChannel.trySend(BillingResponse.Error(BillingException.ServiceUnavailable))
                    BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> purchasesChannel.trySend(BillingResponse.Error(BillingException.Disconnected))
                    else -> purchasesChannel.trySend(BillingResponse.Error(BillingException.UnknownError(billingResult.debugMessage)))
                }
            }
            .build()
    }

    override fun isConnected(): Boolean = isConnected

    override fun connectionStateFlow(): Flow<BillingResponse<Boolean>> = connectionChannel.receiveAsFlow()

    override fun purchasesFlow(): Flow<BillingResponse<List<Purchase>>> = purchasesChannel.receiveAsFlow()

    override fun startConnection() {
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
                                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> connectionChannel.trySend(BillingResponse.Error(BillingException.InstanceError(billingResult.debugMessage)))
                                else -> connectionChannel.trySend(BillingResponse.Error(BillingException.UnknownError(billingResult.debugMessage)))
                            }
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    isConnected = false
                    connectionChannel.trySend(BillingResponse.Error(BillingException.Disconnected))
                }
            })
    }

    override fun closeConnection() {
        BlindoLogger.log.e("Billing service disconnected by scope")
        isConnected = false
        billingClient.endConnection()
    }

    override suspend fun getProductDetails(products: List<Product>): BillingResponse<List<Product>> =
        billingClient.queryProductDetails(products.toQueryProductDetailsParams()).let { productDetailsResult ->
            val billingResult = productDetailsResult.billingResult
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> productDetailsResult.productDetailsList?.takeUnless { it.isEmpty() }?.let { productDetailsList ->
                    BillingResponse.Success(productDetailsList.mapNotNull { it.toBlindoModel() })
                } ?: BillingResponse.Error(BillingException.EmptyResponse)
                BillingClient.BillingResponseCode.DEVELOPER_ERROR,
                BillingClient.BillingResponseCode.ERROR -> BillingResponse.Error(BillingException.InstanceError(billingResult.debugMessage))
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> BillingResponse.Error(BillingException.FeatureNotSupported)
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> BillingResponse.Error(BillingException.ServiceUnavailable)
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> BillingResponse.Error(BillingException.Disconnected)
                else -> BillingResponse.Error(BillingException.UnknownError(billingResult.debugMessage))
            }
        }

    override suspend fun launchBilling(activity: AppCompatActivity, products: List<Product>): BillingResponse<Boolean> =
        billingClient.launchBillingFlow(activity, products.toBillingFlowParams()).toBlindoModel()

    override fun askForPurchases(productType: ProductType) {
        billingClient.queryPurchasesAsync(productType.toQueryPurchasesParams()) { billingResult, purchasesList ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> purchasesList.takeUnless { it.isEmpty() }?.also { purchases ->
                    purchasesChannel.trySend(BillingResponse.Success(purchasesList.mapNotNull { it.toBlindoModel() }))
                } ?: purchasesChannel.trySend(BillingResponse.Error(BillingException.EmptyResponse))
                BillingClient.BillingResponseCode.USER_CANCELED -> purchasesChannel.trySend(BillingResponse.Error(BillingException.CanceledByUser))
                BillingClient.BillingResponseCode.DEVELOPER_ERROR,
                    BillingClient.BillingResponseCode.ERROR -> purchasesChannel.trySend(BillingResponse.Error(BillingException.InstanceError(billingResult.debugMessage)))
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> purchasesChannel.trySend(BillingResponse.Error(BillingException.FeatureNotSupported))
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
                    BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> purchasesChannel.trySend(BillingResponse.Error(BillingException.ServiceUnavailable))
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> purchasesChannel.trySend(BillingResponse.Error(BillingException.Disconnected))
                else -> purchasesChannel.trySend(BillingResponse.Error(BillingException.UnknownError(billingResult.debugMessage)))
            }
        }
    }
}
