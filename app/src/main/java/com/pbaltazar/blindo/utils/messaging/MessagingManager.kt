package com.pbaltazar.blindo.utils.messaging

import android.app.NotificationManager
import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.utils.constants.UPDATES_CHANNEL
import com.pbaltazar.blindo.utils.notifications.NotificationsManager

object MessagingManager {

    private lateinit var context: Context
    private lateinit var firebaseMessaging: FirebaseMessaging

    val isInitialized: Boolean get() {
        return this::context.isInitialized &&
            this::firebaseMessaging.isInitialized
    }

    fun initialize(context: Context) {
        this.context = context
        firebaseMessaging = FirebaseMessaging.getInstance()
    }

    fun registerNotificationChannels() {
        if (NotificationsManager.isInitialized.not()) {
            throw RuntimeException("NotificationsManager is not initialized.")
        }
        NotificationsManager.createNotificationChannel(
            UPDATES_CHANNEL,
            context.getString(R.string.messaging__channel_updates),
            context.getString(R.string.messaging__channel_updates_description),
            NotificationManager.IMPORTANCE_DEFAULT
        )
    }
}
