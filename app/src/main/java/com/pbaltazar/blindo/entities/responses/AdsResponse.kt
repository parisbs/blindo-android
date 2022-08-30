package com.pbaltazar.blindo.entities.responses

sealed class AdsResponse<out  T> {

    class Success<T>(val data: T): AdsResponse<T>()
    class Error(val error: Throwable): AdsResponse<Nothing>()
}
