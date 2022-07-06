package com.pbaltazar.blindo.usecases

import org.koin.dsl.module

val useCasesModule = module {

    factory { GetLocalApps(get()) }

    factory { MutationCreateDevice(get()) }
    factory { MutationCreateRating(get()) }
    factory { MutationCreateUser(get()) }
    factory { MutationDownloadBackup(get()) }
    factory { MutationDownloadPack(get()) }
    factory { MutationImageDescription(get()) }
    factory { MutationLaunchSli(get()) }
    factory { MutationProcessPurchase(get()) }
    factory { MutationProcessPacks(get()) }
    factory { MutationUpdateRating(get()) }
    factory { MutationUpdateDevice(get()) }
    factory { MutationUpdateUser(get()) }

    factory { QueryAuthenticateUser(get()) }
    factory { QueryGetApp(get()) }
    factory { QueryGetAppByPackageName(get()) }
    factory { QueryGetAppOnly(get()) }
    factory { QueryGetAppPacks(get()) }
    factory { QueryGetAppPacksByPackageName(get()) }
    factory { QueryGetAppRatings(get()) }
    factory { QueryGetAppRatingsByPackageName(get()) }
    factory { QueryGetDevice(get()) }
    factory { QueryGetMembership(get()) }
    factory { QueryGetPublicUser(get()) }
    factory { QueryGetPublicUserPacks(get()) }
    factory { QueryGetPublicUserRatings(get()) }
    factory { QueryGetUser(get()) }
    factory { QueryListApps(get()) }
    factory { QueryListPacks(get()) }
    factory { QueryListRatings(get()) }
}
