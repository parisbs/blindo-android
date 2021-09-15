package com.pbaltazar.blindo.ui.user.backup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.entities.InstallablePack
import com.pbaltazar.blindo.entities.Pack
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.errors.AuthenticationProviderException
import com.pbaltazar.blindo.entities.inputs.PackInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.entities.responses.AuthenticationProviderResponse
import com.pbaltazar.blindo.graphql.type.SupportedScreenreadersEnum
import com.pbaltazar.blindo.usecases.MutationDownloadBackup
import com.pbaltazar.blindo.usecases.QueryListPacks
import com.pbaltazar.blindo.utils.authentication.provider.AuthenticationProvider
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class BackupViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val authenticationProvider: AuthenticationProvider,
    private val queryListPacks: QueryListPacks,
    private val mutationDownloadBackup: MutationDownloadBackup
) : ViewModel() {

    private val userPacks = MutableLiveData<UserPacks>()
    val packs: LiveData<UserPacks> get() = userPacks

    private val userBackup = MutableLiveData<InstallableBackup>()
    val backup: LiveData<InstallableBackup> get() = userBackup

    fun getUserPacks(packInput: PackInput) = viewModelScope.launch(backgroundDispatcher) {
        when (val apiResponse = queryListPacks(packInput)) {
            is ApiResponse.Success -> apiResponse.data.takeIf { it.isNotEmpty() }?.also {
                userPacks.postValue(
                    UserPacks.Success(
                        it,
                        apiResponse.hasNextPage,
                        apiResponse.nextPageToken
                    )
                )
            } ?: userPacks.postValue(UserPacks.Empty)
            is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                is ApiException.EmptyResponse -> userPacks.postValue(UserPacks.Empty)
                is ApiException.WithErrors -> userPacks.postValue(
                    UserPacks.Error(
                        apiException.errorsList.joinToString(", ")
                    )
                )
                is ApiException.CallFailure -> userPacks.postValue(
                    UserPacks.Error(
                        apiException.error.localizedMessage ?: apiException.error.toString()
                    )
                )
            }
        }
    }

    fun downloadBackup(targetScreenreader: SupportedScreenreadersEnum) = viewModelScope.launch(backgroundDispatcher) {
        when (val tokenResponse = authenticationProvider.getIdToken()) {
            is AuthenticationProviderResponse.Success -> when (val apiResponse = mutationDownloadBackup(targetScreenreader, tokenResponse.data)) {
                is ApiResponse.Success -> userBackup.postValue(
                    InstallableBackup.Success(
                        apiResponse.data
                    )
                )
                is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                    is ApiException.EmptyResponse -> userBackup.postValue(InstallableBackup.Empty)
                    is ApiException.WithErrors -> userBackup.postValue(
                        InstallableBackup.Error(
                            apiException.errorsList.joinToString(", ")
                        )
                    )
                    is ApiException.CallFailure -> userBackup.postValue(
                        InstallableBackup.Error(
                            apiException.error.localizedMessage ?: apiException.error.toString()
                        )
                    )
                }
            }
            is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                is AuthenticationProviderException.Empty -> userBackup.postValue(
                    InstallableBackup.Error(
                        "Unable to retrieve user authorization token, empty response"
                    )
                )
                is AuthenticationProviderException.NotSignedIn -> userBackup.postValue(
                    InstallableBackup.Error(
                        "The user appears as not signed in"
                    )
                )
                is AuthenticationProviderException.UnknownError -> userBackup.postValue(
                    InstallableBackup.Error(
                        "Unable to retrieve user authorization token, unknown error"
                    )
                )
                is AuthenticationProviderException.Error -> userBackup.postValue(
                    InstallableBackup.Error(
                        tokenError.error.localizedMessage ?: tokenError.error.toString()
                    )
                )
            }
        }
    }

    sealed class UserPacks {
        class Success(val packs: List<Pack>, val hasNextPage: Boolean, val nextPageToken: String? = null): UserPacks()
        object Empty: UserPacks()
        class Error(val errorMessage: String): UserPacks()
    }

    sealed class InstallableBackup {
        class Success(val installablePack: InstallablePack): InstallableBackup()
        object Empty: InstallableBackup()
        class Error(val errorMessage: String): InstallableBackup()
    }
}
