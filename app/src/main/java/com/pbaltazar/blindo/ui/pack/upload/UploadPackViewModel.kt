package com.pbaltazar.blindo.ui.pack.upload

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blindo.apollito.utils.extensions.toJson
import com.pbaltazar.blindo.entities.Label
import com.pbaltazar.blindo.entities.ProcessPacksResult
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.errors.AuthenticationProviderException
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.entities.responses.AuthenticationProviderResponse
import com.pbaltazar.blindo.usecases.MutationProcessPacks
import com.pbaltazar.blindo.utils.authentication.provider.AuthenticationProvider
import com.pbaltazar.blindo.utils.constants.TALKBACK_LABELS_ARRAY
import com.pbaltazar.blindo.utils.extensions.toLabelsList
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.coroutines.CoroutineContext

class UploadPackViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val context: Context,
    private val authenticationProvider: AuthenticationProvider,
    private val mutationProcessPacks: MutationProcessPacks
) : ViewModel() {

    private val isAuthLaunched = MutableLiveData<Boolean>()

    fun setIsAuthLaunched(isAuthLaunched: Boolean) {
        this.isAuthLaunched.value = isAuthLaunched
    }

    fun getIsAuthLaunched(): Boolean? = isAuthLaunched.value

    private val labelsList = MutableLiveData<LabelsViewState>()
    val labels: LiveData<LabelsViewState> get() = labelsList

    private val processResult = MutableLiveData<ProcessPacksViewState>()
    val results: LiveData<ProcessPacksViewState> get() = processResult

    fun processLabelsUri(uri: Uri) = viewModelScope.launch(backgroundDispatcher) {
        try {
            context.contentResolver.openInputStream(uri)?.also { file ->
                BufferedReader(InputStreamReader(file)).also { content ->
                    content.readText().toJson().optJSONArray(TALKBACK_LABELS_ARRAY)?.also { labelsArray ->
                        labelsList.postValue(LabelsViewState.Success(labelsArray.toLabelsList()))
                    } ?: labelsList.postValue(LabelsViewState.Empty)
                    content.close()
                }
            }
        } catch (e: Exception) {
            Timber.e(e)
            labelsList.postValue(
                LabelsViewState.Error(
                    e.localizedMessage ?: e.toString()
                )
            )
        }
    }

    fun processPacks(labels: List<Label>) = viewModelScope.launch(backgroundDispatcher) {
        when (val tokenResponse = authenticationProvider.getIdToken()) {
            is AuthenticationProviderResponse.Success -> when (val apiResponse = mutationProcessPacks(labels, tokenResponse.data)) {
                is ApiResponse.Success -> processResult.postValue(
                    ProcessPacksViewState.Success(
                        apiResponse.data
                    )
                )
                is ApiResponse.Error -> when (val apiError = apiResponse.error) {
                    is ApiException.EmptyResponse -> processResult.postValue(ProcessPacksViewState.Error("Unable to process your labels, empty response"))
                    is ApiException.WithErrors -> processResult.postValue(ProcessPacksViewState.Error(apiError.errorsList.toString()))
                    is ApiException.CallFailure -> processResult.postValue(
                        ProcessPacksViewState.Error(
                            apiError.error.localizedMessage ?: apiError.error.toString()
                        )
                    )
                }
            }
            is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                is AuthenticationProviderException.Empty -> processResult.postValue(ProcessPacksViewState.Error("Unable to retrieve user authorization token, empty response"))
                is AuthenticationProviderException.NotSignedIn -> processResult.postValue(ProcessPacksViewState.Error("The user appears as not signed in"))
                is AuthenticationProviderException.UnknownError -> processResult.postValue(ProcessPacksViewState.Error("Unable to retrieve user authorization token, unknown error"))
                is AuthenticationProviderException.Error -> processResult.postValue(
                    ProcessPacksViewState.Error(
                        tokenError.error.localizedMessage ?: tokenError.error.toString()
                    )
                )
            }
        }
    }

    sealed class LabelsViewState {
        class Success(val labels: List<Label>): LabelsViewState()
        object Empty: LabelsViewState()
        class Error(val errorMessage: String): LabelsViewState()
    }

    sealed class ProcessPacksViewState {
        class Success(val result: ProcessPacksResult): ProcessPacksViewState()
        class Error(val errorMessage: String): ProcessPacksViewState()
    }
}
