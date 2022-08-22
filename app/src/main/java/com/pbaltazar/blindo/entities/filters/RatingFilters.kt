package com.pbaltazar.blindo.entities.filters

data class RatingFilters(
    val commentIsNull: Boolean? = null,
    val commentLanguageIn: List<String>? = null
)
