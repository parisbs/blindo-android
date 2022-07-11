package com.pbaltazar.blindo.entities.inputs

import com.pbaltazar.blindo.entities.filters.sorts.CoinSort

data class ListCoinsInput(
    val sort: List<CoinSort>,
    val first: Int,
    val after: String? = null,
    val idToken: String
)
