package com.pbaltazar.blindo.utils.messaging.ui

import android.content.Context
import androidx.lifecycle.*
import com.pbaltazar.blindo.utils.constants.IS_PUSH_NOTIFICATIONS_CONFIGURED
import com.pbaltazar.blindo.utils.constants.IS_SUBSCRIBED_TO_TOPIC_PREFIX
import com.pbaltazar.blindo.utils.messaging.MessagingManager
import com.pbaltazar.blindo.utils.preferences.UserPreferences
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

@Suppress("unused")
class MessagingViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val userPreferences: UserPreferences,
    private val messagingManager: MessagingManager,
) : ViewModel() {

    val messagingToken: LiveData<String?> = messagingManager.messagingTokenFlow.asLiveData()

    private val _isSubscribedToTopic = MutableLiveData<Map<MessagingManager.Companion.Topic, Boolean>>()
    val isSubscribedToTopic: LiveData<Map<MessagingManager.Companion.Topic, Boolean>> get() = _isSubscribedToTopic

    fun initialize(context: Context) {
        messagingManager.registerNotificationChannels(context)
    }

    fun getDeviceMessagingToken() =
        messagingManager.getDeviceMessagingToken()

    fun isPushNotificationsConfigured(): Boolean =
        userPreferences.getBoolean(IS_PUSH_NOTIFICATIONS_CONFIGURED, false)

    fun setIsPushNotificationsConfigured(isConfigured: Boolean) =
        userPreferences.setBoolean(IS_PUSH_NOTIFICATIONS_CONFIGURED, isConfigured)

    fun getTopicPreferenceName(topic: MessagingManager.Companion.Topic): String =
        "$IS_SUBSCRIBED_TO_TOPIC_PREFIX${topic.name.lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }}"

    fun isSubscribedToTopic(topic: MessagingManager.Companion.Topic): Boolean =
        userPreferences.getBoolean(getTopicPreferenceName(topic), false)

    fun subscribeToTopic(topic: MessagingManager.Companion.Topic) = viewModelScope.launch(backgroundDispatcher) {
        if (isSubscribedToTopic(topic).not()) {
            val isSubscribed = messagingManager.subscribeToTopic(topic)
            userPreferences.setBoolean(getTopicPreferenceName(topic), isSubscribed)
            _isSubscribedToTopic.postValue(mapOf(Pair(topic, isSubscribed)))
        } else _isSubscribedToTopic.postValue(mapOf(Pair(topic, true)))
    }

    fun unsubscribeToTopic(topic: MessagingManager.Companion.Topic) = viewModelScope.launch(backgroundDispatcher) {
        if (isSubscribedToTopic(topic)) {
            val isUnsubscribed = messagingManager.unsubscribeToTopic(topic)
            userPreferences.setBoolean(getTopicPreferenceName(topic), isUnsubscribed.not())
            _isSubscribedToTopic.postValue(mapOf(Pair(topic, isUnsubscribed.not())))
        } else _isSubscribedToTopic.postValue(mapOf(Pair(topic, true)))
    }
}
