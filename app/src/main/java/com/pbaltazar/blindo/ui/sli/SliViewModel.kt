package com.pbaltazar.blindo.ui.sli

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.entities.InstallablePack
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.errors.AuthenticationProviderException
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.entities.responses.AuthenticationProviderResponse
import com.pbaltazar.blindo.graphql.type.SupportedScreenreadersEnum
import com.pbaltazar.blindo.usecases.MutationLaunchSli
import com.pbaltazar.blindo.utils.authentication.provider.AuthenticationProvider
import com.pbaltazar.blindo.utils.log.BlindoLogger
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class SliViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val context: Context,
    private val authenticationProvider: AuthenticationProvider,
    private val mutationLaunchSli: MutationLaunchSli
) : ViewModel() {

    private val installedApps = MutableLiveData<LocalApps>()
    val apps: LiveData<LocalApps> get() = installedApps

    private val labelsPack = MutableLiveData<SliResult>()
    val installable: LiveData<SliResult> get() = labelsPack

    fun getInstalledApps() = viewModelScope.launch(backgroundDispatcher) {
        try {
            context.packageManager.getInstalledApplications(0).mapNotNull { it.packageName }.takeUnless { it.isNullOrEmpty() }?.also { apps ->
                installedApps.postValue(LocalApps.Success(apps))
            } ?: installedApps.postValue(LocalApps.Empty)
        } catch (e: Exception) {
            BlindoLogger.e(e)
            installedApps.postValue(LocalApps.Error(e.localizedMessage ?: e.toString()))
        }
    }

    fun launchSli(
        apps: List<String>,
        language: String,
        targetScreenreader: SupportedScreenreadersEnum,
        preferUserLabels: Boolean,
        translate: Boolean
    ) = viewModelScope.launch(backgroundDispatcher) {
        authenticationProvider.getUser()?.also {
            when (val tokenResponse = authenticationProvider.getIdToken()) {
                is AuthenticationProviderResponse.Success -> when (val apiResponse = mutationLaunchSli(
                    apps,
                    language,
                    targetScreenreader,
                    preferUserLabels,
                    translate,
                    tokenResponse.data
                )) {
                    is ApiResponse.Success -> labelsPack.postValue(
                        SliResult.Success(apiResponse.data)
                    )
                    is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                        is ApiException.EmptyResponse -> labelsPack.postValue(SliResult.Empty)
                        is ApiException.WithErrors -> labelsPack.postValue(SliResult.Error(apiException.errorsList.joinToString(", ")))
                        is ApiException.CallFailure -> labelsPack.postValue(SliResult.Error(apiException.error.localizedMessage ?: apiException.error.toString()))
                    }
                }
                is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                    is AuthenticationProviderException.Empty -> labelsPack.postValue(SliResult.Error("Unable to retrieve user authorization token, empty response"))
                    is AuthenticationProviderException.NotSignedIn -> labelsPack.postValue(SliResult.Error("The user appears as not signed in"))
                    is AuthenticationProviderException.UnknownError -> labelsPack.postValue(SliResult.Error("Unable to retrieve user authorization token, unknown error"))
                    is AuthenticationProviderException.Error -> labelsPack.postValue(SliResult.Error(tokenError.error.localizedMessage ?: tokenError.error.toString()))
                }
            }
        }
    }

    sealed class LocalApps {
        class Success(val apps: List<String>): LocalApps()
        object Empty: LocalApps()
        class Error(val errorMessage: String): LocalApps()
    }

    sealed class SliResult {
        class Success(val installablePack: InstallablePack): SliResult()
        object Empty: SliResult()
        class Error(val errorMessage: String): SliResult()
    }
}
