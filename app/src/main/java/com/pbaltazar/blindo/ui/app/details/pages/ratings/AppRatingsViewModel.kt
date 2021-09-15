package com.pbaltazar.blindo.ui.app.details.pages.ratings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.usecases.QueryGetAppRatings
import com.pbaltazar.blindo.usecases.QueryGetAppRatingsByPackageName
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AppRatingsViewModel(
    private val backgroundContext: CoroutineContext,
    private val queryGetAppRatings: QueryGetAppRatings,
    private val queryGetAppRatingsByPackageName: QueryGetAppRatingsByPackageName
) : ViewModel() {

    private val ratingsList = MutableLiveData<RatingsList>()
    val ratings: LiveData<RatingsList> get() = ratingsList

    fun loadRatings(appInput: AppInput) = viewModelScope.launch(backgroundContext) {
        when (val apiResponse = if (appInput.id.isNotEmpty())
            queryGetAppRatings(appInput)
            else
                queryGetAppRatingsByPackageName(appInput)
        ) {
            is ApiResponse.Success -> ratingsList.postValue(
                RatingsList.Success(
                    ratings = apiResponse.data,
                    hasNextPage = apiResponse.hasNextPage,
                    nextPageToken = apiResponse.nextPageToken
                )
            )
            is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                is  ApiException.EmptyResponse -> ratingsList.postValue(RatingsList.Empty)
                is ApiException.WithErrors -> ratingsList.postValue(RatingsList.Error(apiException.errorsList.joinToString(", ")))
                is ApiException.CallFailure -> ratingsList.postValue(RatingsList.Error(apiException.error.localizedMessage ?: apiException.error.toString()))
            }
        }
    }

    sealed class RatingsList {
        class Success(val ratings: List<Rating>, val hasNextPage: Boolean, val nextPageToken: String? = null) : RatingsList()
        object Empty : RatingsList()
        class Error(val reason: String) : RatingsList()
    }
}
