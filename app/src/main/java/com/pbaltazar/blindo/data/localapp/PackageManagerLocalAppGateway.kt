package com.pbaltazar.blindo.data.localapp

import android.content.Context
import com.google.firebase.perf.metrics.AddTrace
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.errors.LocalAppsException
import com.pbaltazar.blindo.entities.responses.LocalAppsResponse
import com.pbaltazar.blindo.utils.extensions.toLocalModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class PackageManagerLocalAppGateway(
    private val context: Context
) : LocalAppGateway {

    @AddTrace(name = "loadingLocalApps", enabled = true)
    override suspend fun getLocalApps(): LocalAppsResponse<List<App>> =
        suspendCoroutine { continuation ->
            context.packageManager.getInstalledApplications(0).takeUnless { it.isNullOrEmpty() }?.also { apps ->
                continuation.resume(
                    LocalAppsResponse.Success(
                        apps.mapNotNull { it.toLocalModel(context) }.sortedBy { it.packageLabel }
                    )
                )
            } ?: continuation.resume(LocalAppsResponse.Error(LocalAppsException.EmptyResponse))
    }
}
