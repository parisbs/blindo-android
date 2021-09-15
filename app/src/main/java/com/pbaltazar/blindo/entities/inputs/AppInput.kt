package com.pbaltazar.blindo.entities.inputs

import com.pbaltazar.blindo.entities.enums.AppSort

data class AppInput(
    val id: String = "",
    val packageName: String = "",
    val sort: List<AppSort> = emptyList(),
    val pageSize: Int = 50,
    val nextPageToken: String? = null,
    val packInput: PackInput = PackInput(),
    val commentInput: CommentInput = CommentInput()
)
