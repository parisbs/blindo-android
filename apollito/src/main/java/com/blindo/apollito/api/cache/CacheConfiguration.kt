package com.blindo.apollito.api.cache

import com.blindo.apollito.api.constants.FetchPolicy
import com.blindo.apollito.api.constants.TimeUnit

data class CacheConfiguration(
    val fileName: String = "apollito.db",
    val cacheSize: Long = 10485760,
    val expireAfter: Long = 8,
    val expireUnit: TimeUnit = TimeUnit.HOURS,
    val defaultFetchPolicy: FetchPolicy = FetchPolicy.CACHE_FIRST
) {
    val expireAfterMillis: Long get() {
        return expireAfter * expireUnit.millisFactor
    }
}
