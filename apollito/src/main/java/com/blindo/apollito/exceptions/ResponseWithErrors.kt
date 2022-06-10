package com.blindo.apollito.exceptions

import com.apollographql.apollo3.api.Error

class ResponseWithErrors(
    val errors: List<Error>
) : Exception()
