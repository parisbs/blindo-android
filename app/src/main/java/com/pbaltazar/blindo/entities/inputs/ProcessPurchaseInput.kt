package com.pbaltazar.blindo.entities.inputs

import com.pbaltazar.blindo.entities.purchases.enums.ProductType

data class ProcessPurchaseInput(
    val kind: ProductType,
    val productId: String,
    val token: String,
    val isAcknowledged: Boolean,
    val purchasesFirst: Int = 5
)
