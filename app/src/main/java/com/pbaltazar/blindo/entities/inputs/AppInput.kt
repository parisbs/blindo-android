package com.pbaltazar.blindo.entities.inputs

import com.pbaltazar.blindo.entities.filters.AppFilters
import com.pbaltazar.blindo.entities.filters.sorts.AppSort

data class AppInput(
    val id: String = "",
    val packageName: String = "",
    val filters: AppFilters? = null,
    val sort: List<AppSort> = emptyList(),
    val pageSize: Int = 50,
    val nextPageToken: String? = null,
    val packInput: PackInput = PackInput(),
    val ratingInput: RatingInput = RatingInput()
)
