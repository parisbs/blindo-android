package com.pbaltazar.blindo.services

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.navigation.NavDeepLinkBuilder
import com.blindo.screenshotwatcher.ScreenshotWatcherDelegate
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.ui.permissions.PermissionsActivity
import com.pbaltazar.blindo.utils.log.BlindoLogger
import com.pbaltazar.blindo.utils.vision.BlindoVisionBridge
import com.pbaltazar.blindo.utils.vision.BlindoVisionServiceListener

class BlindoVisionService : AccessibilityService(),
    ScreenshotWatcherDelegate.ScreenshotWatcherListener,
    BlindoVisionServiceListener {

        private lateinit var screenshotWatcherDelegate: ScreenshotWatcherDelegate

        private var latestNodeScreenshot: Bitmap? = null
        private var isWatching: Boolean = false
    set(value) {
        field = value
        if (field) {
            screenshotWatcherDelegate.startScreenshotWatcher()
        } else {
            screenshotWatcherDelegate.stopScreenshotWatcher()
        }
    }

    override fun getNodeScreenshot(): Bitmap? = latestNodeScreenshot

    override fun startScreenshotWatcher() {
        isWatching = true
    }

    override fun stopScreenshotWatcher() {
        isWatching = false
    }

    override fun isWatching(): Boolean = isWatching

    override fun onServiceConnected() {
        super.onServiceConnected()
        screenshotWatcherDelegate = ScreenshotWatcherDelegate(this, this)
        BlindoVisionBridge.listener = this as BlindoVisionServiceListener

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            Intent(this, PermissionsActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(this)
            }
        }

        startScreenshotWatcher()
    }

    override fun onScreenCaptured(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.apply {
                findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)?.also { node ->
                    val fullScreenshot: Bitmap = BitmapFactory.decodeStream(this)
                    this.close()
                    var bounds: Rect = Rect()
                    node.getBoundsInScreen(bounds)
                    latestNodeScreenshot = Bitmap.createBitmap(fullScreenshot, bounds.left, bounds.top, bounds.width(), bounds.height())
                    val pendingIntent = NavDeepLinkBuilder(this@BlindoVisionService)
                        .setGraph(R.navigation.main_navigation)
                        .setDestination(R.id.navVisionResults)
                        .createPendingIntent()
                    pendingIntent.send()
                    stopScreenshotWatcher()
                }
            }
        } catch (e: Exception) {
            BlindoLogger.log.e(e)
        }
    }

    override fun onScreenCapturedFailure(throwable: Throwable) = BlindoLogger.log.e(throwable)

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Do nothing
    }

    override fun onInterrupt() {
        stopScreenshotWatcher()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopScreenshotWatcher()
    }
}
