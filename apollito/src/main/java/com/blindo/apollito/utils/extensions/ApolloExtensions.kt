package com.blindo.apollito.utils.extensions

import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Error
import com.apollographql.apollo3.api.Operation
import com.blindo.apollito.exceptions.EmptyResponse
import com.blindo.apollito.exceptions.ResponseWithErrors
import com.blindo.apollito.models.Response
import timber.log.Timber

internal fun <D: Operation.Data> ApolloResponse<D>.processResponse(isDebug: Boolean): Response<D> =
    if (hasErrors().not()) {
        data?.let {
            Response.Success(it)
        } ?: Response.Failure(EmptyResponse()).also {
            if (isDebug) {
                Timber.e(it.error)
            }
        }
    } else {
        if (isDebug) {
            Timber.e(
                errors?.mapNotNull { it.toReadableLog() }?.takeUnless { it.isNullOrEmpty() }?.toString() ?: "Empty errors list"
            )
        }
        Response.Failure(
            ResponseWithErrors(
                errors ?: emptyList<Error>()
            )
        )
    }

fun Error.toReadableLog(): String = "$message"

fun Map<String, Any?>.toReadableLog(locations: List<Error.Location>): String {
    var details = ""
    var current = 0
    forEach { attribute ->
        var description = "${attribute.key}: ${attribute.value}"
        locations.getOrNull(current)?.also { location ->
            description += " (${location.line}:${location.column})"
        }
        if (details.isNullOrEmpty()) {
            details += description
        } else {
            details += ", $description"
        }
        current += 1
    }
    return details
}

fun Error.toHumanReadable(): String = message
