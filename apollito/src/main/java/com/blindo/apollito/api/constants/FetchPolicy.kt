package com.blindo.apollito.api.constants

enum class FetchPolicy(
    val apolloFetchPolicy: com.apollographql.apollo3.cache.normalized.FetchPolicy
) {
    CACHE_FIRST(com.apollographql.apollo3.cache.normalized.FetchPolicy.CacheFirst),
    CACHE_ONLY(com.apollographql.apollo3.cache.normalized.FetchPolicy.CacheOnly),
    NETWORK_FIRST(com.apollographql.apollo3.cache.normalized.FetchPolicy.NetworkFirst),
    NETWORK_ONLY(com.apollographql.apollo3.cache.normalized.FetchPolicy.NetworkOnly)
}
