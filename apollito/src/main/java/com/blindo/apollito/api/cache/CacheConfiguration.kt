package com.blindo.apollito.api.cache

import com.blindo.apollito.api.constants.TimeUnit

data class CacheConfiguration(
    val fileName: String = "Apollito",
    val cacheSize: Long = 10485760,
    val expireTime: Long = 1,
    val expireUnit: TimeUnit = TimeUnit.DAYS
)
