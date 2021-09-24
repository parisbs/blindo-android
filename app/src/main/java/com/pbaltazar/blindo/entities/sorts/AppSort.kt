package com.pbaltazar.blindo.entities.sorts

import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.graphql.type.AppSortEnum

enum class AppSort(
    val spinnerIndex: Int,
    val apiEnum: Any
) {
    UPDATED_AT_ASC(0, AppSortEnum.UPDATED_AT_ASC),
    UPDATED_AT_DESC(1, AppSortEnum.UPDATED_AT_DESC),
    PACKAGE_LABEL_ASC(0, AppSortEnum.PACKAGE_LABEL_ASC),
    PACKAGE_LABEL_DESC(1, AppSortEnum.PACKAGE_LABEL_DESC),
    TOTAL_RATING_ASC(0, AppSortEnum.TOTAL_RATING_ASC),
    TOTAL_RATING_DESC(1, AppSortEnum.TOTAL_RATING_DESC),
    AVAILABLE_PACKS_ASC(0, AppSortEnum.AVAILABLE_PACKS_ASC),
    AVAILABLE_PACKS_DESC(1, AppSortEnum.AVAILABLE_PACKS_DESC);

    fun invert(): AppSort = name.split("_").dropLast(1).joinToString("_").let { prefix ->
        var position: Int = 0
        if (spinnerIndex == position) {
            position = 1
        }
        valuesOf(prefix).filterBySpinnerPosition(position)
    }

    companion object {
        fun valuesFromSpinner(id: Int): List<AppSort> = when (id) {
            R.id.lastUpdatedSpinner -> valuesOf("UPDATED_AT_")
            R.id.packageLabelSpinner -> valuesOf("PACKAGE_LABEL_")
            R.id.totalRatingSpinner -> valuesOf("TOTAL_RATING_")
            R.id.availablePacksSpinner -> valuesOf("AVAILABLE_PACKS_")
            else -> throw IllegalArgumentException("Not valid spinner ID")
        }

        fun valueFromSpinnerAndPosition(id: Int, position: Int): AppSort =
            valuesFromSpinner(id).filterBySpinnerPosition(position)

        fun valuesOf(startWith: String): List<AppSort> =
            values().filter { it.name.startsWith(startWith, true) }

        fun List<AppSort>.filterBySpinnerPosition(position: Int): AppSort = filter {
            it.spinnerIndex == position
        }.firstOrNull() ?: throw NullPointerException("Invalid spinner position")
    }
}
