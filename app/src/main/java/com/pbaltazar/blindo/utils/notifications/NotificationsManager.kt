package com.pbaltazar.blindo.utils.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pbaltazar.blindo.utils.core.Capabilities
import com.pbaltazar.blindo.utils.log.BlindoLogger

object NotificationsManager {

    private lateinit var context: Context
    private lateinit var notificationManager: NotificationManager

    val isInitialized: Boolean get() {
        return this::context.isInitialized &&
            this::notificationManager.isInitialized
    }

    fun initialize(context: Context) {
        this.context = context
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun createNotificationChannel(id: String, name: String, description: String, importance: Int) {
        if (Capabilities.isAtLeastAndroid8() && isInitialized) {
            NotificationChannel(id, name, importance).apply {
                setDescription(description)
                notificationManager.createNotificationChannel(this)
            }
        } else BlindoLogger.log.e("NotificationsManager is not initialized.")
    }

    fun createSimpleNotification(
        @DrawableRes icon: Int,
        title: String,
        body: String,
        channelId: String,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        pendingIntent: PendingIntent? = null,
        autoCancel: Boolean = true,
        timeOutAfterMillis: Long? = null
    ): Notification {
        if (isInitialized) {
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(priority)
                .setAutoCancel(autoCancel)
            pendingIntent?.also {
                notification.setContentIntent(it)
            }
            timeOutAfterMillis?.also {
                notification.setTimeoutAfter(it)
            }
            return notification.build()
        } else {
            throw UninitializedPropertyAccessException("NotificationsManager is not initialized.")
        }
    }

    fun notify(id: Int, notification: Notification) {
        with(NotificationManagerCompat.from(context)) {
            this.notify(id, notification)
        }
    }
}
