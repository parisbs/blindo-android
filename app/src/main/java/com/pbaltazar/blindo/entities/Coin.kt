package com.pbaltazar.blindo.entities

import com.pbaltazar.blindo.entities.enums.CoinState
import com.pbaltazar.blindo.entities.enums.CoinType

data class Coin(
    val id: String,
    val productId: String,
    val token: String,
    val state: CoinState,
    val type: CoinType,
    val isConsumed: Boolean,
    val latestPurchase: Purchase
)
