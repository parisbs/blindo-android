package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.pack.PackGateway
import com.pbaltazar.blindo.entities.InstallablePack
import com.pbaltazar.blindo.entities.responses.ApiResponse

class MutationDownloadPack(
    private val packGateway: PackGateway
) {
    suspend operator fun invoke(
        installablePack: InstallablePack,
        idToken: String
    ): ApiResponse<InstallablePack> =
        packGateway.downloadPack(installablePack, idToken)
}
