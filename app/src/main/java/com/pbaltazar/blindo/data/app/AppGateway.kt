package com.pbaltazar.blindo.data.app

import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.responses.ApiResponse

interface AppGateway {

    suspend fun listApps(appInput: AppInput): ApiResponse<List<App>>

    suspend fun getApp(appInput: AppInput): ApiResponse<App>

    suspend fun getAppByPackageName(appInput: AppInput): ApiResponse<App>

    suspend fun getAppOnly(appInput: AppInput): ApiResponse<App>
}
