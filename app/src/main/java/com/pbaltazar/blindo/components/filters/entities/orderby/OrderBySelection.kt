package com.pbaltazar.blindo.components.filters.entities.orderby

import androidx.annotation.IdRes

data class OrderBySelection(
    @IdRes
    val elementId: Int,
    val isChecked: Boolean,
    val direction: OrderByDirection
)
