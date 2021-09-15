package com.pbaltazar.blindo.ui.app.details.pages.packs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.entities.Pack
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.usecases.*
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AppPacksViewModel(
    private val backgroundContext: CoroutineContext,
    private val queryGetAppPacks: QueryGetAppPacks,
    private val queryGetAppPacksByPackageName: QueryGetAppPacksByPackageName
) : ViewModel() {

    private val packsList = MutableLiveData<PacksList>()
    val packs: LiveData<PacksList> get() = packsList

    fun loadPacks(appInput: AppInput) = viewModelScope.launch(backgroundContext) {
        when (val apiResponse = if (appInput.id.isNotEmpty())
            queryGetAppPacks(appInput)
            else
                queryGetAppPacksByPackageName(appInput)
            ) {
            is ApiResponse.Success -> apiResponse.data.also { packsList ->
                this@AppPacksViewModel.packsList.postValue(
                    PacksList.Success(
                        packs = packsList,
                        hasNextPage = apiResponse.hasNextPage,
                        nextPageToken = apiResponse.nextPageToken
                    )
                )
            }
            is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                is ApiException.EmptyResponse -> packsList.postValue(PacksList.Empty)
                is ApiException.WithErrors -> packsList.postValue(PacksList.Error(apiException.errorsList.joinToString(", ")))
                is ApiException.CallFailure -> packsList.postValue(PacksList.Error(apiException.error.localizedMessage ?: apiException.error.toString()))
            }
        }
    }

    sealed class PacksList {
        class Success(val packs: List<Pack>, val hasNextPage: Boolean, val nextPageToken: String? = null) : PacksList()
        object Empty : PacksList()
        class Error(val reason: String) : PacksList()
    }
}
