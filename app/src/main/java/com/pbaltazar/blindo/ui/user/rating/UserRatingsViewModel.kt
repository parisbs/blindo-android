package com.pbaltazar.blindo.ui.user.rating

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.inputs.RatingInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.usecases.QueryListRatings
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class UserRatingsViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val queryListRatings: QueryListRatings
) : ViewModel() {

    private val userRatings = MutableLiveData<UserRatings>()
    val ratings: LiveData<UserRatings> get() = userRatings

    fun getUserRatings(ratingInput: RatingInput) = viewModelScope.launch(backgroundDispatcher) {
        when (val apiResponse = queryListRatings(ratingInput)) {
            is ApiResponse.Success -> userRatings.postValue(
                UserRatings.Success(
                    apiResponse.data,
                    hasNextPage = apiResponse.hasNextPage,
                    nextPageToken = apiResponse.nextPageToken
                )
            )
            is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                is ApiException.EmptyResponse -> userRatings.postValue(UserRatings.Empty)
                is ApiException.WithErrors -> userRatings.postValue(UserRatings.Error(apiException.errorsList.toString()))
                is ApiException.CallFailure -> userRatings.postValue(
                    UserRatings.Error(
                        apiException.error.localizedMessage ?: apiException.error.toString()
                    )
                )
            }
        }
    }

    sealed class UserRatings {
        class Success(val ratings: List<Rating>, val hasNextPage: Boolean, val nextPageToken: String? = null): UserRatings()
        object Empty: UserRatings()
        class Error(val errorMessage: String): UserRatings()
    }
}
