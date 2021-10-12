package com.pbaltazar.blindo.ui.filter.apps

import androidx.lifecycle.ViewModel
import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.entities.filters.sorts.AppSort
import com.pbaltazar.blindo.utils.preferences.UserPreferences

class AppsFilterViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {

    fun getAppsPageSize(): Int = userPreferences.getAppsPageSize()

    fun setAppsPageSize(size: Int): Boolean = userPreferences.setAppsPageSize(size)

    fun getAppSort(): List<AppSort> = userPreferences.getAppSort()

    fun setAppSort(sort: List<AppSort>): Boolean = userPreferences.setAppSort(sort)

    fun getIsAppTotalRatingRangeChecked(): Boolean = userPreferences.getIsTotalRatingRangeChecked()

    fun setIsAppTotalRatingRangeChecked(isChecked: Boolean): Boolean = userPreferences.setIsAppTotalRatingRangeChecked(isChecked)

    fun getAppTotalRatingRange(): FloatRange = userPreferences.getAppTotalRatingRange()

    fun setAppTotalRatingRange(floatRange: FloatRange): Boolean = userPreferences.setAppTotalRatingRange(floatRange)
}
