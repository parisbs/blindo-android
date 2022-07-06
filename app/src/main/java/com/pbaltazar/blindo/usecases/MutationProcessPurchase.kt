package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.purchase.PurchaseGateway
import com.pbaltazar.blindo.entities.ProcessPurchaseResult
import com.pbaltazar.blindo.entities.inputs.ProcessPurchaseInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

class MutationProcessPurchase(
    private val purchaseGateway: PurchaseGateway
) {
    suspend operator fun invoke(processPurchaseInput: ProcessPurchaseInput, idToken: String): ApiResponse<ProcessPurchaseResult> =
        purchaseGateway.processPurchase(processPurchaseInput, idToken)
}
