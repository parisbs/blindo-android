package com.pbaltazar.blindo.utils.extensions

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.firebase.perf.metrics.AddTrace
import com.pbaltazar.blindo.entities.*
import com.pbaltazar.blindo.graphql.type.SupportedScreenreadersEnum
import com.pbaltazar.blindo.utils.constants.DOWNLOADS_DIR
import com.pbaltazar.blindo.utils.constants.LABELS_PROVIDER
import com.pbaltazar.blindo.utils.constants.TALKBACK_ARRAY_PACKAGE_NAME
import com.pbaltazar.blindo.utils.constants.TALKBACK_LABELS_ARRAY
import com.wizeline.simpleapollo.utils.extensions.toJson
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.*

const val MONTHLY_LONG_VALUE = 2592000

@AddTrace(name = "getTalkbackInstallablePackUri", enabled = true)
fun Pack.getTalkbackInstallableFileUri(context: Context): Uri? =
    File(context.filesDir, "${DOWNLOADS_DIR}/${this.hash}.blp").let { packFile ->
        if (packFile.exists()) {
            Timber.i("Retrieving file ${packFile.absolutePath}")
            FileProvider.getUriForFile(context, LABELS_PROVIDER, packFile)
        } else {
            Timber.e("The pack file ${packFile} not exists")
            null
        }
    }

@AddTrace(name = "saveTalkbackInstallablePackFile", enabled = true)
fun InstallablePack.saveTalkbackInstallableFile(context: Context): Uri? =
    File(context.filesDir, DOWNLOADS_DIR).let { dir ->
        if (dir.exists().not()) {
            Timber.i("Creating downloads internal directory...")
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

fun BlindoPurchase.toLocalMembership(): Membership = this.originalJson.toJson().let { json ->
    Membership(
        expireAt = Date((json.optLong("purchaseTime", (System.currentTimeMillis() / 1000)) + MONTHLY_LONG_VALUE).toLong()),
        isCanceled = json.optBoolean("autoRenewing", true).not()
    )
}

fun Membership.isExpired(): Boolean = this.expireAt.before(Date(System.currentTimeMillis()))

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
