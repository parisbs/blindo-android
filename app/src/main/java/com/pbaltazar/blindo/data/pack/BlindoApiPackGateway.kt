package com.pbaltazar.blindo.data.pack

import com.apollographql.apollo.api.Input
import com.pbaltazar.blindo.data.ApiHelpers
import com.pbaltazar.blindo.entities.InstallablePack
import com.pbaltazar.blindo.entities.Label
import com.pbaltazar.blindo.entities.Pack
import com.pbaltazar.blindo.entities.ProcessPacksResult
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.inputs.PackInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.graphql.*
import com.pbaltazar.blindo.graphql.type.*
import com.pbaltazar.blindo.utils.extensions.isNullOrEmptyOrBlank
import com.pbaltazar.blindo.utils.extensions.toApiModel
import com.pbaltazar.blindo.utils.extensions.toLabelInput
import com.wizeline.simpleapollo.api.SimpleApolloClient
import com.wizeline.simpleapollo.models.Response

class BlindoApiPackGateway(
    private val blindoApiClient: SimpleApolloClient
) : ApiHelpers, PackGateway {

    override suspend fun getAppPacks(appInput: AppInput): ApiResponse<List<Pack>> =
        blindoApiClient.query(
            GetAppPacksQuery(
                id = appInput.id,
                packsSort = appInput.packInput.sort.mapNotNull { it.apiEnum as PackSortEnum },
                packsFirst = appInput.packInput.pageSize,
                packsAfter = Input.optional(appInput.packInput.nextPageToken)
            )
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.getApp?.packs?.let { query ->
                    query.edges?.takeIf { it.isNotEmpty() }?.let { packs ->
                        ApiResponse.Success(
                            data = packs.mapNotNull { it?.node?.toApiModel() },
                            hasNextPage = query.pageInfo.hasNextPage,
                            nextPageToken = query.pageInfo.endCursor
                        )
                    } ?: ApiResponse.Error(ApiException.EmptyResponse)
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun getAppPacksByPackageName(appInput: AppInput): ApiResponse<List<Pack>> =
        blindoApiClient.query(
            GetAppPacksByPackageNameQuery(
                packageName = appInput.packageName,
                packsSort = appInput.packInput.sort.mapNotNull { it.apiEnum as PackSortEnum },
                packsFirst = appInput.packInput.pageSize,
                packsAfter = Input.optional(appInput.packInput.nextPageToken)
            )
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.getAppByPackageName?.packs?.let { query ->
                    query.edges?.takeIf { it.isNotEmpty() }?.let { packs ->
                        ApiResponse.Success(
                            data = packs.mapNotNull { it?.node?.toApiModel() },
                            hasNextPage = query.pageInfo.hasNextPage,
                            nextPageToken = query.pageInfo.endCursor
                        )
                    } ?: ApiResponse.Error(ApiException.EmptyResponse)
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun listPacks(packInput: PackInput): ApiResponse<List<Pack>> =
        blindoApiClient.query(
            ListPacksQuery(
                filters = Input.optional(
                    PackFilter(
                        appId = Input.optional(packInput.appId.takeUnless { it.isNullOrEmptyOrBlank() }),
                        userId = Input.optional(packInput.userId.takeUnless { it.isNullOrEmptyOrBlank() })
                    )
                ),
                sort = Input.optional(packInput.sort.mapNotNull { it.apiEnum as PackSortEnum }),
                first = Input.optional(packInput.pageSize),
                after = Input.optional(packInput.nextPageToken)
            )
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.listPacks?.edges?.takeUnless { it.isNullOrEmpty() }?.let { packs ->
                    ApiResponse.Success(
                        packs.mapNotNull { it?.node?.toApiModel() },
                        response.data.listPacks?.pageInfo?.hasNextPage ?: false,
                        response.data.listPacks?.pageInfo?.endCursor
                    )
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun downloadPack(
        installablePack: InstallablePack,
        idToken: String
    ): ApiResponse<InstallablePack> =
        blindoApiClient.mutate(
            DownloadPackMutation(
                input = DownloadPackInput(
                    packId = installablePack.pack.id,
                    targetScreenreader = installablePack.targetScreenreaders,
                    translateTo = Input.optional(installablePack.translateTo)
                )
            ),
            idToken
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.downloadPack?.let { pack ->
                    ApiResponse.Success(pack.toApiModel(installablePack))
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun downloadBackup(
        targetScreenreader: SupportedScreenreadersEnum,
        idToken: String
    ): ApiResponse<InstallablePack> =
        blindoApiClient.mutate(
            DownloadBackupMutation(
                input = DownloadBackupInput(
                    targetScreenreader = targetScreenreader
                )
            ),
            idToken
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.downloadBackup?.let { backup ->
                    ApiResponse.Success(backup.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun launchSli(
        apps: List<String>,
        language: String,
        targetScreenreader: SupportedScreenreadersEnum,
        preferUserLabels: Boolean,
        translate: Boolean,
        idToken: String
    ): ApiResponse<InstallablePack> =
        blindoApiClient.mutate(
            LaunchSliMutation(
                input = LaunchSliInput(
                    packageNames = apps,
                    targetScreenreader = targetScreenreader,
                    preferMyLabels = preferUserLabels,
                    targetLanguage = language,
                    requiresTranslation = translate
                )
            ),
            idToken
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.launchSli?.let { pack ->
                    ApiResponse.Success(pack.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }

    override suspend fun processPacks(
        labels: List<Label>,
        idToken: String
    ): ApiResponse<ProcessPacksResult> =
        blindoApiClient.mutate(
            ProcessPacksMutation(
                input = ProcessPacksInput(
                    labels = labels.mapNotNull { it.toLabelInput() }
                )
            ),
            idToken
        ).let { response ->
            when (response) {
                is Response.Success -> response.data.processPacks?.let { result ->
                    ApiResponse.Success(result.toApiModel())
                } ?: ApiResponse.Error(ApiException.EmptyResponse)
                is Response.Failure -> processErrors(response.error)
            }
        }
}
