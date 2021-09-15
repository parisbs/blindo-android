package com.pbaltazar.blindo.entities

data class BlindoPurchase(
    val id: String = "",
    val orderId: String = "",
    val state: Int = 0,
    val isAcknowledged: Boolean = false,
    val country: String = "",
    val price: Float = 0F,
    val currency: String = "",
    val originalJson: String = ""
)
