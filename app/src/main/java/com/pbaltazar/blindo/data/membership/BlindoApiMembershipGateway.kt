package com.pbaltazar.blindo.data.membership

import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.pbaltazar.blindo.data.ApiHelpers
import com.pbaltazar.blindo.entities.BlindoPurchase
import com.pbaltazar.blindo.entities.Membership
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.graphql.GetMembershipQuery
import com.pbaltazar.blindo.graphql.ProcessMembershipMutation
import com.pbaltazar.blindo.graphql.type.ProcessMembershipInput
import com.pbaltazar.blindo.utils.extensions.toApiModel
import com.pbaltazar.blindo.utils.extensions.toBlindoReceipt
import com.wizeline.simpleapollo.api.SimpleApolloClient
import com.wizeline.simpleapollo.models.Response

class BlindoApiMembershipGateway(
    private val blindoApiClient: SimpleApolloClient
) : ApiHelpers, MembershipGateway {
    override suspend fun getMembership(idToken: String): ApiResponse<Membership> =
        blindoApiClient.query(
            GetMembershipQuery(),
            idToken,
            HttpCachePolicy.NETWORK_ONLY
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.getMembership?.let { membership ->
                    ApiResponse.Success(membership.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun processMembership(
        blindoPurchase: BlindoPurchase,
        idToken: String
    ): ApiResponse<Membership> =
        blindoApiClient.mutate(
            ProcessMembershipMutation(
                input = ProcessMembershipInput(
                    receipt = blindoPurchase.originalJson.toBlindoReceipt()
                )
            ),
            idToken
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.processMembership?.membership?.let { membership ->
                    ApiResponse.Success(membership.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }
}
