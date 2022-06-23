package com.blindo.apollito.api

import android.content.Context
import android.util.Log
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarType
import com.apollographql.apollo3.api.Mutation
import com.apollographql.apollo3.api.Query
import com.apollographql.apollo3.api.http.HttpHeader
import com.apollographql.apollo3.cache.normalized.ApolloStore
import com.apollographql.apollo3.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo3.cache.normalized.apolloStore
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.apollographql.apollo3.cache.normalized.normalizedCache
import com.apollographql.apollo3.cache.normalized.sql.SqlNormalizedCacheFactory
import com.apollographql.apollo3.network.okHttpClient
import com.blindo.apollito.api.cache.CacheConfiguration
import com.blindo.apollito.api.constants.AUTHORIZATION_HEADER
import com.blindo.apollito.api.constants.FetchPolicy
import com.blindo.apollito.api.constants.TimeUnit
import com.blindo.apollito.exceptions.ExpectedParameterError
import com.blindo.apollito.models.Response
import com.blindo.apollito.utils.extensions.processResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class ApollitoClient private constructor(
    private val apolloClient: ApolloClient,
    private val defaultFetchPolicy: FetchPolicy,
    private val isDebug: Boolean
) {

    companion object {
        const val TAG = "Apollito"
    }

    class Builder(
        private var context: Context? = null,
        private var serverUrl: String? = null,
        private var timeOutValue: Long = 30,
        private var timeOutUnit: TimeUnit = TimeUnit.SECONDS,
        private var cacheConfiguration: CacheConfiguration = CacheConfiguration(),
        private var customTypeAdapters: Map<CustomScalarType, Adapter<*>>? = null,
        private var isDebug: Boolean = false
    ) {

        fun context(context: Context) = apply {
            this.context = context
        }

        fun serverUrl(serverUrl: String) = apply {
            this.serverUrl = serverUrl
        }

        fun connectionTimeOut(value: Long, timeUnit: TimeUnit) = apply {
            this.timeOutValue = value
            this.timeOutUnit = timeOutUnit
        }

        fun cacheConfiguration(cacheConfiguration: CacheConfiguration) = apply {
            this.cacheConfiguration = cacheConfiguration
        }

        fun addCustomTypeAdapters(customTypeAdapters: Map<CustomScalarType, Adapter<*>>) = apply {
            this.customTypeAdapters = customTypeAdapters
        }

        fun isDebug(isDebug: Boolean = true) = apply {
            this.isDebug = isDebug
        }

        fun build(): ApollitoClient {
            if (this.context == null) {
                throw ExpectedParameterError("$this requires a context")
            }
            val graphqlServerUrl = this.serverUrl?.let { it } ?: throw ExpectedParameterError("$this requires a server url")
            val apolloClient = ApolloClient.Builder()
                .okHttpClient(this.getOkHttpClient())
                .serverUrl(graphqlServerUrl)
                .normalizedCache(
                    MemoryCacheFactory(
                        cacheConfiguration.cacheSize.toInt(),
                        cacheConfiguration.expireAfterMillis
                    ).chain(
                        SqlNormalizedCacheFactory(
                            context!!,
                            cacheConfiguration.fileName
                        )
                    )
                )
            customTypeAdapters?.takeUnless { it.isNullOrEmpty() }?.forEach { (customScalarType, adapter) ->
                apolloClient.addCustomScalarAdapter(customScalarType, adapter)
            }
            return ApollitoClient(
                apolloClient = apolloClient.build(),
                defaultFetchPolicy = cacheConfiguration.defaultFetchPolicy,
                isDebug = isDebug
            )
        }

        private fun getOkHttpClient(): OkHttpClient =
            OkHttpClient.Builder()
                .addInterceptor(
                    HttpLoggingInterceptor().setLevel(
                        if (this.isDebug)
                            HttpLoggingInterceptor.Level.BODY
                        else
                            HttpLoggingInterceptor.Level.NONE
                    )
                )
                .connectTimeout(this.timeOutValue, this.timeOutUnit.javaTimeUnit)
                .readTimeout(this.timeOutValue, this.timeOutUnit.javaTimeUnit)
                .callTimeout(this.timeOutValue, this.timeOutUnit.javaTimeUnit)
                .writeTimeout(this.timeOutValue, this.timeOutUnit.javaTimeUnit)
                .build()
    }

    fun apolloClient(): ApolloClient = apolloClient

    fun apolloStore(): ApolloStore = apolloClient.apolloStore

    suspend fun <D: Query.Data, Q: Query<D>> query(
        query: Q,
        authorizationToken: String? = null,
        fetchPolicy: FetchPolicy? = null
    ): Response<D> =
        try {
            this.apolloClient.query(query)
                .fetchPolicy(
                    fetchPolicy?.apolloFetchPolicy ?: defaultFetchPolicy.apolloFetchPolicy
                )
                .httpHeaders(
                    authorizationToken?.let {
                        listOf(
                            HttpHeader(AUTHORIZATION_HEADER, it)
                        )
                    } ?: emptyList()
                )
                .execute()
                .processResponse(isDebug)
        } catch (e: Exception) {
            if (isDebug) {
                Log.e(TAG, e.message, e)
            }
            Response.Failure(e)
        }

    suspend fun <D: Mutation.Data, M: Mutation<D>> mutate(
        mutation: M,
        authorizationToken: String? = null
    ): Response<D> =
        try {
            this.apolloClient.mutation(mutation)
                .httpHeaders(
                    authorizationToken?.let {
                        listOf(
                            HttpHeader(AUTHORIZATION_HEADER, it)
                        )
                    } ?: emptyList()
                )
                .execute()
                .processResponse(this.isDebug)
        } catch (e: Exception) {
            if (this.isDebug) {
                Log.e(TAG, e.message, e)
            }
            Response.Failure(e)
        }
}
