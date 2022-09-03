package com.pbaltazar.blindo.utils.messaging

import android.app.NotificationManager
import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.utils.constants.UPDATES_NOTIFICATION_CHANNEL
import com.pbaltazar.blindo.utils.notifications.NotificationsManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class MessagingManager(
    private val context: Context,
    private val firebaseMessaging: FirebaseMessaging
) {

    private val messagingTokenChannel: Channel<String?> = Channel(Channel.UNLIMITED)
    val messagingTokenFlow: Flow<String?> get() = messagingTokenChannel.receiveAsFlow()

    fun getDeviceMessagingToken() {
            firebaseMessaging.token.addOnCompleteListener { task ->
                if (task.isSuccessful) messagingTokenChannel.trySend(task.result)
            }
        }

    fun registerNotificationChannels() {
        if (NotificationsManager.isInitialized.not()) {
            throw RuntimeException("NotificationsManager is not initialized.")
        }
        NotificationsManager.createNotificationChannel(
            UPDATES_NOTIFICATION_CHANNEL,
            context.getString(R.string.notification__updates_channel_title),
            context.getString(R.string.notification__updates_channel_description),
            NotificationManager.IMPORTANCE_DEFAULT
        )
    }
}
