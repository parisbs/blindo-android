package com.pbaltazar.blindo.entities.sorts

import com.pbaltazar.blindo.graphql.type.PackSortEnum

enum class PackSort(
    val apiEnum: Any,
    val bynaryValue: Int
) {
    NUMBER_OF_LABELS_ASC(PackSortEnum.NUMBER_OF_LABELS_ASC, 1),
    NUMBER_OF_LABELS_DESC(PackSortEnum.NUMBER_OF_LABELS_DESC, 2),
    UPDATED_AT_ASC(PackSortEnum.UPDATED_AT_ASC, 4),
    UPDATED_AT_DESC(PackSortEnum.UPDATED_AT_DESC, 8);
}
