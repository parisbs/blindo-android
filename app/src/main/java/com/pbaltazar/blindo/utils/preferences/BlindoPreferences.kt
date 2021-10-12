package com.pbaltazar.blindo.utils.preferences

import android.content.SharedPreferences
import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.entities.filters.common.IntRange
import com.pbaltazar.blindo.entities.filters.sorts.AppSort
import com.pbaltazar.blindo.entities.filters.sorts.RatingSort
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.constants.*
import com.pbaltazar.blindo.utils.extensions.getEnumsList
import com.pbaltazar.blindo.utils.extensions.putAndCommit
import com.pbaltazar.blindo.utils.extensions.putAndCommitEnumsList

class BlindoPreferences(
    private val sharedPreferences: SharedPreferences
) : UserPreferences {

    private val baseListener: SharedPreferences.OnSharedPreferenceChangeListener = object : SharedPreferences.OnSharedPreferenceChangeListener {
        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
            listeners.forEach { onUserPreferencesChangeListener ->
                onUserPreferencesChangeListener.onUserPreferencesChange(key)
            }
        }
    }
    private val listeners: MutableList<OnUserPreferencesChangeListener> = mutableListOf()

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(baseListener)
    }

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

    override fun getIsTotalRatingRangeChecked(): Boolean = sharedPreferences.getBoolean(APP_TOTAL_RATING_RANGE_CHECKED, false)

    override fun setIsAppTotalRatingRangeChecked(isChecked: Boolean): Boolean = sharedPreferences.putAndCommit(APP_TOTAL_RATING_RANGE_CHECKED, isChecked)

    override fun getAppTotalRatingRange(): FloatRange = FloatRange(
        begin = sharedPreferences.getFloat(APP_TOTAL_RATING_RANGE_BEGIN, 1.0F),
        end = sharedPreferences.getFloat(APP_TOTAL_RATING_RANGE_END, 5.0F)
    )

    override fun setAppTotalRatingRange(floatRange: FloatRange): Boolean =
        sharedPreferences.putAndCommit(APP_TOTAL_RATING_RANGE_BEGIN, floatRange.begin) &&
            sharedPreferences.putAndCommit(APP_TOTAL_RATING_RANGE_END, floatRange.end)

    override fun getCommentsPageSize(): Int = sharedPreferences.getInt(COMMENTS_PAGE_SIZE, 25)

    override fun setCommentsPageSize(size: Int): Boolean = sharedPreferences.putAndCommit(COMMENTS_PAGE_SIZE, size)

    override fun getCommentSort(): List<RatingSort> = sharedPreferences.getEnumsList<RatingSort>(
        COMMENT_SORT,
        listOf<RatingSort>(
            RatingSort.UPDATED_AT_DESC
        )
    )

    override fun setCommentSort(sort: List<RatingSort>): Boolean = sharedPreferences.putAndCommitEnumsList(COMMENT_SORT, sort)

    override fun getString(key: String, defaultValue: String): String =
        sharedPreferences.getString(key, defaultValue) ?: defaultValue

    override fun setString(key: String, value: String): Boolean =
        sharedPreferences.putAndCommit(key, value)

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        sharedPreferences.getBoolean(key, defaultValue)

    override fun setBoolean(key: String, value: Boolean): Boolean =
        sharedPreferences.putAndCommit(key, value)

    override fun getInt(key: String, defaultValue: Int): Int =
        sharedPreferences.getInt(key, defaultValue)

    override fun setInt(key: String, value: Int): Boolean =
        sharedPreferences.putAndCommit(key, value)

    override fun getFloatRange(key: String, defaultValue: FloatRange): FloatRange =
        FloatRange(
            begin = sharedPreferences.getFloat("${key}_begin", defaultValue.begin),
            end = sharedPreferences.getFloat("${key}_end", defaultValue.end)
        )

    override fun setFloatRange(key: String, value: FloatRange): Boolean =
        sharedPreferences.putAndCommit("${key}_begin", value.begin) && sharedPreferences.putAndCommit("${key}_end", value.end)

    override fun getIntRange(key: String, defaultValue: IntRange): IntRange =
        IntRange(
            begin = sharedPreferences.getInt("${key}_begin", defaultValue.begin),
            end = sharedPreferences.getInt("${key}_end", defaultValue.end)
        )

    override fun setInRange(key: String, value: IntRange): Boolean =
        sharedPreferences.putAndCommit("${key}_begin", value.begin) && sharedPreferences.putAndCommit("${key}_end", value.end)

    override fun registerOnUserPreferencesChangeListener(listener: OnUserPreferencesChangeListener): Boolean =
        listeners.add(listener)

    override fun unregisterOnUserPreferencesChangeListener(listener: OnUserPreferencesChangeListener): Boolean =
        listeners.remove(listener)
}
