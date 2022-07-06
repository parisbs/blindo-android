package com.pbaltazar.blindo.entities.purchases

import com.pbaltazar.blindo.entities.purchases.enums.ProductType

data class Purchase(
    val type: ProductType,
    val productId: String,
    val token: String,
    val isAcknowledged: Boolean
)
