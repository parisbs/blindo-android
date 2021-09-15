package com.pbaltazar.blindo.utils.messaging.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.utils.messaging.MessagingManager
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MessagingViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val context: Context
) : ViewModel() {

    private val refreshedToken = MutableLiveData<MessagingToken>()
    val messagingToken: LiveData<MessagingToken> get() = refreshedToken

    init {
        if (MessagingManager.isInitialized().not()) {
            MessagingManager.initialize(context)
        }
    }

    fun getRefreshedToken() = viewModelScope.launch(backgroundDispatcher) {
        try {
            refreshedToken.postValue(MessagingToken.Success(MessagingManager.getMessagingToken()))
        } catch (e: Exception) {
            refreshedToken.postValue(MessagingToken.Error(e))
        }
    }

    sealed class MessagingToken {
        class Success(val token: String) : MessagingToken()
        class Error(val throwable: Throwable) : MessagingToken()
    }
}
