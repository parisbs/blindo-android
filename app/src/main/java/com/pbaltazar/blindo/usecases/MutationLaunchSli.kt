package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.pack.PackGateway
import com.pbaltazar.blindo.entities.InstallablePack
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.graphql.type.SupportedScreenreadersEnum

class MutationLaunchSli(
    private val packGateway: PackGateway
) {
    suspend operator fun invoke(
        apps: List<String>,
        language: String,
        targetScreenreader: SupportedScreenreadersEnum,
        preferUserLabels: Boolean,
        translate: Boolean,
        idToken: String
    ): ApiResponse<InstallablePack> =
        packGateway.launchSli(
            apps,
            language,
            targetScreenreader,
            preferUserLabels,
            translate,
            idToken
        )
}
