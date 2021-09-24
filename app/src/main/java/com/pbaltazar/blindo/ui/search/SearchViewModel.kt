package com.pbaltazar.blindo.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.sorts.AppSort
import com.pbaltazar.blindo.entities.filters.AppFilters
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.ui.home.HomePagination
import com.pbaltazar.blindo.usecases.QueryListApps
import com.pbaltazar.blindo.utils.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow

class SearchViewModel(
    private val userPreferences : UserPreferences,
    private val queryListApps: QueryListApps
) : ViewModel() {

    private var searchInput: AppFilters? = null

    val searchResults: Flow<PagingData<App>> get() = Pager(
        PagingConfig(
            pageSize = getAppsPageSize(),
            prefetchDistance = 10
        )
    ) {
        HomePagination(queryListApps, AppInput(
            filters = searchInput,
            sort = getAppSort(),
            pageSize = getAppsPageSize()
        ))
    }.flow
        .cachedIn(viewModelScope)

    fun setQuery(query: String) {
        if (isQueryPackageName(query)) {
            searchInput = AppFilters(
                packageName = query
            )
        } else if (isQueryAmbiguous(query)) {
            searchInput = AppFilters(
                packageName = query,
                packageLabel = query
            )
        } else {
            searchInput = AppFilters(
                packageLabel = query
            )
        }
    }

    private fun isQueryPackageName(query: String): Boolean =
        query.contains(Regex("^[A-Za-z]{1}[A-Za-z0-9_]*\\.[A-Za-z0-9_]+"))

    private fun isQueryAmbiguous(query: String): Boolean =
        isQueryPackageName(query).not() && query.contains(Regex("\\s")).not()

    private fun getAppsPageSize(): Int = userPreferences.getAppsPageSize()

    private fun getAppSort(): List<AppSort> = userPreferences.getAppSort()
}
