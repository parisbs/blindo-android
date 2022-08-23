package com.blindo.apollito.api.customtypes

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.blindo.apollito.utils.extensions.toApollitoString
import com.blindo.apollito.utils.extensions.toDate
import java.util.*

class DateTimeCustomTypeAdapter(
    private val dateTimePattern: String,
    private val forceUtc: Boolean = false
) : Adapter<Date> {

    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): Date =
        reader.nextString()?.toDate(dateTimePattern, forceUtc) ?: Date()

    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: Date) {
        writer.value(value.toApollitoString(dateTimePattern, forceUtc))
    }
}
