package com.blindo.apollito.api.constants

enum class TimeUnit(
    val javaTimeUnit: java.util.concurrent.TimeUnit,
    val millisFactor: Long
) {
    MILLIS(java.util.concurrent.TimeUnit.MILLISECONDS, 1),
    SECONDS(java.util.concurrent.TimeUnit.SECONDS, 1000),
    MINUTES(java.util.concurrent.TimeUnit.MINUTES, 60000),
    HOURS(java.util.concurrent.TimeUnit.HOURS, 3600000),
    DAYS(java.util.concurrent.TimeUnit.DAYS, 86400000)
}
