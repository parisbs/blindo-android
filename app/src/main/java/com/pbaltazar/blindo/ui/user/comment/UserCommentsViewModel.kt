package com.pbaltazar.blindo.ui.user.comment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.inputs.CommentInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.usecases.QueryListComments
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class UserCommentsViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val queryListComments: QueryListComments
) : ViewModel() {

    private val userComments = MutableLiveData<UserComments>()
    val comments: LiveData<UserComments> get() = userComments

    fun getUserComments(commentInput: CommentInput) = viewModelScope.launch(backgroundDispatcher) {
        when (val apiResponse = queryListComments(commentInput)) {
            is ApiResponse.Success -> userComments.postValue(
                UserComments.Success(
                    apiResponse.data,
                    hasNextPage = apiResponse.hasNextPage,
                    nextPageToken = apiResponse.nextPageToken
                )
            )
            is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                is ApiException.EmptyResponse -> userComments.postValue(UserComments.Empty)
                is ApiException.WithErrors -> userComments.postValue(UserComments.Error(apiException.errorsList.toString()))
                is ApiException.CallFailure -> userComments.postValue(
                    UserComments.Error(
                        apiException.error.localizedMessage ?: apiException.error.toString()
                    )
                )
            }
        }
    }

    sealed class UserComments {
        class Success(val comments: List<Rating>, val hasNextPage: Boolean, val nextPageToken: String? = null): UserComments()
        object Empty: UserComments()
        class Error(val errorMessage: String): UserComments()
    }
}
