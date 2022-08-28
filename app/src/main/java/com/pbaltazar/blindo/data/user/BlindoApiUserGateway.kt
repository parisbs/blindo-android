package com.pbaltazar.blindo.data.user

import com.apollographql.apollo3.api.Optional
import com.blindo.apollito.api.ApollitoClient
import com.blindo.apollito.api.constants.FetchPolicy
import com.blindo.apollito.models.Response
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

class BlindoApiUserGateway(
    private val blindoApiClient: ApollitoClient
) : ApiHelpers, UserGateway {

    override suspend fun authenticateUser(userInput: UserInput): ApiResponse<User> =
        blindoApiClient.query(
            AuthenticateUserQuery(),
            userInput.idToken,
            FetchPolicy.NETWORK_ONLY
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.authenticateUser?.let { user ->
                    ApiResponse.Success(user.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun getUserCoinsBalance(userInput: UserInput): ApiResponse<Int> =
        blindoApiClient.query(
            GetUserCoinsBalanceQuery(),
            userInput.idToken,
            FetchPolicy.NETWORK_ONLY
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.authenticateUser?.let { user ->
                    ApiResponse.Success(user.coinsLeft)
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun getPublicUser(userInput: UserInput): ApiResponse<User> =
        blindoApiClient.query(
            GetPublicUserQuery(
                id = userInput.id,
                packsFilters = Optional.presentIfNotNull(userInput.packInput.filters?.toGraphQlFilter()),
                packsSort = userInput.packInput.sort.map { PackSortEnum.valueOf(it.name) },
                packsFirst = userInput.packInput.pageSize,
                ratingsFilters = Optional.presentIfNotNull(userInput.ratingInput.filters?.toGraphQLFilter()),
                ratingsSort = userInput.ratingInput.sort.map { RatingSortEnum.valueOf(it.name) },
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
                packsFilters = Optional.presentIfNotNull(userInput.packInput.filters?.toGraphQlFilter()),
                packsSort = userInput.packInput.sort.map { PackSortEnum.valueOf(it.name) },
                packsFirst = userInput.packInput.pageSize,
                packsAfter = Optional.presentIfNotNull(userInput.packInput.nextPageToken)
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
                ratingsFilters = Optional.presentIfNotNull(userInput.ratingInput.filters?.toGraphQLFilter()),
                ratingsSort = userInput.ratingInput.sort.map { RatingSortEnum.valueOf(it.name) },
                ratingsFirst = userInput.ratingInput.pageSize,
                ratingsAfter = Optional.presentIfNotNull(userInput.ratingInput.nextPageToken)
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
                    picture = Optional.presentIfNotNull(user.picture)
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
                    name = Optional.presentIfNotNull(user.name),
                    picture = Optional.presentIfNotNull(user.picture)
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
