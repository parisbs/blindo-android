package com.pbaltazar.blindo.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.filters.AppFilters
import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.entities.filters.sorts.AppSort
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.ui.components.filters.FiltersScreen
import com.pbaltazar.blindo.ui.filter.FiltersSet
import com.pbaltazar.blindo.usecases.QueryListApps
import com.pbaltazar.blindo.utils.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow

class HomeViewModel(
    private val context: Context,
    private val userPreferences: UserPreferences,
    private val queryListApps: QueryListApps
) : ViewModel() {

    val apps: Flow<PagingData<App>> = Pager(
        PagingConfig(
            pageSize = getAppsPageSize(),
            prefetchDistance = 10
        )
    ) {
        HomePagination(queryListApps, getAppInput())
    }.flow
        .cachedIn(viewModelScope)

    private fun getAppsPageSize(): Int =
        userPreferences.getInt(FiltersSet.APP.getPreferencesKeyForPageSize(), 50)

    private fun getAppSort(): List<AppSort> =
        userPreferences.getString(
            FiltersSet.APP.getPreferencesKeyForTypeAndId(
                context,
                FiltersScreen.Companion.FilterType.ORDER_BY_TYPE,
                R.id.filters_screen_order_by_type
            ),
            FiltersSet.APP.getOrderByDefault()
        ).split(",").mapNotNull { AppSort.valueOf(it) }

    private fun isTotalRangeChecked(): Boolean =
        userPreferences.getBoolean(
            FiltersSet.APP.getPreferencesKeyForTypeAndId(
                context,
                FiltersScreen.Companion.FilterType.RANGE_TYPE,
                R.id.appFiltersRangeTotalRating
            ),
            false
        )

    private fun getTotalRatingRange(): FloatRange =
        userPreferences.getFloatRange(
            FiltersSet.APP.getPreferencesKeyForTypeAndId(
                context,
                FiltersScreen.Companion.FilterType.RANGE_TYPE,
                R.id.appFiltersRangeTotalRating
            ),
            FloatRange(1.0F, 5.0F)
        )

    private fun getAppFilters(): AppFilters = AppFilters(
        totalRatingRange = if (isTotalRangeChecked())
            getTotalRatingRange()
    else null
    )

    fun getAppInput(): AppInput = AppInput(
        filters = getAppFilters(),
        sort = getAppSort(),
        pageSize = getAppsPageSize()
    )
}
