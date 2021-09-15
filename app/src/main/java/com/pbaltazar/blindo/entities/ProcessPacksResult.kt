package com.pbaltazar.blindo.entities

data class ProcessPacksResult(
    val createdOrUpdated: Int = 0,
    val skipedOrDuplicated: Int = 0,
    val withErrors: Int = 0
)
