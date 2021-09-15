package com.pbaltazar.blindo.ui.app.local

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.errors.LocalAppsException
import com.pbaltazar.blindo.entities.responses.LocalAppsResponse
import com.pbaltazar.blindo.usecases.GetLocalApps
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class LocalAppsViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val getLocalApps: GetLocalApps
) : ViewModel() {

    private val localApps = MutableLiveData<AppsViewState>()
    val apps: LiveData<AppsViewState>
        get() = localApps

    fun loadLocalApps() = viewModelScope.launch(backgroundDispatcher) {
        when (val response = getLocalApps()) {
            is LocalAppsResponse.Success -> localApps.postValue(AppsViewState.Success(response.data))
            is LocalAppsResponse.Error -> when (response.error) {
                is LocalAppsException.EmptyResponse -> localApps.postValue(AppsViewState.Empty)
            }
        }
    }

    sealed class AppsViewState {
        class Success(val apps: List<App>): AppsViewState()
        object Empty: AppsViewState()
        class Error(val errorMessage: String): AppsViewState()
    }
}
