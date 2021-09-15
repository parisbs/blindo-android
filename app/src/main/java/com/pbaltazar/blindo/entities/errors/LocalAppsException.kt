package com.pbaltazar.blindo.entities.errors

sealed class LocalAppsException {

    object EmptyResponse: LocalAppsException()
}
