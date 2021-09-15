package com.pbaltazar.blindo.data


import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.wizeline.simpleapollo.exceptions.EmptyResponse
import com.wizeline.simpleapollo.exceptions.ResponseWithErrors
import com.wizeline.simpleapollo.utils.extensions.toHumanReadable

interface ApiHelpers {

    fun processErrors(throwable: Throwable): ApiResponse.Error {
        FirebaseCrashlytics.getInstance().recordException(throwable)
        when (throwable) {

            is EmptyResponse -> {
                return ApiResponse.Error(ApiException.EmptyResponse)
            }
            is ResponseWithErrors -> {
                return ApiResponse.Error(ApiException.WithErrors(throwable.errors.mapNotNull { it.toHumanReadable() }))
            }
            else -> {
                return ApiResponse.Error(ApiException.CallFailure(throwable))
            }
        }
    }
}
