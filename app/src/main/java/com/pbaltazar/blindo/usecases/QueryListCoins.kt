package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.purchase.PurchaseGateway
import com.pbaltazar.blindo.entities.Coin
import com.pbaltazar.blindo.entities.inputs.ListCoinsInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

class QueryListCoins(
    private val purchaseGateway: PurchaseGateway
) {
    suspend operator fun invoke(listCoinsInput: ListCoinsInput): ApiResponse<List<Coin>> =
        purchaseGateway.listCoins(listCoinsInput)
}
