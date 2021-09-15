package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.pack.PackGateway
import com.pbaltazar.blindo.entities.InstallablePack
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.graphql.type.SupportedScreenreadersEnum

class MutationDownloadBackup(
    private val packGateway: PackGateway
) {
    suspend operator fun invoke(
        targetScreenreader: SupportedScreenreadersEnum,
        idToken: String
    ): ApiResponse<InstallablePack> =
        packGateway.downloadBackup(targetScreenreader, idToken)
}
