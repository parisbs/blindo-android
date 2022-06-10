package com.pbaltazar.blindo.data.rating

import com.apollographql.apollo3.api.Optional
import com.blindo.apollito.api.ApollitoClient
import com.blindo.apollito.models.Response
import com.pbaltazar.blindo.data.ApiHelpers
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.inputs.RatingInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.graphql.*
import com.pbaltazar.blindo.graphql.type.CreateRatingInput
import com.pbaltazar.blindo.graphql.type.RatingFilter
import com.pbaltazar.blindo.graphql.type.RatingSortEnum
import com.pbaltazar.blindo.graphql.type.UpdateRatingInput
import com.pbaltazar.blindo.utils.extensions.isNullOrEmptyOrBlank
import com.pbaltazar.blindo.utils.extensions.toApiModel
import com.pbaltazar.blindo.utils.extensions.toGraphQLFilter

class BlindoApiRatingGateway(
    private val blindoApiClient: ApollitoClient
) : ApiHelpers, RatingGateway {

    override suspend fun getAppRatings(appInput: AppInput): ApiResponse<List<Rating>> =
        blindoApiClient.query(
            GetAppRatingsQuery(
                id = appInput.id,
                ratingsFilters = Optional.presentIfNotNull(appInput.ratingInput.filters?.toGraphQLFilter()),
                ratingsSort = appInput.ratingInput.sort.mapNotNull { it.apiEnum as RatingSortEnum },
                ratingsFirst = appInput.ratingInput.pageSize,
                ratingsAfter = Optional.presentIfNotNull(appInput.ratingInput.nextPageToken)
            )
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.getApp?.ratings?.let { query ->
                    query.edges.takeIf { it.isNotEmpty() }?.let { ratings ->
                        ApiResponse.Success(
                            data = ratings.mapNotNull { it?.node?.toApiModel() },
                            hasNextPage = query.pageInfo.hasNextPage,
                            nextPageToken = query.pageInfo.endCursor
                        )
                    } ?: ApiResponse.Error(ApiException.EmptyResponse)
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun getAppRatingsByPackageName(appInput: AppInput): ApiResponse<List<Rating>> =
        blindoApiClient.query(
            GetAppRatingsByPackageNameQuery(
                packageName = appInput.packageName,
                ratingsFilters = Optional.presentIfNotNull(appInput.ratingInput.filters?.toGraphQLFilter()),
                ratingsSort = appInput.ratingInput.sort.mapNotNull { it.apiEnum as RatingSortEnum },
                ratingsFirst = appInput.ratingInput.pageSize,
                ratingsAfter = Optional.presentIfNotNull(appInput.ratingInput.nextPageToken)
            )
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.getAppByPackageName?.ratings?.let { query ->
                    query.edges.takeIf { it.isNotEmpty() }?.let { ratings ->
                        ApiResponse.Success(
                            data = ratings.mapNotNull { it?.node?.toApiModel() },
                            hasNextPage = query.pageInfo.hasNextPage,
                            nextPageToken = query.pageInfo.endCursor
                        )
                    } ?: ApiResponse.Error(ApiException.EmptyResponse)
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun createRating(rating: Rating, idToken: String): ApiResponse<Rating> =
        blindoApiClient.mutate(
            CreateRatingMutation(
                input = CreateRatingInput(
                    appId = Optional.presentIfNotNull(rating.app?.id?.takeUnless { it.isNullOrEmpty() }),
                    appPackageName = Optional.presentIfNotNull(rating.app?.packageName?.takeUnless { it.isNullOrEmpty() }),
                    appPackageLabel = Optional.presentIfNotNull(rating.app?.packageLabel?.takeUnless { it.isNullOrEmpty() }),
                    ui = rating.ui,
                    screenreaders = rating.screenreaders,
                    labels = rating.labels,
                    functions = rating.functions,
                    performance = rating.performance,
                    comment = Optional.presentIfNotNull(rating.comment),
                    commentLanguage = rating.commentLanguage ?: ""
                )
            ),
            idToken
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.createRating?.rating?.let { createdRating ->
                    ApiResponse.Success(createdRating.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun updateRating(rating: Rating, idToken: String): ApiResponse<Rating> =
        blindoApiClient.mutate(
            UpdateRatingMutation(
                input = UpdateRatingInput(
                    id = rating.id,
                    ui = Optional.presentIfNotNull(rating.ui),
                    screenreaders = Optional.presentIfNotNull(rating.screenreaders),
                    labels = Optional.presentIfNotNull(rating.labels),
                    functions = Optional.presentIfNotNull(rating.functions),
                    performance = Optional.presentIfNotNull(rating.performance),
                    comment = Optional.presentIfNotNull(rating.comment),
                    commentLanguage = Optional.presentIfNotNull(rating.commentLanguage)
                )
            ),
            idToken
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.updateRating?.rating?.let { updatedRating ->
                    ApiResponse.Success(updatedRating.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun listRatings(ratingInput: RatingInput): ApiResponse<List<Rating>> =
        blindoApiClient.query(
            ListRatingsQuery(
                filters = Optional.presentIfNotNull(
                    RatingFilter(
                        appId = Optional.presentIfNotNull(ratingInput.appId.takeUnless { it.isNullOrEmptyOrBlank() }),
                        userId = Optional.presentIfNotNull(ratingInput.userId.takeUnless { it.isNullOrEmptyOrBlank() })
                    )
                ),
                sort = Optional.presentIfNotNull(ratingInput.sort.mapNotNull { it.apiEnum as RatingSortEnum }),
                first = Optional.presentIfNotNull(ratingInput.pageSize),
                after = Optional.presentIfNotNull(ratingInput.nextPageToken)
            )
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.listRatings?.edges?.takeUnless { it.isNullOrEmpty() }?.let { ratings ->
                    ApiResponse.Success(
                        ratings.mapNotNull { it?.node?.toApiModel() },
                        response.data.listRatings?.pageInfo?.hasNextPage ?: false,
                        response.data.listRatings?.pageInfo?.endCursor
                    )
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }
}
