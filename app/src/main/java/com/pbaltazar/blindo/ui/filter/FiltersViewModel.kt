package com.pbaltazar.blindo.ui.filter

import androidx.lifecycle.ViewModel
import com.pbaltazar.blindo.entities.filters.common.FloatRange
import com.pbaltazar.blindo.entities.filters.common.IntRange
import com.pbaltazar.blindo.utils.preferences.OnUserPreferencesChangeListener
import com.pbaltazar.blindo.utils.preferences.UserPreferences

class FiltersViewModel(
    private val userPreferences : UserPreferences
) : ViewModel() {

    fun registerOnUserPreferencesChangeListener(listener : OnUserPreferencesChangeListener): Boolean =
        userPreferences.registerOnUserPreferencesChangeListener(listener)

    fun unregisterOnUserPreferencesChangeListener(listener: OnUserPreferencesChangeListener): Boolean =
        userPreferences.unregisterOnUserPreferencesChangeListener(listener)

    fun getString(key: String, defValue: String): String =
        userPreferences.getString(key, defValue)

    fun setString(key: String, value: String): Boolean =
        userPreferences.setString(key, value)

    fun getBoolean(key: String, defValue: Boolean): Boolean =
        userPreferences.getBoolean(key, defValue)

    fun setBoolean(key: String, value: Boolean): Boolean =
        userPreferences.setBoolean(key, value)

    fun getFloatRange(key: String, defValue: FloatRange): FloatRange =
        userPreferences.getFloatRange(key, defValue)

    fun setFloatRange(key: String, value: FloatRange): Boolean =
        userPreferences.setFloatRange(key, value)

    fun getIntRange(key: String, defValue: IntRange): IntRange =
        userPreferences.getIntRange(key, defValue)

    fun setIntRange(key: String, value: IntRange): Boolean =
        userPreferences.setInRange(key, value)
}
