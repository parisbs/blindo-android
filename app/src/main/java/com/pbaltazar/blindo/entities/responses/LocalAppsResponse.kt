package com.pbaltazar.blindo.entities.responses

import com.pbaltazar.blindo.entities.errors.LocalAppsException

sealed class LocalAppsResponse<out T> {

    class Success<T>(val data: T): LocalAppsResponse<T>()
    class Error(val error: LocalAppsException): LocalAppsResponse<Nothing>()
}
