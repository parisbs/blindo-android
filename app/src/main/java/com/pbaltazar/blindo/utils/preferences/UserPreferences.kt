package com.pbaltazar.blindo.utils.preferences

import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.entities.filters.common.IntRange
import com.pbaltazar.blindo.entities.filters.sorts.AppSort
import com.pbaltazar.blindo.entities.filters.sorts.RatingSort
import com.pbaltazar.blindo.utils.ads.AdsManager

interface UserPreferences {

    fun getAdsConsentStatus(): AdsManager.ConsentStatus
    fun setAdsConsentStatus(consentStatus: AdsManager.ConsentStatus): Boolean

    fun isFirstTime(): Boolean
    fun disableFirstTime(): Boolean

    fun isPrivacyPolicyAccepted(): Boolean
    fun acceptPrivacyPolicy(): Boolean

    fun getAppsPageSize(): Int
    fun setAppsPageSize(size: Int): Boolean

    fun getAppSort(): List<AppSort>
    fun setAppSort(sort: List<AppSort>): Boolean

    fun getIsTotalRatingRangeChecked(): Boolean
    fun setIsAppTotalRatingRangeChecked(isChecked: Boolean): Boolean

    fun getAppTotalRatingRange(): FloatRange
    fun setAppTotalRatingRange(floatRange: FloatRange): Boolean

    fun getCommentsPageSize(): Int
    fun setCommentsPageSize(size: Int): Boolean

    fun getCommentSort(): List<RatingSort>
    fun setCommentSort(sort: List<RatingSort>): Boolean

    fun getString(key: String, defaultValue: String): String
    fun setString(key: String, value: String): Boolean

    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun setBoolean(key: String, value: Boolean): Boolean

    fun getInt(key: String, defaultValue: Int): Int
    fun setInt(key: String, value: Int): Boolean

    fun getFloatRange(key: String, defaultValue: FloatRange): FloatRange
    fun setFloatRange(key: String, value: FloatRange): Boolean

    fun getIntRange(key: String, defaultValue: IntRange): IntRange
    fun setInRange(key: String, value: IntRange): Boolean

    fun registerOnUserPreferencesChangeListener(listener: OnUserPreferencesChangeListener): Boolean
    fun unregisterOnUserPreferencesChangeListener(listener: OnUserPreferencesChangeListener): Boolean
}
