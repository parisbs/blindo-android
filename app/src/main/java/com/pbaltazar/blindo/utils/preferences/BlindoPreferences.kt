package com.pbaltazar.blindo.utils.preferences

import android.content.SharedPreferences
import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.entities.filters.common.IntRange
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.constants.ADS_CONSENT_STATUS
import com.pbaltazar.blindo.utils.constants.DEFAULT_ADS_CONSENT_STATUS
import com.pbaltazar.blindo.utils.constants.IS_FIRST_TIME
import com.pbaltazar.blindo.utils.constants.IS_PRIVACY_POLICY_ACCEPTED
import com.pbaltazar.blindo.utils.extensions.putAndCommit

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
