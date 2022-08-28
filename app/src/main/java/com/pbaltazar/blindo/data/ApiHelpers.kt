package com.pbaltazar.blindo.data

import com.blindo.apollito.exceptions.EmptyResponse
import com.blindo.apollito.exceptions.ResponseWithErrors
import com.blindo.apollito.utils.extensions.toHumanReadable
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.responses.ApiResponse

interface ApiHelpers {

    fun processErrors(throwable: Throwable): ApiResponse.Error {
        FirebaseCrashlytics.getInstance().recordException(throwable)
        return when (throwable) {
            is EmptyResponse -> {
                ApiResponse.Error(ApiException.EmptyResponse)
            }
            is ResponseWithErrors -> {
                ApiResponse.Error(ApiException.WithErrors(throwable.errors.map { it.toHumanReadable() }))
            }
            else -> {
                ApiResponse.Error(ApiException.CallFailure(throwable))
            }
        }
    }
}
