package com.pbaltazar.blindo.entities.filters.sorts

import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.graphql.type.AppSortEnum
import com.pbaltazar.blindo.ui.components.filters.entities.orderby.OrderByDirection
import com.pbaltazar.blindo.ui.components.filters.entities.orderby.OrderByEnum

enum class AppSort(
    val spinnerIndex: Int,
    val apiEnum: Any
) : OrderByEnum {
    UPDATED_AT_ASC(0, AppSortEnum.UPDATED_AT_ASC),
    UPDATED_AT_DESC(1, AppSortEnum.UPDATED_AT_DESC),
    PACKAGE_LABEL_ASC(0, AppSortEnum.PACKAGE_LABEL_ASC),
    PACKAGE_LABEL_DESC(1, AppSortEnum.PACKAGE_LABEL_DESC),
    TOTAL_RATING_ASC(0, AppSortEnum.TOTAL_RATING_ASC),
    TOTAL_RATING_DESC(1, AppSortEnum.TOTAL_RATING_DESC),
    AVAILABLE_PACKS_ASC(0, AppSortEnum.AVAILABLE_PACKS_ASC),
    AVAILABLE_PACKS_DESC(1, AppSortEnum.AVAILABLE_PACKS_DESC);

    override fun getName(): String = name

    override fun associatedIds(): List<Int> = when (this) {
        UPDATED_AT_ASC, UPDATED_AT_DESC -> listOf(
            R.id.filters_order_by_updated_at
        )
            PACKAGE_LABEL_ASC, PACKAGE_LABEL_DESC -> listOf(
                R.id.appFiltersOrderByPackageLabel
            )
        TOTAL_RATING_ASC, TOTAL_RATING_DESC -> listOf(
            R.id.appFiltersORderByTotalRating
        )
        AVAILABLE_PACKS_ASC, AVAILABLE_PACKS_DESC -> listOf(
            R.id.appFiltersOrderByAvailablePacks
        )
    }

    override fun getDirection(): OrderByDirection =
        OrderByDirection.valueOf(name.split("_").last())
}
