package com.pbaltazar.blindo.ui.preferences

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.utils.core.ui.BlindoPreferencesFragment

class EntryPointPreferencesFragment : BlindoPreferencesFragment(R.xml.preference_screen_entry_point) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBlindoVision()
        setAdsSettings()
        setClearCache()
        setClearSearchHistory()
    }

    private fun setBlindoVision() {
        findPreference<Preference>("vision")?.apply {
            setOnPreferenceClickListener { _ ->
                findNavController().navigate(
                    EntryPointPreferencesFragmentDirections.goToBlindoVisionSettings()
                )
                true
            }
        }
    }

    private fun setAdsSettings() {
        findPreference<Preference>("adsSettings")?.apply {
            setOnPreferenceClickListener { _ ->
                findNavController().navigate(
                    EntryPointPreferencesFragmentDirections.goToAdsSettings(closeAfterUpdate = false)
                )
                true
            }
        }
    }

    private fun setClearCache() {
        findPreference<Preference>("clearCache")?.apply {
            setOnPreferenceClickListener { _ ->
                findNavController().navigate(
                    EntryPointPreferencesFragmentDirections.clearCache()
                )
                true
            }
        }
    }

    private fun setClearSearchHistory() {
        findPreference<Preference>("clearSearchHistory")?.apply {
            setOnPreferenceClickListener { _ ->
                findNavController().navigate(
                    EntryPointPreferencesFragmentDirections.clearSearchHistory()
                )
                true
            }
        }
    }
}
