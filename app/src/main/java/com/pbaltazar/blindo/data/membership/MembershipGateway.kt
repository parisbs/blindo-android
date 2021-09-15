package com.pbaltazar.blindo.data.membership

import com.pbaltazar.blindo.entities.BlindoPurchase
import com.pbaltazar.blindo.entities.Membership
import com.pbaltazar.blindo.entities.responses.ApiResponse

interface MembershipGateway {

    suspend fun getMembership(idToken: String): ApiResponse<Membership>

    suspend fun processMembership(
        blindoPurchase: BlindoPurchase,
        idToken: String
    ): ApiResponse<Membership>
}
