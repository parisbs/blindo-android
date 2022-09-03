package com.pbaltazar.blindo.utils.messaging.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.pbaltazar.blindo.utils.messaging.MessagingManager

class MessagingViewModel(
    private val messagingManager: MessagingManager,
) : ViewModel() {

    val messagingToken: LiveData<String?> = messagingManager.messagingTokenFlow.asLiveData()

    fun getDeviceMessagingToken() =
        messagingManager.getDeviceMessagingToken()
}
