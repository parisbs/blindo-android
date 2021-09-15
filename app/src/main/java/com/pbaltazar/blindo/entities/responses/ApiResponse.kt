package com.pbaltazar.blindo.entities.responses

import com.pbaltazar.blindo.entities.errors.ApiException

sealed class ApiResponse<out T> {

    class Success<T>(val data: T, val hasNextPage: Boolean = false, val nextPageToken: String? = null): ApiResponse<T>()
    class Error(val error: ApiException): ApiResponse<Nothing>()
}
