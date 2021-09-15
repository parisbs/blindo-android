package com.pbaltazar.blindo.data.pack

import com.pbaltazar.blindo.entities.InstallablePack
import com.pbaltazar.blindo.entities.Label
import com.pbaltazar.blindo.entities.Pack
import com.pbaltazar.blindo.entities.ProcessPacksResult
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.inputs.PackInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.graphql.type.SupportedScreenreadersEnum

interface PackGateway {

    suspend fun getAppPacks(appInput: AppInput): ApiResponse<List<Pack>>

    suspend fun getAppPacksByPackageName(appInput: AppInput): ApiResponse<List<Pack>>

    suspend fun listPacks(packInput: PackInput): ApiResponse<List<Pack>>

    suspend fun downloadPack(
        installablePack: InstallablePack,
        idToken: String
    ): ApiResponse<InstallablePack>

    suspend fun downloadBackup(
        targetScreenreader: SupportedScreenreadersEnum,
        idToken: String
    ): ApiResponse<InstallablePack>

    suspend fun launchSli(
        apps: List<String>,
        language: String,
        targetScreenreader: SupportedScreenreadersEnum,
        preferUserLabels: Boolean,
        translate: Boolean,
        idToken: String
    ): ApiResponse<InstallablePack>

    suspend fun processPacks(
        labels: List<Label>,
        idToken: String
    ): ApiResponse<ProcessPacksResult>
}
