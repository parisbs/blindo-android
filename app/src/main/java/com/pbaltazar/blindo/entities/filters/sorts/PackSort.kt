package com.pbaltazar.blindo.entities.filters.sorts

import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.graphql.type.PackSortEnum
import com.pbaltazar.blindo.ui.components.filters.entities.orderby.OrderByDirection
import com.pbaltazar.blindo.ui.components.filters.entities.orderby.OrderByEnum
import com.pbaltazar.blindo.ui.components.filters.entities.orderby.OrderBySelection

enum class PackSort(
    val apiEnum: Any,
    val bynaryValue: Int
) : OrderByEnum {
    UPDATED_AT_ASC(PackSortEnum.UPDATED_AT_ASC, 4) {
        override fun getDirection(): OrderByDirection = OrderByDirection.ASC
    },
    UPDATED_AT_DESC(PackSortEnum.UPDATED_AT_DESC, 8) {
        override fun getDirection(): OrderByDirection = OrderByDirection.DESC
    },
    NUMBER_OF_LABELS_ASC(PackSortEnum.NUMBER_OF_LABELS_ASC, 1) {
        override fun getDirection(): OrderByDirection = OrderByDirection.ASC
    },
    NUMBER_OF_LABELS_DESC(PackSortEnum.NUMBER_OF_LABELS_DESC, 2) {
        override fun getDirection(): OrderByDirection = OrderByDirection.DESC
    },
    DOWNLOADS_ASC(PackSortEnum.DOWNLOADS_ASC, 1) {
        override fun getDirection(): OrderByDirection = OrderByDirection.ASC
    },
    DOWNLOADS_DESC(PackSortEnum.DOWNLOADS_DESC, 2) {
        override fun getDirection(): OrderByDirection = OrderByDirection.DESC
    };

    override fun getName(): String = name

    override fun associatedIds(): List<Int> = when (this) {
        UPDATED_AT_ASC, UPDATED_AT_DESC -> listOf(
            R.id.filters_order_by_updated_at
        )
        NUMBER_OF_LABELS_ASC, NUMBER_OF_LABELS_DESC -> listOf(
            R.id.appsPacksFiltersOrderByNumberOfLabels
        )
        DOWNLOADS_ASC, DOWNLOADS_DESC -> listOf(
            R.id.appsPacksFiltersOrderByDownloads
        )
    }

    companion object {
        fun fromOrderBySelection(selection: OrderBySelection): PackSort =
            values().filter { it.associatedIds().contains(selection.elementId) && it.getDirection().equals(selection.direction) }
                .takeIf { it.size == 1 }
                ?.first() ?: throw IllegalArgumentException("Invalid OrderBySelection, element ID or direction no exists.")
    }
}
