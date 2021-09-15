package com.pbaltazar.blindo.ui.app.details.pages.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.usecases.QueryGetAppOnly
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AppStatisticsViewModel(
    private val backgroundContext: CoroutineContext,
    private val queryGetAppOnly: QueryGetAppOnly
) : ViewModel() {

    private val appStatisticsResponse = MutableLiveData<AppStatistics>()
    val statistics: LiveData<AppStatistics> get() = appStatisticsResponse

    fun getAppStatistics(id: String) = viewModelScope.launch(backgroundContext) {
        when (val apiResponse = queryGetAppOnly(
            AppInput(id = id)
        )) {
            is ApiResponse.Success -> appStatisticsResponse.postValue(
                AppStatistics.Success(apiResponse.data)
            )
            is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                is ApiException.EmptyResponse -> appStatisticsResponse.postValue(AppStatistics.Empty)
                is ApiException.WithErrors -> appStatisticsResponse.postValue(AppStatistics.Error(apiException.errorsList.joinToString(", ")))
                is ApiException.CallFailure -> appStatisticsResponse.postValue(AppStatistics.Error(apiException.error.localizedMessage ?: apiException.error.toString()))
            }
        }
    }

    sealed class AppStatistics {
        class Success(val statistics: App) : AppStatistics()
        object Empty : AppStatistics()
        class Error(val reason: String) : AppStatistics()
    }
}
