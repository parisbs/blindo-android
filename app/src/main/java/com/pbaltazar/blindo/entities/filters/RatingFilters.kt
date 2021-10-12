package com.pbaltazar.blindo.entities.filters

data class RatingFilters(
    val commentIsNull: Boolean = true,
    val commentLanguageIn: List<String>? = null
)
