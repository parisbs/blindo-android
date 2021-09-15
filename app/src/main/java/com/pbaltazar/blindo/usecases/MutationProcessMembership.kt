package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.membership.MembershipGateway
import com.pbaltazar.blindo.entities.BlindoPurchase
import com.pbaltazar.blindo.entities.Membership
import com.pbaltazar.blindo.entities.responses.ApiResponse

class MutationProcessMembership(
    private val membershipGateway: MembershipGateway
) {
    suspend operator fun invoke(
        blindoPurchase: BlindoPurchase,
        idToken: String
    ): ApiResponse<Membership> =
        membershipGateway.processMembership(blindoPurchase, idToken)
}
