package com.pbaltazar.blindo.entities.inputs

import com.pbaltazar.blindo.entities.enums.PackSort

data class PackInput(
    val id: String = "",
    val appId: String = "",
    val userId: String = "",
    val sort: List<PackSort> = emptyList(),
    val pageSize: Int = 25,
    val nextPageToken: String? = null
)
