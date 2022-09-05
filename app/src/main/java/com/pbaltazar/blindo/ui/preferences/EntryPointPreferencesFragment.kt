package com.pbaltazar.blindo.ui.preferences

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.utils.core.ui.BlindoPreferencesFragment
import com.pbaltazar.blindo.utils.messaging.MessagingManager
import com.pbaltazar.blindo.utils.messaging.ui.MessagingViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EntryPointPreferencesFragment : BlindoPreferencesFragment(R.xml.preference_screen_entry_point) {

    private val messagingViewModel: MessagingViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBlindoVision()
        setNotificationSettings()
        setAdsSettings()
        setClearCache()
        setClearSearchHistory()
    }

    private fun setBlindoVision() {
        findPreference<Preference>("vision")?.apply {
            setOnPreferenceClickListener {
                findNavController().navigate(
                    EntryPointPreferencesFragmentDirections.goToBlindoVisionSettings()
                )
                true
            }
        }
    }

    private fun setNotificationSettings() {
        findPreference<CheckBoxPreference>(messagingViewModel.getTopicPreferenceName(MessagingManager.Companion.Topic.NEWS))?.apply {
            isChecked = messagingViewModel.isSubscribedToTopic(MessagingManager.Companion.Topic.NEWS)
            setOnPreferenceClickListener {
                if (isChecked) messagingViewModel.subscribeToTopic(MessagingManager.Companion.Topic.NEWS)
                else messagingViewModel.unsubscribeToTopic(MessagingManager.Companion.Topic.NEWS)
                return@setOnPreferenceClickListener true
            }
        }
    }

    private fun setAdsSettings() {
        findPreference<Preference>("adsSettings")?.apply {
            setOnPreferenceClickListener {
                findNavController().navigate(
                    EntryPointPreferencesFragmentDirections.goToAdsSettings(closeAfterUpdate = false)
                )
                true
            }
        }
    }

    private fun setClearCache() {
        findPreference<Preference>("clearCache")?.apply {
            setOnPreferenceClickListener {
                findNavController().navigate(
                    EntryPointPreferencesFragmentDirections.clearCache()
                )
                true
            }
        }
    }

    private fun setClearSearchHistory() {
        findPreference<Preference>("clearSearchHistory")?.apply {
            setOnPreferenceClickListener {
                findNavController().navigate(
                    EntryPointPreferencesFragmentDirections.clearSearchHistory()
                )
                true
            }
        }
    }
}
