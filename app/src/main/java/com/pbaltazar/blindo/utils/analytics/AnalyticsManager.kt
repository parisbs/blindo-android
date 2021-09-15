package com.pbaltazar.blindo.utils.analytics

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AnalyticsManager {

    private lateinit var firebaseAnalitycs: FirebaseAnalytics
    private var isInitialized: Boolean = false

    fun initialize() {
        Firebase.analytics
        isInitialized = true
    }

    fun isInitialized(): Boolean = isInitialized && this::firebaseAnalitycs.isInitialized
}
