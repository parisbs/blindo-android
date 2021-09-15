package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.app.AppGateway
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

class QueryGetAppOnly(
    private val appGateway: AppGateway
) {
    suspend operator fun invoke(appInput: AppInput): ApiResponse<App> =
        appGateway.getAppOnly(appInput)
}
