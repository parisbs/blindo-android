package com.pbaltazar.blindo.data

import com.apollographql.apollo.api.CustomTypeAdapter
import com.apollographql.apollo.api.ScalarType
import com.pbaltazar.blindo.BuildConfig
import com.pbaltazar.blindo.data.app.AppGateway
import com.pbaltazar.blindo.data.app.BlindoApiAppGateway
import com.pbaltazar.blindo.data.comment.BlindoApiCommentGateway
import com.pbaltazar.blindo.data.comment.CommentGateway
import com.pbaltazar.blindo.data.device.BlindoApiDeviceGateway
import com.pbaltazar.blindo.data.device.DeviceGateway
import com.pbaltazar.blindo.data.localapp.LocalAppGateway
import com.pbaltazar.blindo.data.localapp.PackageManagerLocalAppGateway
import com.pbaltazar.blindo.data.membership.BlindoApiMembershipGateway
import com.pbaltazar.blindo.data.membership.MembershipGateway
import com.pbaltazar.blindo.data.pack.BlindoApiPackGateway
import com.pbaltazar.blindo.data.pack.PackGateway
import com.pbaltazar.blindo.data.user.BlindoApiUserGateway
import com.pbaltazar.blindo.data.user.UserGateway
import com.pbaltazar.blindo.graphql.type.CustomType
import com.wizeline.simpleapollo.api.SimpleApolloClient
import com.wizeline.simpleapollo.api.cache.CacheConfiguration
import com.wizeline.simpleapollo.api.constants.DateTimePatterns
import com.wizeline.simpleapollo.api.constants.TimeUnit
import com.wizeline.simpleapollo.api.customtypes.DateTimeCustomTypeAdapter
import com.wizeline.simpleapollo.api.customtypes.JSONStringCustomTypeAdapter
import org.koin.dsl.module

val dataModule = module {

    single<SimpleApolloClient> {

        SimpleApolloClient.Builder()
            .context(get())
            .serverUrl(BuildConfig.SERVER_URL)
            .enableCache(
                CacheConfiguration(
                fileName = "cache",
                cacheSize = 10485860,
                expireTime = 25,
                expireUnit = TimeUnit.MINUTES
            )
            )
            .addCustomTypeAdapters(
                mapOf<ScalarType, CustomTypeAdapter<*>>(
                    Pair(
                        CustomType.DATETIME,
                        DateTimeCustomTypeAdapter(DateTimePatterns.ISO8601_MICROS_TZ.pattern)
                    ),
                    Pair(
                        CustomType.JSONSTRING,
                        JSONStringCustomTypeAdapter()
                    )
                )
            )
            .isDebug(BuildConfig.DEBUG)
            .build()
    }

    single<AppGateway> {
        BlindoApiAppGateway(
            get()
        )
    }

    single<CommentGateway> {
        BlindoApiCommentGateway(
            get()
        )
    }

    single<DeviceGateway> {
        BlindoApiDeviceGateway(
            get()
        )
    }

    single<LocalAppGateway> {
        PackageManagerLocalAppGateway(
            get()
        )
    }

    single<MembershipGateway> {
        BlindoApiMembershipGateway(
            get()
        )
    }

    single<PackGateway> {
        BlindoApiPackGateway(
            get()
        )
    }

    single<UserGateway> {
        BlindoApiUserGateway(
            get()
        )
    }
}
