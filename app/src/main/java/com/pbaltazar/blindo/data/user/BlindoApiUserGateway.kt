package com.pbaltazar.blindo.data.user

import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.pbaltazar.blindo.data.ApiHelpers
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.graphql.CreateUserMutation
import com.pbaltazar.blindo.graphql.GetUserQuery
import com.pbaltazar.blindo.graphql.UpdateUserMutation
import com.pbaltazar.blindo.graphql.type.CreateUserInput
import com.pbaltazar.blindo.graphql.type.UpdateUserInput
import com.pbaltazar.blindo.utils.extensions.toApiModel
import com.wizeline.simpleapollo.api.SimpleApolloClient
import com.wizeline.simpleapollo.models.Response

class BlindoApiUserGateway(
    private val blindoApiClient: SimpleApolloClient
) : ApiHelpers, UserGateway {

    override suspend fun getUser(sub: String, idToken: String): ApiResponse<User> =
        blindoApiClient.query(
            GetUserQuery(
                sub = sub
            ),
            idToken,
            HttpCachePolicy.NETWORK_ONLY
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.getUser?.let { user ->
                    ApiResponse.Success(user.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun createUser(user: User, idToken: String): ApiResponse<User> =
        blindoApiClient.mutate(
            CreateUserMutation(
                input = CreateUserInput(
                    sub = user.sub,
                    email = user.email,
                    name = user.name,
                    picture = Input.optional(user.picture)
                )
            ),
            idToken
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.createUser?.user?.let { user ->
                    ApiResponse.Success(user.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun updateUser(user: User, idToken: String): ApiResponse<User> =
        blindoApiClient.mutate(
            UpdateUserMutation(
                input = UpdateUserInput(
                    id = user.id,
                    name = Input.optional(user.name),
                    picture = Input.optional(user.picture)
                )
            ),
            idToken
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.updateUser?.user?.let { user ->
                    ApiResponse.Success(user.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }
}
