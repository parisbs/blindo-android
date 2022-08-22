package com.pbaltazar.blindo.data

import com.apollographql.apollo3.api.Adapter
import com.apollographql.apollo3.api.CustomScalarType
import com.blindo.apollito.api.ApollitoClient
import com.blindo.apollito.api.constants.DateTimePatterns
import com.blindo.apollito.api.constants.TimeUnit
import com.blindo.apollito.api.customtypes.DateTimeCustomTypeAdapter
import com.blindo.apollito.api.customtypes.JSONStringCustomTypeAdapter
import com.pbaltazar.blindo.BuildConfig
import com.pbaltazar.blindo.data.app.AppGateway
import com.pbaltazar.blindo.data.app.BlindoApiAppGateway
import com.pbaltazar.blindo.data.device.BlindoApiDeviceGateway
import com.pbaltazar.blindo.data.device.DeviceGateway
import com.pbaltazar.blindo.data.localapp.LocalAppGateway
import com.pbaltazar.blindo.data.localapp.PackageManagerLocalAppGateway
import com.pbaltazar.blindo.data.pack.BlindoApiPackGateway
import com.pbaltazar.blindo.data.pack.PackGateway
import com.pbaltazar.blindo.data.purchase.BlindoApiPurchaseGateway
import com.pbaltazar.blindo.data.purchase.PurchaseGateway
import com.pbaltazar.blindo.data.rating.BlindoApiRatingGateway
import com.pbaltazar.blindo.data.rating.RatingGateway
import com.pbaltazar.blindo.data.user.BlindoApiUserGateway
import com.pbaltazar.blindo.data.user.UserGateway
import com.pbaltazar.blindo.data.vision.BlindoApiVisionGateway
import com.pbaltazar.blindo.data.vision.VisionGateway
import com.pbaltazar.blindo.graphql.type.DateTime
import com.pbaltazar.blindo.graphql.type.JSONString
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val BASIC_APOLLITO_CLIENT = "basicApollitoClient"
const val EXTENDED_TIMEOUT_APOLLITO_CLIENT = "extendedTimeoutApollitoClient"

val dataModule = module {

    single(named(BASIC_APOLLITO_CLIENT)) {
        ApollitoClient.Builder()
            .context(get())
            .serverUrl(BuildConfig.SERVER_URL)
            .addCustomTypeAdapters(
                mapOf<CustomScalarType, Adapter<*>>(
                    Pair(
                        DateTime.type,
                        DateTimeCustomTypeAdapter(DateTimePatterns.ISO8601_MICROS_TZ.pattern)
                    ),
                    Pair(
                        JSONString.type,
                        JSONStringCustomTypeAdapter()
                    )
                )
            )
            .isDebug(BuildConfig.DEBUG)
            .build()
    }

    single(named(EXTENDED_TIMEOUT_APOLLITO_CLIENT)) {
        ApollitoClient.Builder()
            .context(get())
            .serverUrl(BuildConfig.SERVER_URL)
            .connectionTimeOut(300, TimeUnit.SECONDS)
            .addCustomTypeAdapters(
                mapOf<CustomScalarType, Adapter<*>>(
                    Pair(
                        DateTime.type,
                        DateTimeCustomTypeAdapter(DateTimePatterns.ISO8601_MICROS_TZ.pattern)
                    ),
                    Pair(
                        JSONString.type,
                        JSONStringCustomTypeAdapter()
                    )
                )
            )
            .isDebug(BuildConfig.DEBUG)
            .build()
    }

    single<AppGateway> {
        BlindoApiAppGateway(
            get(named(BASIC_APOLLITO_CLIENT))
        )
    }

    single<RatingGateway> {
        BlindoApiRatingGateway(
            get(named(BASIC_APOLLITO_CLIENT))
        )
    }

    single<DeviceGateway> {
        BlindoApiDeviceGateway(
            get(named(BASIC_APOLLITO_CLIENT))
        )
    }

    single<LocalAppGateway> {
        PackageManagerLocalAppGateway(
            get()
        )
    }

    single<PurchaseGateway> {
        BlindoApiPurchaseGateway(
            get(named(BASIC_APOLLITO_CLIENT))
        )
    }

    single<PackGateway> {
        BlindoApiPackGateway(
            get(named(BASIC_APOLLITO_CLIENT)),
            get(named(EXTENDED_TIMEOUT_APOLLITO_CLIENT))
        )
    }

    single<UserGateway> {
        BlindoApiUserGateway(
            get(named(BASIC_APOLLITO_CLIENT))
        )
    }

    single<VisionGateway> {
        BlindoApiVisionGateway(
            get(named(EXTENDED_TIMEOUT_APOLLITO_CLIENT))
        )
    }
}
