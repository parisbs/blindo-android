package com.pbaltazar.blindo.entities

import java.util.*

data class Membership(
    val id: String = "",
    val expireAt: Date = Date(System.currentTimeMillis()),
    val isCanceled: Boolean = false,
    val cancelReason: Int? = null,
    val token: String = "",
    val purchases: List<BlindoPurchase> = emptyList()
)
