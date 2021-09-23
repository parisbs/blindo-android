package com.pbaltazar.blindo.utils.preferences

import com.pbaltazar.blindo.entities.enums.AppSort
import com.pbaltazar.blindo.entities.enums.RatingSort
import com.pbaltazar.blindo.entities.enums.PackSort
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

    fun getPacksPageSize(): Int
    fun setPacksPageSize(size: Int): Boolean

    fun getPackSort(): List<PackSort>
    fun setPackSort(sort: List<PackSort>): Boolean

    fun getCommentsPageSize(): Int
    fun setCommentsPageSize(size: Int): Boolean

    fun getCommentSort(): List<RatingSort>
    fun setCommentSort(sort: List<RatingSort>): Boolean
}
