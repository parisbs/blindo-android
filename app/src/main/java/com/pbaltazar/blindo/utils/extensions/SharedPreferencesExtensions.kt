package com.pbaltazar.blindo.utils.extensions

import android.content.SharedPreferences

fun SharedPreferences.putAndCommit(key: String, value: Any): Boolean = when (value) {
    is Boolean -> edit().putBoolean(key, value).commit()
    is Float -> edit().putFloat(key, value).commit()
    is Int -> edit().putInt(key, value).commit()
    is Long -> edit().putLong(key, value).commit()
    is String -> edit().putString(key, value).commit()
    else -> throw IllegalArgumentException("This type is not supported, only accept Boolean, Float, Int, Long and String")
}

fun <T, E: Enum<T>> SharedPreferences.putAndCommitEnumsList(key: String, value: List<E>): Boolean = value.mapNotNull { it.name }
    .joinToString(separator = ",")
    .let { enumsString ->
    putAndCommit(key, enumsString)
}

inline fun <reified T: Enum<T>> SharedPreferences.getEnumsList(key: String, default: List<T>): List<T> {
    val enumsString: String? = this.getString(
        key,
        default.mapNotNull { it.name }.joinToString(",")
    )
    val enumsList: List<T> = enumsString?.split(",")?.mapNotNull {
        try {
            enumValueOf<T>(it)
        } catch (e: Exception) {
            null
        }
    } ?: default
    return enumsList
}
