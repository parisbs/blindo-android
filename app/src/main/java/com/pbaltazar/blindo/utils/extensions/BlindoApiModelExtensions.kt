package com.pbaltazar.blindo.utils.extensions

import com.pbaltazar.blindo.entities.*
import com.pbaltazar.blindo.entities.connections.PackConnection
import com.pbaltazar.blindo.entities.connections.RatingConnection
import com.pbaltazar.blindo.graphql.*
import com.pbaltazar.blindo.graphql.type.LabelInput
import org.json.JSONObject

fun ListAppsQuery.Node.toApiModel(): App? = App(
    id = id,
    packageName = packageName,
    packageIcon = packageIcon,
    packageLabel = packageLabel,
    category = category,
    totalRating = totalRating.toFloat(),
    numberOfRatings = numberOfRatings,
    availablePacks = availablePacks
)

fun GetAppQuery.GetApp.toApiModel(): App = App(
    uiRating = uiRating?.toFloat(),
    screenreadersRating = screenreadersRating?.toFloat(),
    labelsRating = labelsRating?.toFloat(),
    functionsRating = functionsRating?.toFloat(),
    performanceRating = performanceRating?.toFloat(),
    totalRating = totalRating.toFloat(),
    packs = packs?.toApiModel(),
    ratings = ratings?.toApiModel()
)

fun GetAppQuery.Packs.toApiModel(): PackConnection = PackConnection(
    packs = edges.mapNotNull { it?.node?.toApiModel() },
    hasNextPage = pageInfo.hasNextPage,
    nextPageToken = pageInfo.endCursor
)

fun GetAppQuery.Node.toApiModel(): Pack = Pack(
    id = id,
    numberOfLabels = numberOfLabels,
    downloads = downloads,
    language = language,
    hash = hash,
    createdAt = createdAt,
    updatedAt = updatedAt,
    user = user?.toApiModel()
)

fun GetAppQuery.User.toApiModel(): User = User(
    id = id,
    name = name,
    picture = picture
)

fun GetAppQuery.Ratings.toApiModel(): RatingConnection = RatingConnection(
    ratings = edges.mapNotNull { it?.node?.toApiModel() },
    hasNextPage = pageInfo.hasNextPage,
    nextPageToken = pageInfo.endCursor
)

fun GetAppQuery.Node1.toApiModel(): Rating {
    val totalRating: Double = ((ui + screenreaders + labels + functions + performance) / 5).toDouble()
    return Rating(
        id = id,
        ui = ui,
        screenreaders = screenreaders,
        labels = labels,
        functions = functions,
        performance = performance,
        total = totalRating.toFloat(),
        comment = comment,
        commentLanguage = commentLanguage,
        createdAt = createdAt,
        updatedAt = updatedAt,
        user = user?.toApiModel()
    )
}

    fun GetAppQuery.User1.toApiModel(): User = User(
        id = id,
        name = name,
        picture = picture
    )

    fun GetAppByPackageNameQuery.GetAppByPackageName.toApiModel(): App = App(
        id = id,
        packageIcon = packageIcon,
        packageLabel = packageLabel,
        category = category,
        uiRating = uiRating?.toFloat(),
        screenreadersRating = screenreadersRating?.toFloat(),
        labelsRating = labelsRating?.toFloat(),
        functionsRating = functionsRating?.toFloat(),
        performanceRating = performanceRating?.toFloat(),
        totalRating = totalRating.toFloat(),
        numberOfRatings = numberOfRatings,
        availablePacks = availablePacks,
        packs = packs?.toApiModel(),
        ratings = ratings?.toApiModel()
    )

fun GetAppByPackageNameQuery.Packs.toApiModel(): PackConnection = PackConnection(
    packs = edges.mapNotNull { it?.node?.toApiModel() },
    hasNextPage = pageInfo.hasNextPage,
    nextPageToken = pageInfo.endCursor
)

fun GetAppByPackageNameQuery.Node.toApiModel(): Pack = Pack(
    id = id,
    numberOfLabels = numberOfLabels,
    downloads = downloads,
    language = language,
    hash = hash,
    createdAt = createdAt,
    updatedAt = updatedAt,
    user = user?.toApiModel()
)

