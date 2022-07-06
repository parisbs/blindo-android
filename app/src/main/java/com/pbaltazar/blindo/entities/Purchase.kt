package com.pbaltazar.blindo.entities

import com.pbaltazar.blindo.entities.purchases.enums.ProductType
import java.util.*

data class Purchase(
    val id: String,
    val kind: ProductType,
    val orderId: String,
    val purchasedAt: Date,
    val startAt: Date,
    val expireAt: Date,
    val isAcknowledged: Boolean
)
