package com.pbaltazar.blindo.ui.preferences

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.pbaltazar.blindo.R

class BlindoVisionPreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.blindo_vision_preferences, rootKey)
    }
}
