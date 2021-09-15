package com.pbaltazar.blindo.usecases

import com.pbaltazar.blindo.data.localapp.LocalAppGateway
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.responses.LocalAppsResponse

class GetLocalApps(
    private val localAppGateway: LocalAppGateway
) {
    suspend operator fun invoke(): LocalAppsResponse<List<App>> =
        localAppGateway.getLocalApps()
}
