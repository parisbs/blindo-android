package com.pbaltazar.blindo.ui.components.filters.entities.orderby

enum class OrderByDirection(
    val spinnerPosition: Int
) {
    ASC(0),
    DESC(1);

    companion object {
        fun fromSpinnerPosition(position: Int): OrderByDirection =
            values().filter { it.spinnerPosition == position }
                .takeIf { it.size == 1 }?.first() ?: throw IllegalArgumentException("Invalid position value")
    }
}
