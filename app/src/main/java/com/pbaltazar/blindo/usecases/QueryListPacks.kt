package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.pack.PackGateway
import com.pbaltazar.blindo.entities.Pack
import com.pbaltazar.blindo.entities.inputs.PackInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.graphql.type.PackSortEnum

class QueryListPacks(
    private val packGateway: PackGateway
) {
    suspend operator fun invoke(packInput: PackInput): ApiResponse<List<Pack>> =
        packGateway.listPacks(packInput)
}
