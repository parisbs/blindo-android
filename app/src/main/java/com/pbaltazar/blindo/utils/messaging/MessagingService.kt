package com.pbaltazar.blindo.utils.messaging

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pbaltazar.blindo.utils.log.BlindoLogger

class MessagingService : FirebaseMessagingService(){

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data.isNotEmpty()) {
            BlindoLogger.i(message.data.toString())
        }
        super.onMessageReceived(message)
    }
}
