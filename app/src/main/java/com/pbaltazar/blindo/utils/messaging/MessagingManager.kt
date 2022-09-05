package com.pbaltazar.blindo.utils.messaging

import android.app.NotificationManager
import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.utils.constants.NEWS_NOTIFICATION_CHANNEL
import com.pbaltazar.blindo.utils.notifications.NotificationsManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MessagingManager(
    private val firebaseMessaging: FirebaseMessaging
) {

    companion object {
        enum class Topic {
            NEWS,
            UPDATES;
        }
    }

    private val messagingTokenChannel: Channel<String?> = Channel(Channel.UNLIMITED)
    val messagingTokenFlow: Flow<String?> get() = messagingTokenChannel.receiveAsFlow()

    fun getDeviceMessagingToken() {
            firebaseMessaging.token.addOnCompleteListener { task ->
                if (task.isSuccessful) messagingTokenChannel.trySend(task.result)
            }
        }

    fun registerNotificationChannels(context: Context) {
        if (NotificationsManager.isInitialized.not()) {
            throw RuntimeException("NotificationsManager is not initialized.")
        }
        NotificationsManager.createNotificationChannel(
            NEWS_NOTIFICATION_CHANNEL,
            context.getString(R.string.notification__news_channel_title),
            context.getString(R.string.notification__news_channel_description),
            NotificationManager.IMPORTANCE_HIGH
        )
    }

    suspend fun subscribeToTopic(topic: Topic): Boolean =
        suspendCoroutine { continuation ->
            firebaseMessaging.subscribeToTopic(topic.name.lowercase()).addOnCompleteListener { task ->
                continuation.resume(task.isSuccessful)
            }
        }

    suspend fun unsubscribeToTopic(topic: Topic): Boolean =
        suspendCoroutine { continuation ->
            firebaseMessaging.unsubscribeFromTopic(topic.name.lowercase()).addOnCompleteListener { task ->
                continuation.resume(task.isSuccessful)
            }
        }
}
