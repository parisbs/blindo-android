package com.pbaltazar.blindo.entities.enums

import com.pbaltazar.blindo.graphql.type.RatingSortEnum

enum class CommentSort(
    val apiEnum: Any
) {
    UPDATED_AT_ASC(RatingSortEnum.UPDATED_AT_ASC),
    UPDATED_AT_DESC(RatingSortEnum.UPDATED_AT_DESC)
}
