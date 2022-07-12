package com.pbaltazar.blindo.utils.core.ui

import android.os.Bundle
import androidx.annotation.XmlRes
import androidx.preference.PreferenceFragmentCompat

open class BlindoPreferencesFragment(
    @XmlRes val preferenceScreenResource: Int
) : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(preferenceScreenResource, rootKey)
    }
}
