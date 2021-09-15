package com.pbaltazar.blindo.entities.enums

import com.pbaltazar.blindo.graphql.type.AppSortEnum

enum class AppSort(
    val apiEnum: Any
) {
    PACKAGE_LABEL_ASC(AppSortEnum.PACKAGE_LABEL_ASC),
    PACKAGE_LABEL_DESC(AppSortEnum.PACKAGE_LABEL_DESC),
    TOTAL_RATING_ASC(AppSortEnum.TOTAL_RATING_ASC),
    TOTAL_RATING_DESC(AppSortEnum.TOTAL_RATING_DESC),
    AVAILABLEPACKS_ASC(AppSortEnum.AVAILABLE_PACKS_ASC),
    AVAILABLE_PACKS_DESC(AppSortEnum.AVAILABLE_PACKS_DESC)
}