fun GetAppByPackageNameQuery.User.toApiModel(): User = User(
    id = id,
    name = name,
    picture = picture
)

fun GetAppByPackageNameQuery.Ratings.toApiModel(): RatingConnection = RatingConnection(
    ratings = edges.mapNotNull { it?.node?.toApiModel() },
    hasNextPage = pageInfo.hasNextPage,
    nextPageToken = pageInfo.endCursor
)

fun GetAppByPackageNameQuery.Node1.toApiModel(): Rating {
    val totalRating: Double = ((ui + screenreaders + labels + functions + performance) / 5).toDouble()
    return Rating(
        id = id,
        ui = ui,
        screenreaders = screenreaders,
        labels = labels,
        functions = functions,
        performance = performance,
        total = totalRating.toFloat(),
        comment = comment,
        commentLanguage = commentLanguage,
        createdAt = createdAt,
        updatedAt = updatedAt,
        user = user?.toApiModel()
    )
}

fun GetAppByPackageNameQuery.User1.toApiModel(): User = User(
    id = id,
    name = name,
    picture = picture
)

fun GetAppOnlyQuery.GetApp.toApiModel(): App = App(
    uiRating = uiRating?.toFloat(),
    screenreadersRating = screenreadersRating?.toFloat(),
    labelsRating = labelsRating?.toFloat(),
    functionsRating = functionsRating?.toFloat(),
    performanceRating = performanceRating?.toFloat(),
    totalRating = totalRating.toFloat()
)

fun GetAppPacksQuery.Node.toApiModel(): Pack = Pack(
    id = id,
    numberOfLabels = numberOfLabels,
    downloads = downloads,
    language = language,
    hash = hash,
    createdAt = createdAt,
    updatedAt = updatedAt,
    user = user?.toApiModel()
)

fun GetAppPacksQuery.User.toApiModel(): User = User(
    id = id,
    name = name,
    picture = picture
)

fun GetAppPacksByPackageNameQuery.Node.toApiModel(): Pack = Pack(
    id = id,
    numberOfLabels = numberOfLabels,
    downloads = downloads,
    language = language,
    hash = hash,
    createdAt = createdAt,
    updatedAt = updatedAt,
    user = user?.toApiModel()
)

fun GetAppPacksByPackageNameQuery.User.toApiModel(): User = User(
    id = id,
    name = name,
    picture = picture
)

fun GetAppRatingsQuery.Node.toApiModel(): Rating {
    val totalRating: Double = ((ui + screenreaders + labels + functions + performance) / 5).toDouble()
    return Rating(
        id = id,
        ui = ui,
        screenreaders = screenreaders,
        labels = labels,
        functions = functions,
        performance = performance,
        total = totalRating.toFloat(),
        comment = comment,
        commentLanguage = commentLanguage,
        createdAt = createdAt,
        updatedAt = updatedAt,
        user = user?.toApiModel()
    )
}

fun GetAppRatingsQuery.User.toApiModel(): User = User(
    id = id,
    name = name,
    picture = picture
)

fun GetAppRatingsByPackageNameQuery.Node.toApiModel(): Rating {
    val totalRating: Double = ((ui + screenreaders + labels + functions + performance) / 5).toDouble()
    return Rating(
        id = id,
        ui = ui,
        screenreaders = screenreaders,
        labels = labels,
        functions = functions,
        performance = performance,
        total = totalRating.toFloat(),
        comment = comment,
        commentLanguage = commentLanguage,
        createdAt = createdAt,
        updatedAt = updatedAt,
        user = user?.toApiModel()
    )
}

fun GetAppRatingsByPackageNameQuery.User.toApiModel(): User = User(
    id = id,
    name = name,
    picture = picture
)

