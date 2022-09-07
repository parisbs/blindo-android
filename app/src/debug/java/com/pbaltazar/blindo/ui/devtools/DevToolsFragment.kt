package com.pbaltazar.blindo.ui.devtools

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.utils.core.ui.BlindoPreferencesFragment

class DevToolsFragment : BlindoPreferencesFragment(R.xml.devtools_screen) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findPreference<Preference>("devToolsBilling")?.setOnPreferenceClickListener {
            findNavController().navigate(
                DevToolsFragmentDirections.goToDevToolsBilling()
            )
            true
        }
        findPreference<Preference>("devToolsDevice")?.setOnPreferenceClickListener {
            findNavController().navigate(
                DevToolsFragmentDirections.goToDevToolsDevice()
            )
            true
        }
        findPreference<Preference>("devToolsPreferences")?.setOnPreferenceClickListener {
            findNavController().navigate(
                DevToolsFragmentDirections.goToDevToolsPreferences()
            )
            true
        }
    }
}
