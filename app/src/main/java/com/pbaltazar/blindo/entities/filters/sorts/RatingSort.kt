package com.pbaltazar.blindo.entities.filters.sorts

import com.pbaltazar.blindo.graphql.type.RatingSortEnum

enum class RatingSort(
    val apiEnum: Any
) {
    UPDATED_AT_ASC(RatingSortEnum.UPDATED_AT_ASC),
    UPDATED_AT_DESC(RatingSortEnum.UPDATED_AT_DESC)
}
