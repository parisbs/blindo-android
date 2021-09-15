package com.pbaltazar.blindo.utils.preferences

import android.content.SharedPreferences
import com.pbaltazar.blindo.entities.enums.AppSort
import com.pbaltazar.blindo.entities.enums.CommentSort
import com.pbaltazar.blindo.entities.enums.PackSort
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.constants.*
import com.pbaltazar.blindo.utils.extensions.getEnumsList
import com.pbaltazar.blindo.utils.extensions.putAndCommit
import com.pbaltazar.blindo.utils.extensions.putAndCommitEnumsList

class BlindoPreferences(
    private val sharedPreferences: SharedPreferences
) : UserPreferences {

    override fun getAdsConsentStatus(): AdsManager.ConsentStatus =
        sharedPreferences.getString(ADS_CONSENT_STATUS, DEFAULT_ADS_CONSENT_STATUS)!!.let {
            AdsManager.ConsentStatus.valueOf(it)
        }

    override fun setAdsConsentStatus(consentStatus: AdsManager.ConsentStatus): Boolean =
        sharedPreferences.putAndCommit(ADS_CONSENT_STATUS, consentStatus.name)

    override fun isFirstTime(): Boolean = sharedPreferences.getBoolean(IS_FIRST_TIME, true)

    override fun disableFirstTime() = sharedPreferences.putAndCommit(IS_FIRST_TIME, false)

    override fun isPrivacyPolicyAccepted(): Boolean = sharedPreferences.getBoolean(IS_PRIVACY_POLICY_ACCEPTED, false)

    override fun acceptPrivacyPolicy(): Boolean = sharedPreferences.putAndCommit(IS_PRIVACY_POLICY_ACCEPTED, true)

    override fun getAppsPageSize(): Int = sharedPreferences.getInt(APPS_PAGE_SIZE, 50)

    override fun setAppsPageSize(size: Int) = sharedPreferences.putAndCommit(APPS_PAGE_SIZE, size)

    override fun getAppSort(): List<AppSort> = sharedPreferences.getEnumsList<AppSort>(
        APP_SORT,
        listOf<AppSort>(
            AppSort.AVAILABLE_PACKS_DESC,
            AppSort.TOTAL_RATING_ASC
        )
    )

    override fun setAppSort(sort: List<AppSort>): Boolean = sharedPreferences.putAndCommitEnumsList(APP_SORT, sort)

    override fun getPacksPageSize(): Int = sharedPreferences.getInt(PACKS_PAGE_SIZE, 25)

    override fun setPacksPageSize(size: Int): Boolean = sharedPreferences.putAndCommit(PACKS_PAGE_SIZE, size)

    override fun getPackSort(): List<PackSort> = sharedPreferences.getEnumsList<PackSort>(
        PACK_SORT,
        listOf<PackSort>(
            PackSort.UPDATED_AT_DESC
        )
    )

    override fun setPackSort(sort: List<PackSort>): Boolean = sharedPreferences.putAndCommitEnumsList(PACK_SORT, sort)

    override fun getCommentsPageSize(): Int = sharedPreferences.getInt(COMMENTS_PAGE_SIZE, 25)

    override fun setCommentsPageSize(size: Int): Boolean = sharedPreferences.putAndCommit(COMMENTS_PAGE_SIZE, size)

    override fun getCommentSort(): List<CommentSort> = sharedPreferences.getEnumsList<CommentSort>(
        COMMENT_SORT,
        listOf<CommentSort>(
            CommentSort.UPDATED_AT_DESC
        )
    )

    override fun setCommentSort(sort: List<CommentSort>): Boolean = sharedPreferences.putAndCommitEnumsList(COMMENT_SORT, sort)
}
