package com.pbaltazar.blindo.utils.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.utils.constants.UPDATES_CHANNEL
import com.pbaltazar.blindo.utils.core.Capabilities
import kotlinx.coroutines.channels.Channel

object MessagingManager {

    private lateinit var firebaseMessaging: FirebaseMessaging
    private lateinit var context: Context
    private var isInitialized: Boolean = false

    private val tokenChannel: Channel<String> = Channel(Channel.UNLIMITED)

    private val channels: MutableList<NotificationChannel> = mutableListOf()

    fun initialize(context: Context) {
        this.context = context
        firebaseMessaging = FirebaseMessaging.getInstance()
        isInitialized = true
    }

    fun isInitialized(): Boolean =
        isInitialized &&
            this::firebaseMessaging.isInitialized &&
            this::context.isInitialized

    private fun verifyIsInitialized() {
        if (isInitialized.not()) {
            throw UninitializedPropertyAccessException("Messaging manager is not initialized")
        }
    }

    suspend fun getMessagingToken(): String {
        verifyIsInitialized()
        firebaseMessaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful.not()) {
                throw task.exception ?: RuntimeException("Unable to get the messaging token")
            }
            tokenChannel.trySend(task.result)
        }
        return tokenChannel.receive()
    }

    fun registerNotificationChannels(context: Context) {
        verifyIsInitialized()
        if (Capabilities.isAtLeastAndroid8()) {
            if (channels.isEmpty()) {
                channels.add(
                    NotificationChannel(
                        UPDATES_CHANNEL,
                        context.getString(R.string.messaging__channel_updates),
                        NotificationManager.IMPORTANCE_MAX
                    ).apply {
                        description = context.getString(R.string.messaging__channel_updates_description)
                    }
                )
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                createNotificationChannels(channels)
            }
        }
    }
}
