package com.pbaltazar.blindo.entities.filters

import com.pbaltazar.blindo.entities.filters.common.FloatRange

data class AppFilters(
    val packageName: String? = null,
    val packageLabel: String? = null,
    val totalRatingRange: FloatRange? = null
)
