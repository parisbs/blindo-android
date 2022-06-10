package com.blindo.apollito.models

sealed class Response<out T> {
    class Success<T>(val data: T) : Response<T>()
    class Failure(val error: Throwable) : Response<Nothing>()
}