fun ListPacksQuery.Node.toApiModel(): Pack = Pack(
        id = id,
        numberOfLabels = numberOfLabels,
        downloads = downloads,
        language = language,
        hash = hash,
        user = user?.toApiModel(),
        app = app?.toApiModel(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun ListPacksQuery.User.toApiModel(): User =     User(
        id = id,
        name = name,
        picture = picture
    )

    fun ListPacksQuery.App.toApiModel(): App = App(
        id = id,
        packageName = packageName,
        packageIcon = packageIcon,
        packageLabel = packageLabel,
        category = category
    )

    fun ListRatingsQuery.Node.toApiModel(): Rating {
        val totalRating: Double = ((ui + screenreaders + labels + functions + performance) / 5).toDouble()
        return Rating(
            id = id,
            ui = ui,
            screenreaders = screenreaders,
            labels = labels,
            functions = functions,
            performance = performance,
            total = totalRating.toFloat(),
            comment = comment,
            commentLanguage = commentLanguage,
            createdAt = createdAt,
            updatedAt = updatedAt,
            user = user?.toApiModel(),
            app = app?.toApiModel()
        )
    }

    fun ListRatingsQuery.User.toApiModel(): User = User(
        id = id,
        name = name,
        picture = picture
    )

fun ListRatingsQuery.App.toApiModel(): App = App(
    id = id,
    packageName = packageName,
    packageIcon = packageIcon,
    packageLabel = packageLabel,
    category = category
)

    fun CreateUserMutation.User.toApiModel(): User = User(
        id = id,
        name = name,
        picture = picture
    )

    fun GetUserQuery.GetUser.toApiModel(): User = User(
        id = id,
        name = name,
        picture = picture,
        isPremium = isPremium
    )

    fun GetDeviceQuery.GetDevice.toApiModel(): Device = Device(
        id = id,
        name = name,
        language = language,
        country = country
    )

    fun CreateDeviceMutation.Device.toApiModel(): Device = Device(
        id = id,
        hardwareFingerprint = hardwareFingerprint,
        name = name,
        language = language,
        country = country
    )

fun UpdateDeviceMutation.Device.toApiModel(): Device = Device(
    id = id,
    hardwareFingerprint = hardwareFingerprint,
    name = name,
    language = language,
    country = country
)

fun CreateRatingMutation.Rating.toApiModel(): Rating = Rating(
        id = id
    )

    fun UpdateRatingMutation.Rating.toApiModel(): Rating = Rating(
        id = id
    )

    fun DownloadPackMutation.DownloadPack.toApiModel(request: InstallablePack): InstallablePack = InstallablePack(
        pack = request.pack,
        targetScreenreaders = targetScreenreader,
        translateTo = translateTo,
        installable = installablePack ?: JSONObject()
    )

    fun GetMembershipQuery.GetMembership.toApiModel(): Membership = Membership(
        id = id,
        expireAt = expireAt,
        isCanceled = isCanceled,
        cancelReason = cancelReason,
        token = token
    )

    fun ProcessMembershipMutation.Membership.toApiModel(): Membership = Membership(
        id = id,
        expireAt = expireAt,
        isCanceled = isCanceled,
        cancelReason = cancelReason,
        token = token
    )

    fun UpdateUserMutation.User.toApiModel(): User = User(
        id = id,
        name = name,
        picture = picture,
        isPremium = isPremium
    )

    fun LaunchSliMutation.LaunchSli.toApiModel(): InstallablePack = InstallablePack(
        targetScreenreaders = targetScreenreader,
        translateTo = targetLanguage,
        installable = installablePack ?: JSONObject()
    )

    fun Label.toLabelInput(): LabelInput = LabelInput(
        packageName = packageName,
        packageVersion = packageVersion,
        packageSignature = packageSignature,
        viewName = viewName,
        labelText = labelText,
        language = language
    )

    fun ProcessPacksMutation.ProcessPacks.toApiModel(): ProcessPacksResult = ProcessPacksResult(
        createdOrUpdated = createdOrUpdated,
        skipedOrDuplicated = skipedOrDuplicated,
        withErrors = withErrors
    )

    fun DownloadBackupMutation.DownloadBackup.toApiModel(): InstallablePack = InstallablePack(
        targetScreenreaders = targetScreenreader,
        installable = installablePack ?: JSONObject()
    )
