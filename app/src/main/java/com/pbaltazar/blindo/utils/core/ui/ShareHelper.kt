package com.pbaltazar.blindo.utils.core.ui

import android.content.Context
import android.content.Intent
import com.pbaltazar.blindo.utils.constants.BASE_URL_FOR_SHARES

@Suppress("unused")
object ShareHelper {
    fun shareUrl(
        context: Context,
        title: String,
        urlPath: String
    ) {
        Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "$BASE_URL_FOR_SHARES$urlPath")
                putExtra(Intent.EXTRA_TITLE, title)
                type = "text/plain"
            },
            title
        ).apply {
            context.startActivity(this)
        }
    }
}
