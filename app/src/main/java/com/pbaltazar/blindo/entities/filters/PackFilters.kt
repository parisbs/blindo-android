package com.pbaltazar.blindo.entities.filters

import com.pbaltazar.blindo.entities.filters.common.IntRange

data class PackFilters(
    val numberOfLabelsRange: IntRange? = null,
    val languageIn: List<String>
)
