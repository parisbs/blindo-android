package com.pbaltazar.blindo.entities.filters

data class AppFilters(
    val packageName: String? = null,
    val packageLabel: String? = null,
    val totalRatingRange: FloatRange? = null
)
