package com.pbaltazar.blindo.entities.errors

sealed class AuthenticationProviderException {

    object Empty: AuthenticationProviderException()
    object NotSignedIn: AuthenticationProviderException()
    object UnknownError: AuthenticationProviderException()
    class Error(val error: Throwable): AuthenticationProviderException()
}
