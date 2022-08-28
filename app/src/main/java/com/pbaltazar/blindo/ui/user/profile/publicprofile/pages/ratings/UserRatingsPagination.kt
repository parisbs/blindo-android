package com.pbaltazar.blindo.ui.user.profile.publicprofile.pages.ratings

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.inputs.UserInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.usecases.QueryGetPublicUserRatings

class UserRatingsPagination(
    private val query: QueryGetPublicUserRatings,
    private val userInput: UserInput
) : PagingSource<String, User>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, User> {
        return when (val apiResponse = query(
            userInput.copy(
                ratingInput = userInput.ratingInput.copy(
                    nextPageToken = params.key
                )
            )
        )) {
            is ApiResponse.Success -> LoadResult.Page(
                data = listOf(apiResponse.data),
                prevKey = null,
                nextKey = if (apiResponse.data.ratings?.hasNextPage == true)
                    apiResponse.data.ratings.nextPageToken
                else null
            )
            is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                is ApiException.EmptyResponse -> LoadResult.Error(Exception("Empty response"))
                is ApiException.WithErrors -> LoadResult.Error(Exception(apiException.errorsList.joinToString(", ")))
                is ApiException.CallFailure -> LoadResult.Error(apiException.error)
            }
        }
    }

    override fun getRefreshKey(state: PagingState<String, User>): String? {
        return null
    }
}
