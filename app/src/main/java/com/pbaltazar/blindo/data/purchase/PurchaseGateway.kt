package com.pbaltazar.blindo.data.purchase

import com.pbaltazar.blindo.entities.Membership
import com.pbaltazar.blindo.entities.ProcessPurchaseResult
import com.pbaltazar.blindo.entities.inputs.ProcessPurchaseInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

interface PurchaseGateway {

    suspend fun getMembership(idToken: String): ApiResponse<Membership>

    suspend fun processPurchase(processPurchaseInput: ProcessPurchaseInput, idToken: String): ApiResponse<ProcessPurchaseResult>
}
