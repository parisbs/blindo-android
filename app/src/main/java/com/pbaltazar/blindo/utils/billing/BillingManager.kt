package com.pbaltazar.blindo.utils.billing

import androidx.appcompat.app.AppCompatActivity
import com.pbaltazar.blindo.entities.BlindoPurchase
import com.pbaltazar.blindo.entities.Sku
import com.pbaltazar.blindo.entities.responses.BillingResponse

interface BillingManager {

    fun isConnected(): Boolean

    suspend fun startConnection(): BillingResponse<Boolean>

    fun closeConnection()

    suspend fun getSkus(skuList: List<String>): BillingResponse<List<Sku>>

    suspend fun launchBilling(
        activity: AppCompatActivity,
        sku: Sku
    ): BillingResponse<List<BlindoPurchase>>

    suspend fun acknowledgeBilling(blindoPurchase: BlindoPurchase): BillingResponse<Boolean>

    suspend fun getBillings(): BillingResponse<List<BlindoPurchase>>
}
