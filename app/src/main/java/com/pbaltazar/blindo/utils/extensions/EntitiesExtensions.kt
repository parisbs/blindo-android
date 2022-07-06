package com.pbaltazar.blindo.utils.extensions

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.apollographql.apollo3.api.Optional
import com.google.firebase.perf.metrics.AddTrace
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.entities.InstallablePack
import com.pbaltazar.blindo.entities.Membership
import com.pbaltazar.blindo.entities.Pack
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.enums.MembershipState
import com.pbaltazar.blindo.entities.filters.AppFilters
import com.pbaltazar.blindo.entities.filters.PackFilters
import com.pbaltazar.blindo.entities.filters.RatingFilters
import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.entities.filters.common.IntRange
import com.pbaltazar.blindo.graphql.type.AppFilter
import com.pbaltazar.blindo.graphql.type.PackFilter
import com.pbaltazar.blindo.graphql.type.RatingFilter
import com.pbaltazar.blindo.graphql.type.SupportedScreenreadersEnum
import com.pbaltazar.blindo.utils.constants.DOWNLOADS_DIR
import com.pbaltazar.blindo.utils.constants.LABELS_PROVIDER
import com.pbaltazar.blindo.utils.constants.TALKBACK_ARRAY_PACKAGE_NAME
import com.pbaltazar.blindo.utils.constants.TALKBACK_LABELS_ARRAY
import com.pbaltazar.blindo.utils.log.BlindoLogger
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

const val MONTHLY_LONG_VALUE = 2592000

@AddTrace(name = "getTalkbackInstallablePackUri", enabled = true)
fun Pack.getTalkbackInstallableFileUri(context: Context): Uri? =
    File(context.filesDir, "${DOWNLOADS_DIR}/${this.hash}.blp").let { packFile ->
        if (packFile.exists()) {
            BlindoLogger.log.i("Retrieving file ${packFile.absolutePath}")
            FileProvider.getUriForFile(context, LABELS_PROVIDER, packFile)
        } else {
            BlindoLogger.log.e("The pack file ${packFile} not exists")
            null
        }
    }

@AddTrace(name = "saveTalkbackInstallablePackFile", enabled = true)
fun InstallablePack.saveTalkbackInstallableFile(context: Context): Uri? =
    File(context.filesDir, DOWNLOADS_DIR).let { dir ->
        if (dir.exists().not()) {
            BlindoLogger.log.i("Creating downloads internal directory...")
            dir.mkdir()
        }
        File(dir, "${this.pack.hash}.blp").let { packFile ->
            OutputStreamWriter(FileOutputStream(packFile)).apply {
                write(this@saveTalkbackInstallableFile.installable.toString())
                close()
            }
            this.pack.getTalkbackInstallableFileUri(context)
        }
    }

fun Membership.isActive(): Boolean = state.equals(MembershipState.SUBSCRIPTION_STATE_ACTIVE) ||
    state.equals(MembershipState.SUBSCRIPTION_STATE_IN_GRACE_PERIOD) || state.equals(MembershipState.SUBSCRIPTION_STATE_CANCELED)

fun Membership.isExpired(): Boolean = state.equals(MembershipState.SUBSCRIPTION_STATE_EXPIRED)

fun InstallablePack.countLabels(): Int {
    if (targetScreenreaders == SupportedScreenreadersEnum.TALKBACK) {
        return installable.optJSONArray(TALKBACK_LABELS_ARRAY)?.length() ?: 0
    } else {
        return 0
    }
}

fun InstallablePack.countApps(): Int {
    if (targetScreenreaders == SupportedScreenreadersEnum.TALKBACK) {
        return installable.optJSONArray(TALKBACK_LABELS_ARRAY)?.let { labelsArray ->
                val apps = emptyList<String>().toMutableList()
                for (i in 0 until labelsArray.length()) {
                    labelsArray.optJSONObject(i).optString(TALKBACK_ARRAY_PACKAGE_NAME, "").also { packageName ->
                        if (packageName.isNotEmpty() && apps.indexOf(packageName) < 0) {
                            apps.add(packageName)
                        }
                    }
                }
                apps.size
            } ?: 0
    } else {
        return 0
    }
}

fun User.getAuthenticationMethod(): String = picture?.let {
    when {
        it.contains("google") == true -> "Google"
        it.contains(".fb.") == true -> "Facebook"
        it.contains("twitter") == true -> "Twitter"
        else -> "Unknown"
    }
} ?: "Email"

