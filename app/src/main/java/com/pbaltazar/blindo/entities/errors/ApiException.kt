package com.pbaltazar.blindo.entities.errors

sealed class ApiException {

    object EmptyResponse: ApiException()
    class WithErrors(val errorsList: List<String>): ApiException()
    class CallFailure(val error: Throwable): ApiException()
}
