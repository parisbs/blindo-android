package com.pbaltazar.blindo.utils.billing

import androidx.appcompat.app.AppCompatActivity
import com.pbaltazar.blindo.entities.purchases.Product
import com.pbaltazar.blindo.entities.purchases.Purchase
import com.pbaltazar.blindo.entities.purchases.enums.ProductType
import com.pbaltazar.blindo.entities.responses.BillingResponse
import kotlinx.coroutines.flow.Flow

interface BillingManager {

    fun isConnected(): Boolean

    fun connectionStateFlow(): Flow<BillingResponse<Boolean>>

    fun purchasesFlow(): Flow<BillingResponse<List<Purchase>>>

    fun startConnection()

    fun closeConnection()

    suspend fun getProductDetails(products: List<Product>): BillingResponse<List<Product>>

    suspend fun launchBilling(activity: AppCompatActivity, products: List<Product>): BillingResponse<Boolean>

    fun askForPurchases(productType: ProductType)
}
