package com.pbaltazar.blindo.data.user

import com.apollographql.apollo.api.Input
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.pbaltazar.blindo.data.ApiHelpers
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.inputs.UserInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.graphql.*
import com.pbaltazar.blindo.graphql.type.CreateUserInput
import com.pbaltazar.blindo.graphql.type.PackSortEnum
import com.pbaltazar.blindo.graphql.type.RatingSortEnum
import com.pbaltazar.blindo.graphql.type.UpdateUserInput
import com.pbaltazar.blindo.utils.extensions.toApiModel
import com.pbaltazar.blindo.utils.extensions.toGraphQLFilter
import com.pbaltazar.blindo.utils.extensions.toGraphQlFilter
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

    override suspend fun getPublicUser(userInput: UserInput): ApiResponse<User> =
        blindoApiClient.query(
            GetPublicUserQuery(
                id = userInput.id,
                packsFilters = Input.optional(userInput.packInput.filters?.toGraphQlFilter()),
                packsSort = userInput.packInput.sort.mapNotNull { PackSortEnum.valueOf(it.name) },
                packsFirst = userInput.packInput.pageSize,
                ratingsFilters = Input.optional(userInput.ratingInput.filters?.toGraphQLFilter()),
                ratingsSort = userInput.ratingInput.sort.mapNotNull { RatingSortEnum.valueOf(it.name) },
                ratingsFirst = userInput.ratingInput.pageSize
            )
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.getPublicUser?.let { user ->
                    ApiResponse.Success(
                        data = user.toApiModel(userInput.id)
                    )
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun getPublicUserPacks(userInput: UserInput): ApiResponse<User> =
        blindoApiClient.query(
            GetPublicUserPacksQuery(
                id = userInput.id,
                packsFilters = Input.optional(userInput.packInput.filters?.toGraphQlFilter()),
                packsSort = userInput.packInput.sort.mapNotNull { PackSortEnum.valueOf(it.name) },
                packsFirst = userInput.packInput.pageSize,
                packsAfter = Input.optional(userInput.packInput.nextPageToken)
            )
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.getPublicUser?.let { user ->
                    ApiResponse.Success(
                        data = user.toApiModel(userInput.id)
                    )
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun getPublicUserRatings(userInput: UserInput): ApiResponse<User> =
        blindoApiClient.query(
            GetPublicUserRatingsQuery(
                id = userInput.id,
                ratingsFilters = Input.optional(userInput.ratingInput.filters?.toGraphQLFilter()),
                ratingsSort = userInput.ratingInput.sort.mapNotNull { RatingSortEnum.valueOf(it.name) },
                ratingsFirst = userInput.ratingInput.pageSize,
                ratingsAfter = Input.optional(userInput.ratingInput.nextPageToken)
            )
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.getPublicUser?.let { user ->
                    ApiResponse.Success(
                        data = user.toApiModel(userInput.id)
                    )
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
