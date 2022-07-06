package com.pbaltazar.blindo.utils.extensions

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.view.View
import com.blindo.apollito.utils.extensions.toJson
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.entities.Label
import com.pbaltazar.blindo.utils.constants.*
import com.pbaltazar.blindo.utils.log.BlindoLogger
import org.json.JSONArray
import org.json.JSONObject
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

fun Date.toUiFormat(): String = SimpleDateFormat().let {
    it.format(this)
}

fun Uri.installTalkbackPack(view: View) {
    try {
        Intent().apply {
            component = ComponentName(TALKBACK_PACKAGE, TALKBACK_IMPORT_LABELS)
            data = this@installTalkbackPack
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            view.context.startActivity(this)
        }
    } catch (e: Exception) {
        FirebaseCrashlytics.getInstance().recordException(e)
        BlindoLogger.log.e(e)
        Snackbar.make(
            view,
            e.localizedMessage ?: e.toString(),
            Snackbar.LENGTH_LONG
        ).show()
    }
}

fun ApplicationInfo.toLocalModel(context: Context): App = App(
    packageName = packageName,
    packageLabel = loadLabel(context.packageManager).toString()
)

fun JSONArray.toLabelsList(): List<Label> {
    val labels = emptyList<Label>().toMutableList()
    for (i in 0 until this.length()) {
        labels.add(this.optJSONObject(i).toLabelModel())
    }
    return labels.toList()
}

fun JSONObject.toLabelModel(): Label = Label(
    packageName = this.optString(TALKBACK_ARRAY_PACKAGE_NAME, ""),
    packageVersion = this.optInt(TALKBACK_ARRAY_PACKAGE_VERSION, 0),
    packageSignature = this.optString(TALKBACK_ARRAY_PACKAGE_SIGNATURE, ""),
    viewName = this.optString(TALKBACK_ARRAY_VIEW_NAME, ""),
    labelText = this.optString(TALKBACK_ARRAY_LABEL_TEXT, ""),
    language = this.optString(TALKBACK_ARRAY_LOCALE, "")
)

fun List<Label>.countApps(): Int = this.let { labels ->
    val apps = emptyList<String>().toMutableList()
    labels.forEach { label ->
        if (apps.indexOf(label.packageName) < 0) {
            apps.add(label.packageName)
        }
    }
    apps.size
}

fun List<Label>.getLanguages(): List<String> = this.let { labels ->
    val languages = emptyList<String>().toMutableList()
    labels.forEach { label ->
        val language = Locale.Builder().setLanguage(label.language).build().displayLanguage
        if (languages.indexOf(language) < 0) {
            languages.add(language)
        }
    }
    languages.toList()
}

fun List<String>.toHumanReadable(): String = this.let {
    var formated = ""
    it.forEach { item ->
        if (formated.isNullOrEmpty()) {
            formated += item
        } else {
            formated += ", $item"
        }
    }
    formated
}

fun String.isNullOrEmptyOrBlank(): Boolean {
    if (isNullOrEmpty() || isBlank()) {
        return true
    } else {
        return false
    }
}

fun Float.toRatingString(locale: Locale = Locale.getDefault()): String = DecimalFormat.getNumberInstance(locale).let {
    it.minimumFractionDigits = 1
    it.maximumFractionDigits = 1
    it.format(this.toDouble())
}

fun String.toBlindoReceipt(): JSONObject = this.toJson().let { receipt ->
    JSONObject()
        .put(
            RECEIPT_ORIGIN_KEY,
            receipt.optString(RECEIPT_PACKAGENAME_KEY)
        )
        .put(
            RECEIPT_SKU_KEY,
            receipt.optString(RECEIPT_PRODUCTID_KEY)
        )
        .put(
            RECEIPT_TOKEN_KEY,
            receipt.optString(RECEIPT_PURCHASETOKEN_KEY)
        )
}
