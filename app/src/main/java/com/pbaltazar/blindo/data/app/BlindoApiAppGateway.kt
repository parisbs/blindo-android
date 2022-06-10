package com.pbaltazar.blindo.data.app

import com.apollographql.apollo3.api.Optional
import com.blindo.apollito.api.ApollitoClient
import com.blindo.apollito.models.Response
import com.pbaltazar.blindo.data.ApiHelpers
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.graphql.GetAppByPackageNameQuery
import com.pbaltazar.blindo.graphql.GetAppOnlyQuery
import com.pbaltazar.blindo.graphql.GetAppQuery
import com.pbaltazar.blindo.graphql.ListAppsQuery
import com.pbaltazar.blindo.graphql.type.AppSortEnum
import com.pbaltazar.blindo.graphql.type.PackSortEnum
import com.pbaltazar.blindo.graphql.type.RatingSortEnum
import com.pbaltazar.blindo.utils.extensions.toApiModel
import com.pbaltazar.blindo.utils.extensions.toGraphQLFilter

class BlindoApiAppGateway(
    private val blindoApiClient: ApollitoClient
) : ApiHelpers, AppGateway {

    override suspend fun listApps(appInput: AppInput): ApiResponse<List<App>> =
        blindoApiClient.query(
            ListAppsQuery(
                filters = Optional.presentIfNotNull(appInput.filters?.toGraphQLFilter()),
                sort = appInput.sort.mapNotNull { it.apiEnum as AppSortEnum },
                first = appInput.pageSize,
                after = Optional.presentIfNotNull(appInput.nextPageToken)
            )
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.listApps?.edges?.let { apps ->
                    ApiResponse.Success(
                        apps.mapNotNull { it?.node?.toApiModel() },
                        response.data.listApps?.pageInfo?.hasNextPage ?: false,
                        response.data.listApps?.pageInfo?.endCursor
                    )
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun getApp(appInput: AppInput): ApiResponse<App> =
        blindoApiClient.query(
            GetAppQuery(
                id = appInput.id,
                packsFirst = appInput.packInput.pageSize,
                packsSort = appInput.packInput.sort.mapNotNull { it.apiEnum as PackSortEnum },
                ratingsFilters = Optional.presentIfNotNull(appInput.ratingInput.filters?.toGraphQLFilter()),
                ratingsFirst = appInput.ratingInput.pageSize,
                ratingsSort = appInput.ratingInput.sort.mapNotNull { it.apiEnum as RatingSortEnum }
            )
        ).let { response ->
                when (response) {
                    is Response.Success -> response.data.getApp?.let { app ->
                            ApiResponse.Success(app.toApiModel())
                        } ?: ApiResponse.Error(ApiException.EmptyResponse)
                    is Response.Failure -> processErrors(response.error)
                }
            }

    override suspend fun getAppByPackageName(appInput: AppInput): ApiResponse<App> =
        blindoApiClient.query(
            GetAppByPackageNameQuery(
                packageName = appInput.packageName,
                packsFirst = appInput.packInput.pageSize,
                packsSort = appInput.packInput.sort.mapNotNull { it.apiEnum as PackSortEnum },
                ratingsFilters = Optional.presentIfNotNull(appInput.ratingInput.filters?.toGraphQLFilter()),
                ratingsFirst = appInput.ratingInput.pageSize,
                ratingsSort = appInput.ratingInput.sort.mapNotNull { it.apiEnum as RatingSortEnum }
            )
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.getAppByPackageName?.let { app ->
                    ApiResponse.Success(app.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun getAppOnly(appInput: AppInput): ApiResponse<App> =
        blindoApiClient.query(
            GetAppOnlyQuery(
                id = appInput.id
            )
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.getApp?.let { app ->
                    ApiResponse.Success(app.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }
}