fun AppFilters.toGraphQLFilter(): AppFilter = AppFilter(
    totalRatingRange = Optional.presentIfNotNull(totalRatingRange?.toGraphQLFilter()),
    or = Optional.presentIfNotNull(let {
        val clauses: MutableList<AppFilter> = mutableListOf()
        clauses.addAll(getPackageNameClausesList())
        clauses.addAll(getPackageLabelClausesList())
        clauses.toList()
    })
)

fun AppFilters.getPackageNameClausesList(): List<AppFilter> = packageName?.let { pn ->
    listOf(
        AppFilter(packageNameIlike = Optional.presentIfNotNull("%$pn")),
        AppFilter(packageNameIlike = Optional.presentIfNotNull("%${pn}%")),
        AppFilter(packageNameIlike = Optional.presentIfNotNull("${pn}%")),
        AppFilter(packageNameIlike = Optional.presentIfNotNull(pn))
    )
} ?: emptyList()

fun AppFilters.getPackageLabelClausesList(): List<AppFilter> = packageLabel?.let { pl ->
    listOf(
        AppFilter(packageLabelIlike = Optional.presentIfNotNull("%$pl")),
        AppFilter(packageLabelIlike = Optional.presentIfNotNull("%${pl}%")),
        AppFilter(packageLabelIlike = Optional.presentIfNotNull("${pl}%")),
        AppFilter(Optional.presentIfNotNull(pl))
    )
} ?: emptyList()

fun FloatRange.toGraphQLFilter(): com.pbaltazar.blindo.graphql.type.FloatRange =
    com.pbaltazar.blindo.graphql.type.FloatRange(
        begin = begin.toDouble(),
        end = end.toDouble()
    )

fun FloatRange.toIntRange(): IntRange = IntRange(
    begin = Math.round(begin),
    end = Math.round(end)
)

fun IntRange.toFloatRange(): FloatRange = FloatRange(
    begin = begin.toFloat(),
    end = end.toFloat()
)

fun PackFilters.toGraphQlFilter(): PackFilter = PackFilter(
    languageIn = Optional.presentIfNotNull(languageIn)
)

fun RatingFilters.toGraphQLFilter(): RatingFilter = RatingFilter(
    commentIsNull = Optional.presentIfNotNull(commentIsNull),
    commentLanguageIn = Optional.presentIfNotNull(commentLanguageIn)
)

fun MembershipState.toReadableString(context: Context): String = context.getString(
    when (this) {
        MembershipState.SUBSCRIPTION_STATE_UNSPECIFIED -> R.string.membership__subscription_state_unspecified
        MembershipState.SUBSCRIPTION_STATE_PENDING -> R.string.membership__subscription_state_pending
        MembershipState.SUBSCRIPTION_STATE_ACTIVE -> R.string.membership__subscription_state_active
        MembershipState.SUBSCRIPTION_STATE_PAUSED -> R.string.membership__subscription_state_paused
        MembershipState.SUBSCRIPTION_STATE_IN_GRACE_PERIOD -> R.string.membership__subscription_state_in_grace_period
        MembershipState.SUBSCRIPTION_STATE_ON_HOLD -> R.string.membership__subscription_state_on_hold
        MembershipState.SUBSCRIPTION_STATE_CANCELED -> R.string.membership__subscription_state_canceled
        MembershipState.SUBSCRIPTION_STATE_EXPIRED -> R.string.membership__subscription_state_expired
    }
)

fun MembershipState.getInfoString(context: Context): String = context.getString(
    when (this) {
        MembershipState.SUBSCRIPTION_STATE_UNSPECIFIED -> R.string.membership__subscription_state_unspecified_info
        MembershipState.SUBSCRIPTION_STATE_PENDING -> R.string.membership__subscription_state_pending_info
        MembershipState.SUBSCRIPTION_STATE_ACTIVE -> R.string.membership__subscription_state_active_info
        MembershipState.SUBSCRIPTION_STATE_PAUSED -> R.string.membership__subscription_state_paused_info
        MembershipState.SUBSCRIPTION_STATE_IN_GRACE_PERIOD -> R.string.membership__subscription_state_in_grace_period_info
        MembershipState.SUBSCRIPTION_STATE_ON_HOLD -> R.string.membership__subscription_state_on_hold_info
        MembershipState.SUBSCRIPTION_STATE_CANCELED -> R.string.membership__subscription_state_canceled_info
        MembershipState.SUBSCRIPTION_STATE_EXPIRED -> R.string.membership__subscription_state_expired_info
    }
)
