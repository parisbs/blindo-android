package com.pbaltazar.blindo.data.purchase

import com.pbaltazar.blindo.entities.Coin
import com.pbaltazar.blindo.entities.Membership
import com.pbaltazar.blindo.entities.ProcessPurchaseResult
import com.pbaltazar.blindo.entities.inputs.ListCoinsInput
import com.pbaltazar.blindo.entities.inputs.ProcessPurchaseInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

interface PurchaseGateway {

    suspend fun listCoins(listCoinsInput: ListCoinsInput): ApiResponse<List<Coin>>

    suspend fun getMembership(idToken: String): ApiResponse<Membership>

    suspend fun processPurchase(processPurchaseInput: ProcessPurchaseInput, idToken: String): ApiResponse<ProcessPurchaseResult>
}
