package com.pbaltazar.blindo.ui.pack.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.entities.InstallablePack
import com.pbaltazar.blindo.entities.Pack
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.errors.AuthenticationProviderException
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.entities.responses.AuthenticationProviderResponse
import com.pbaltazar.blindo.usecases.MutationDownloadPack
import com.pbaltazar.blindo.utils.authentication.provider.AuthenticationProvider
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class PackDetailsViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val authenticationProvider: AuthenticationProvider,
    private val mutationDownloadPack: MutationDownloadPack
) : ViewModel() {

    private val _pack = MutableLiveData<Pack?>()

    fun setTargetPack(pack: Pack) {
        _pack.value = pack
    }

    fun getTargetPack(): Pack? = _pack.value

    private val installable = MutableLiveData<DownloadPackViewState>()
    val installablePack: LiveData<DownloadPackViewState> get() = installable

    fun downloadPack(request: InstallablePack) = viewModelScope.launch(backgroundDispatcher) {
        when (val tokenResponse = authenticationProvider.getIdToken()) {
            is AuthenticationProviderResponse.Success -> when (val apiResponse = mutationDownloadPack(request, tokenResponse.data)) {
                is ApiResponse.Success -> installable.postValue(DownloadPackViewState.Success(apiResponse.data))
                is ApiResponse.Error -> when (val apiError = apiResponse.error) {
                    is ApiException.EmptyResponse -> installable.postValue(DownloadPackViewState.Error("Empty response"))
                    is ApiException.WithErrors -> installable.postValue(DownloadPackViewState.Error(apiError.errorsList.toString()))
                    is ApiException.CallFailure -> installable.postValue(
                        DownloadPackViewState.Error(
                            apiError.error.localizedMessage ?: apiError.error.toString()
                        )
                    )
                }
            }
            is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                is AuthenticationProviderException.Empty -> installable.postValue(DownloadPackViewState.Error("Unable to retrieve user authorization token, empty response"))
                is AuthenticationProviderException.UnknownError -> installable.postValue(DownloadPackViewState.Error("Unable to retrieve user authorization token, unknown error"))
                is AuthenticationProviderException.NotSignedIn -> installable.postValue(DownloadPackViewState.Error("The user appears as not signed in"))
                is AuthenticationProviderException.Error -> installable.postValue(
                    DownloadPackViewState.Error(
                        tokenError.error.localizedMessage ?: tokenError.error.toString()
                    )
                )
            }
        }
    }

    sealed class DownloadPackViewState {
        class Success(val installablePack: InstallablePack): DownloadPackViewState()
        class Error(val errorMessage: String): DownloadPackViewState()
    }
}
