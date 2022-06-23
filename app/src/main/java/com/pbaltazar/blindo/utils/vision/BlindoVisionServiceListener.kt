package com.pbaltazar.blindo.utils.vision

import android.graphics.Bitmap

interface BlindoVisionServiceListener {
    fun getNodeScreenshot(): Bitmap? = null
    fun startScreenshotWatcher() = Unit
    fun stopScreenshotWatcher() = Unit
    fun isWatching(): Boolean = false
}
