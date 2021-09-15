package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.pack.PackGateway
import com.pbaltazar.blindo.entities.Pack
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

class QueryGetAppPacks(
    private val packGateway : PackGateway
) {
    suspend operator fun invoke(appInput : AppInput): ApiResponse<List<Pack>> =
        packGateway.getAppPacks(appInput)
}
