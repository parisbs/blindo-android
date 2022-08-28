package com.pbaltazar.blindo.services

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.app.NotificationCompat
import androidx.navigation.NavDeepLinkBuilder
import com.blindo.screenshotwatcher.ScreenshotWatcherDelegate
import com.blindo.screenshotwatcher.exceptions.PermissionException
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.utils.constants.VISION_AUTO_DISCARD_NOTIFICATIONS
import com.pbaltazar.blindo.utils.constants.VISION_NOTIFICATION_CHANNEL
import com.pbaltazar.blindo.utils.log.BlindoLogger
import com.pbaltazar.blindo.utils.notifications.NotificationsManager
import com.pbaltazar.blindo.utils.preferences.UserPreferences
import com.pbaltazar.blindo.utils.vision.BlindoVisionBridge
import com.pbaltazar.blindo.utils.vision.BlindoVisionServiceListener
import kotlinx.coroutines.FlowPreview
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@FlowPreview
class BlindoVisionService : AccessibilityService(),
    KoinComponent,
    ScreenshotWatcherDelegate.ScreenshotWatcherListener,
    BlindoVisionServiceListener {

    private val userPreferences: UserPreferences by inject()
        private lateinit var screenshotWatcherDelegate: ScreenshotWatcherDelegate

        private val missingPermissionsNotificationId = 9999
    private val screenshotDetectedNotificationId = 8888

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
        screenshotWatcherDelegate = ScreenshotWatcherDelegate(this, this as ScreenshotWatcherDelegate.ScreenshotWatcherListener)
        BlindoVisionBridge.listener = this

        NotificationsManager.initialize(this)
        if (NotificationsManager.isInitialized) {
            NotificationsManager.createNotificationChannel(
                VISION_NOTIFICATION_CHANNEL,
                getString(R.string.vision__service_name),
                getString(R.string.vision__service_summary),
                NotificationManager.IMPORTANCE_MAX
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                showMissingPermissionsNotification()
            }
        }

        startScreenshotWatcher()
    }

    private fun showMissingPermissionsNotification() {
        NavDeepLinkBuilder(this)
            .setGraph(R.navigation.main_navigation)
            .addDestination(R.id.navPermissions)
            .createPendingIntent().apply {
                NotificationsManager.createSimpleNotification(
                    icon = R.drawable.ic_blindo_192dp,
                    title = getString(R.string.permissions__activity_title),
                    body = getString(R.string.permissions__summary),
                    channelId = VISION_NOTIFICATION_CHANNEL,
                    priority = NotificationCompat.PRIORITY_MAX,
                    pendingIntent = this
                ).also { notification ->
                    NotificationsManager.notify(missingPermissionsNotificationId, notification)
                }
            }
    }

    override fun onScreenCaptured(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.apply {
                findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)?.also { node ->
                    val fullScreenshot: Bitmap = BitmapFactory.decodeStream(this)
                    this.close()
                    val bounds = Rect()
                    node.getBoundsInScreen(bounds)
                    latestNodeScreenshot = Bitmap.createBitmap(fullScreenshot, bounds.left, bounds.top, bounds.width(), bounds.height())
                    NavDeepLinkBuilder(this@BlindoVisionService)
                        .setGraph(R.navigation.main_navigation)
                        .setDestination(R.id.navVisionResults)
                        .createPendingIntent().apply {
                            NotificationsManager.createSimpleNotification(
                                icon = R.drawable.ic_blindo_192dp,
                                title = getString(R.string.vision__service_name),
                                body = getString(R.string.vision__screenshot_detected),
                                channelId = VISION_NOTIFICATION_CHANNEL,
                                priority = NotificationCompat.PRIORITY_MAX,
                                pendingIntent = this,
                                timeOutAfterMillis = userPreferences.getString(VISION_AUTO_DISCARD_NOTIFICATIONS, "15").let { timeOut ->
                                    if (timeOut.toInt() > 0) (timeOut.toInt() * 1000).toLong() else null
                                }
                            ).also { notification ->
                                NotificationsManager.notify(screenshotDetectedNotificationId, notification)
                            }
                        }
                }
            }
        } catch (e: Exception) {
            BlindoLogger.e(e)
        }
    }

    override fun onScreenCapturedFailure(t: Throwable) {
        if (t is PermissionException) showMissingPermissionsNotification()
        else BlindoLogger.e(t)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Do nothing
    }

    override fun onInterrupt() {
        stopScreenshotWatcher()
    }

    override fun onDestroy() {
        stopScreenshotWatcher()
        super.onDestroy()
    }
}
