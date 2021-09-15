package com.pbaltazar.blindo.data.localapp

import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.responses.LocalAppsResponse

interface LocalAppGateway {

    suspend fun getLocalApps(): LocalAppsResponse<List<App>>
}
