package com.pbaltazar.blindo.entities.responses

import com.pbaltazar.blindo.entities.errors.AuthenticationProviderException

sealed class AuthenticationProviderResponse<out T> {

    class Success<T>(val data: T): AuthenticationProviderResponse<T>()
    class Error(val error: AuthenticationProviderException): AuthenticationProviderResponse<Nothing>()
}
