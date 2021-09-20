package com.pbaltazar.blindo.utils.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

object AnalyticsManager {

    private lateinit var firebaseAnalitycs: FirebaseAnalytics
    private var isInitialized: Boolean = false

    fun initialize() {
        Firebase.analytics
        isInitialized = true
    }

    fun isInitialized(): Boolean = isInitialized && this::firebaseAnalitycs.isInitialized

    fun registerEventWithoutParams(event: String) =
        if (isInitialized()) firebaseAnalitycs.logEvent(event, null) else Unit

    fun registerLoginEvent(method: String) = if (isInitialized())
        firebaseAnalitycs.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    else Unit
}
