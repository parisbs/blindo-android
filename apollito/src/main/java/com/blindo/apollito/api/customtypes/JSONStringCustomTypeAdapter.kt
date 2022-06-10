package com.blindo.apollito.api.customtypes

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarAdapters
import com.apollographql.apollo3.api.json.JsonReader
import com.apollographql.apollo3.api.json.JsonWriter
import com.blindo.apollito.utils.extensions.toJson
import org.json.JSONObject

class JSONStringCustomTypeAdapter : Adapter<JSONObject> {

    override fun fromJson(reader: JsonReader, customScalarAdapters: CustomScalarAdapters): JSONObject =
        try {
            reader.toString().toJson()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    override fun toJson(writer: JsonWriter, customScalarAdapters: CustomScalarAdapters, value: JSONObject) {
        try {
            writer.value(value.toString())
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
