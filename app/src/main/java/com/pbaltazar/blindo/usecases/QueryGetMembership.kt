package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.purchase.PurchaseGateway
import com.pbaltazar.blindo.entities.Membership
import com.pbaltazar.blindo.entities.responses.ApiResponse

class QueryGetMembership(
    private val purchaseGateway: PurchaseGateway
) {
    suspend operator fun invoke(idToken: String): ApiResponse<Membership> =
        purchaseGateway.getMembership(idToken)
}
