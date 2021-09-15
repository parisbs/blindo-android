package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.membership.MembershipGateway
import com.pbaltazar.blindo.entities.Membership
import com.pbaltazar.blindo.entities.responses.ApiResponse

class QueryGetMembership(
    private val membershipGateway: MembershipGateway
) {
    suspend operator fun invoke(idToken: String): ApiResponse<Membership> =
        membershipGateway.getMembership(idToken)
}
