package com.pbaltazar.blindo.ui.comment.create

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.Rating
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.errors.AuthenticationProviderException
import com.pbaltazar.blindo.entities.inputs.CommentInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.entities.responses.AuthenticationProviderResponse
import com.pbaltazar.blindo.usecases.MutationCreateComment
import com.pbaltazar.blindo.usecases.MutationUpdateComment
import com.pbaltazar.blindo.usecases.QueryListComments
import com.pbaltazar.blindo.utils.authentication.provider.AuthenticationProvider
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CommentCreatorViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val authenticationProvider: AuthenticationProvider,
    private val queryListComments: QueryListComments,
    private val mutationCreateComment: MutationCreateComment,
    private val mutationUpdateComment: MutationUpdateComment
) : ViewModel() {

    private val _app = MutableLiveData<App?>()

    fun setTargetApp(app: App) {
        _app.value = app
    }

    fun getTargetApp(): App? = _app.value

    private val userRating = MutableLiveData<Rating?>()
    val rating: LiveData<Rating?> get() = userRating

    private val creation = MutableLiveData<RatingCreatorViewState>()
    val isCreated: LiveData<RatingCreatorViewState> get() = creation

    fun setUserComment(rating: Rating) = viewModelScope.launch(backgroundDispatcher) {
        userRating.postValue(rating)
    }

    fun getUserRating(commentInput: CommentInput) = viewModelScope.launch(backgroundDispatcher) {
        when (val apiResponse = queryListComments(commentInput)) {
            is ApiResponse.Success -> userRating.postValue(apiResponse.data.last())
        }
    }

    fun createOrUpdateRating(rating: Rating, isUpdate: Boolean = false) = viewModelScope.launch(backgroundDispatcher) {
        when (val tokenResponse = authenticationProvider.getIdToken()) {
            is AuthenticationProviderResponse.Success -> when (val response =
                    if (isUpdate)
                        mutationUpdateComment(rating, tokenResponse.data)
                    else
                        mutationCreateComment(rating, tokenResponse.data)
                    ) {
                    is ApiResponse.Success -> creation.postValue(RatingCreatorViewState.Success)
                    is ApiResponse.Error -> when (val error = response.error) {
                            is ApiException.EmptyResponse -> creation.postValue(
                                RatingCreatorViewState.Error(
                                    "Empty response"
                                )
                            )
                            is ApiException.WithErrors -> creation.postValue(
                                RatingCreatorViewState.Error(
                                    error.errorsList.toString()
                                )
                            )
                            is ApiException.CallFailure -> creation.postValue(
                                    RatingCreatorViewState.Error(
                                error.error.localizedMessage ?: "Unknown call failure"
                            )
                            )
                    }
            }
            is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                    is AuthenticationProviderException.Empty -> creation.postValue(
                        RatingCreatorViewState.Error(
                            "Unable to retrieve user authorization token, empty response"
                        )
                    )
                    is AuthenticationProviderException.UnknownError -> creation.postValue(
                        RatingCreatorViewState.Error(
                            "Unknown error retrieving user authorization token"
                        )
                    )
                    is AuthenticationProviderException.NotSignedIn -> creation.postValue(
                        RatingCreatorViewState.Error(
                            "The user appears as not signed in"
                        )
                    )
                    is AuthenticationProviderException.Error -> creation.postValue(
                        RatingCreatorViewState.Error(
                            tokenError.error.localizedMessage ?: "Unknown token failure"
                        )
                    )
                }
        }
    }

    sealed class RatingCreatorViewState {
        object Success: RatingCreatorViewState()
        class Error(val errorMessage: String): RatingCreatorViewState()
    }
}
