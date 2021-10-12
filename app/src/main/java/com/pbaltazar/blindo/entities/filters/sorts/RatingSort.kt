package com.pbaltazar.blindo.entities.filters.sorts

import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.graphql.type.RatingSortEnum
import com.pbaltazar.blindo.ui.components.filters.entities.orderby.OrderByDirection
import com.pbaltazar.blindo.ui.components.filters.entities.orderby.OrderByEnum

enum class RatingSort(
    val apiEnum: Any
) : OrderByEnum {
    UPDATED_AT_ASC(RatingSortEnum.UPDATED_AT_ASC) {
        override fun getDirection(): OrderByDirection = OrderByDirection.ASC
    },
    UPDATED_AT_DESC(RatingSortEnum.UPDATED_AT_DESC) {
        override fun getDirection(): OrderByDirection = OrderByDirection.DESC
    };

    override fun getName(): String = name

    override fun associatedIds(): List<Int> = when (this) {
        UPDATED_AT_ASC, UPDATED_AT_DESC -> listOf(
            R.id.filters_order_by_updated_at
        )
    }
}
