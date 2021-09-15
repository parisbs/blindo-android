package com.pbaltazar.blindo.entities

data class Device(
    val id: String = "",
    val hardwareFingerprint: String = "",
    val gcmToken: String? = null,
    val name: String = "",
    val language: String = "",
    val country: String = ""
)
