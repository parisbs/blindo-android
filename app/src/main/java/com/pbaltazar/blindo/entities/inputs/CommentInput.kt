package com.pbaltazar.blindo.entities.inputs

import com.pbaltazar.blindo.entities.enums.CommentSort

data class CommentInput(
    private val id: String = "",
    val appId: String = "",
    val userId: String = "",
    val sort: List<CommentSort> = emptyList(),
    val pageSize: Int = 25,
    val nextPageToken: String? = null
)
