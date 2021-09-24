package com.pbaltazar.blindo.entities.inputs

import com.pbaltazar.blindo.entities.sorts.RatingSort

data class RatingInput(
    private val id: String = "",
    val appId: String = "",
    val userId: String = "",
    val sort: List<RatingSort> = emptyList(),
    val pageSize: Int = 25,
    val nextPageToken: String? = null
)
