package com.pbaltazar.blindo.data.purchase

import com.blindo.apollito.api.ApollitoClient
import com.blindo.apollito.api.constants.FetchPolicy
import com.blindo.apollito.models.Response
import com.pbaltazar.blindo.data.ApiHelpers
import com.pbaltazar.blindo.entities.Membership
import com.pbaltazar.blindo.entities.ProcessPurchaseResult
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.inputs.ProcessPurchaseInput
import com.pbaltazar.blindo.entities.purchases.enums.ProductType
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.graphql.GetMembershipQuery
import com.pbaltazar.blindo.graphql.ProcessPurchaseMutation
import com.pbaltazar.blindo.graphql.type.PurchaseKindEnum
import com.pbaltazar.blindo.graphql.type.PurchaseSortEnum
import com.pbaltazar.blindo.utils.extensions.toApiModel
import com.pbaltazar.blindo.utils.extensions.toBlindoModel

class BlindoApiPurchaseGateway(
    private val blindoApiClient: ApollitoClient
) : ApiHelpers, PurchaseGateway {

    override suspend fun getMembership(idToken: String): ApiResponse<Membership> =
        blindoApiClient.query(
            GetMembershipQuery(
                purchasesSort = listOf(PurchaseSortEnum.CREATED_AT_DESC),
                purchasesFirst = 5
            ),
            idToken,
            FetchPolicy.NETWORK_ONLY
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.getMembership?.let { membership ->
                    ApiResponse.Success(membership.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun processPurchase(processPurchaseInput: ProcessPurchaseInput, idToken: String): ApiResponse<ProcessPurchaseResult> =
        blindoApiClient.mutate(
            ProcessPurchaseMutation(
                input = com.pbaltazar.blindo.graphql.type.ProcessPurchaseInput(
                    kind = when (processPurchaseInput.kind) {
                        ProductType.INAPP -> PurchaseKindEnum.INAPP
                        ProductType.SUBSCRIPTION -> PurchaseKindEnum.SUBSCRIPTION
                    },
                    idProduct = processPurchaseInput.productId,
                    token = processPurchaseInput.token
                ),
                purchasesSort = listOf(PurchaseSortEnum.CREATED_AT_DESC),
                purchasesFirst = processPurchaseInput.purchasesFirst
            ),
            idToken
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.processPurchase?.let { result ->
                    ApiResponse.Success(result.toBlindoModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }
}
