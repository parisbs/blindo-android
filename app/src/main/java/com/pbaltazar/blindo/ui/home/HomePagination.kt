package com.pbaltazar.blindo.ui.home

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.usecases.QueryListApps

class HomePagination(
    private val query: QueryListApps,
    private val appInput: AppInput
) : PagingSource<String, App>() {

    override suspend fun load(params: LoadParams<String>): LoadResult<String, App> {
        return when (val apiResponse = query(
            appInput.copy(
                nextPageToken = params.key
            )
        )) {
            is ApiResponse.Success -> LoadResult.Page(
                data = apiResponse.data,
                prevKey = null,
                nextKey = if (apiResponse.hasNextPage) apiResponse.nextPageToken else null
            )
            is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                is ApiException.EmptyResponse -> LoadResult.Error(Exception("Empty response"))
                is ApiException.WithErrors -> LoadResult.Error(Exception(apiException.errorsList.joinToString(", ")))
                is ApiException.CallFailure -> LoadResult.Error(apiException.error)
            }
        }
    }

    override fun getRefreshKey(state: PagingState<String, App>): String? {
        return null
    }
}
