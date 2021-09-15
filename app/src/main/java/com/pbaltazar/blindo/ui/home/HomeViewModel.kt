package com.pbaltazar.blindo.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.enums.AppSort
import com.pbaltazar.blindo.entities.inputs.AppInput
import com.pbaltazar.blindo.usecases.QueryListApps
import com.pbaltazar.blindo.utils.preferences.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

class HomeViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val userPreferences: UserPreferences,
    private val queryListApps: QueryListApps
) : ViewModel() {

    val apps: Flow<PagingData<App>> = Pager(
        PagingConfig(
            pageSize = getAppsPageSize(),
            prefetchDistance = 10
        )
    ) {
        HomePagination(queryListApps, AppInput(
            sort = getAppSort(),
            pageSize = getAppsPageSize()
        ))
    }.flow
        .cachedIn(viewModelScope)

    private fun getAppsPageSize(): Int = userPreferences.getAppsPageSize()

    private fun getAppSort(): List<AppSort> = userPreferences.getAppSort()
}
