package com.pbaltazar.blindo.data.comment

import com.apollographql.apollo.api.Input
import com.pbaltazar.blindo.data.ApiHelpers
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.inputs.CommentInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.graphql.*
import com.pbaltazar.blindo.graphql.type.CreateRatingInput
import com.pbaltazar.blindo.graphql.type.RatingFilter
import com.pbaltazar.blindo.graphql.type.RatingSortEnum
import com.pbaltazar.blindo.graphql.type.UpdateRatingInput
import com.pbaltazar.blindo.utils.extensions.isNullOrEmptyOrBlank
import com.pbaltazar.blindo.utils.extensions.toApiModel
import com.wizeline.simpleapollo.api.SimpleApolloClient
import com.wizeline.simpleapollo.models.Response

class BlindoApiCommentGateway(
    private val blindoApiClient: SimpleApolloClient
) : ApiHelpers, CommentGateway {

    override suspend fun getAppRatings(appInput: AppInput): ApiResponse<List<Rating>> =
        blindoApiClient.query(
            GetAppRatingsQuery(
                id = appInput.id,
                ratingsSort = appInput.commentInput.sort.mapNotNull { it.apiEnum as RatingSortEnum },
                ratingsFirst = appInput.commentInput.pageSize,
                ratingsAfter = Input.optional(appInput.commentInput.nextPageToken)
            )
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.getApp?.ratings?.let { query ->
                    query.edges?.takeIf { it.isNotEmpty() }?.let { ratings ->
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
                ratingsSort = appInput.commentInput.sort.mapNotNull { it.apiEnum as RatingSortEnum },
                ratingsFirst = appInput.commentInput.pageSize,
                ratingsAfter = Input.optional(appInput.commentInput.nextPageToken)
            )
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.getAppByPackageName?.ratings?.let { query ->
                    query.edges?.takeIf { it.isNotEmpty() }?.let { ratings ->
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

    override suspend fun createComment(rating: Rating, idToken: String): ApiResponse<Rating> =
        blindoApiClient.mutate(
            CreateRatingMutation(
                input = CreateRatingInput(
                    appId = Input.optional(rating.app?.id?.takeUnless { it.isNullOrEmpty() }),
                    appPackageName = Input.optional(rating.app?.packageName?.takeUnless { it.isNullOrEmpty() }),
                    appPackageLabel = Input.optional(rating.app?.packageLabel?.takeUnless { it.isNullOrEmpty() }),
                    ui = rating.ui,
                    screenreaders = rating.screenreaders,
                    labels = rating.labels,
                    functions = rating.functions,
                    performance = rating.performance,
                    comment = Input.optional(rating.comment),
                    commentLanguage = rating.commentLanguage ?: ""
                )
            ),
            idToken
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.createRating?.rating?.let { comment ->
                    ApiResponse.Success(comment.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun updateComment(rating: Rating, idToken: String): ApiResponse<Rating> =
        blindoApiClient.mutate(
            UpdateRatingMutation(
                input = UpdateRatingInput(
                    id = rating.id,
                    ui = Input.optional(rating.ui),
                    screenreaders = Input.optional(rating.screenreaders),
                    labels = Input.optional(rating.labels),
                    functions = Input.optional(rating.functions),
                    performance = Input.optional(rating.performance),
                    comment = Input.optional(rating.comment),
                    commentLanguage = Input.optional(rating.commentLanguage)
                )
            ),
            idToken
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.updateRating?.rating?.let { comment ->
                    ApiResponse.Success(comment.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun listComments(commentInput: CommentInput): ApiResponse<List<Rating>> =
        blindoApiClient.query(
            ListRatingsQuery(
                filters = Input.optional(
                    RatingFilter(
                        appId = Input.optional(commentInput.appId.takeUnless { it.isNullOrEmptyOrBlank() }),
                        userId = Input.optional(commentInput.userId.takeUnless { it.isNullOrEmptyOrBlank() })
                    )
                ),
                sort = Input.optional(commentInput.sort.mapNotNull { it.apiEnum as RatingSortEnum }),
                first = Input.optional(commentInput.pageSize),
                after = Input.optional(commentInput.nextPageToken)
            )
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.listRatings?.edges?.takeUnless { it.isNullOrEmpty() }?.let { comments ->
                    ApiResponse.Success(
                        comments.mapNotNull { it?.node?.toApiModel() },
                        response.data.listRatings?.pageInfo?.hasNextPage ?: false,
                        response.data.listRatings?.pageInfo?.endCursor
                    )
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }
}
