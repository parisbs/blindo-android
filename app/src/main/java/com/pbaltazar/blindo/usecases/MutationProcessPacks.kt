package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.pack.PackGateway
import com.pbaltazar.blindo.entities.Label
import com.pbaltazar.blindo.entities.ProcessPacksResult
import com.pbaltazar.blindo.entities.responses.ApiResponse

class MutationProcessPacks(
    private val packGateway: PackGateway
) {
    suspend operator fun invoke(
        labels: List<Label>,
        idToken: String
    ): ApiResponse<ProcessPacksResult> =
        packGateway.processPacks(labels, idToken)
}
