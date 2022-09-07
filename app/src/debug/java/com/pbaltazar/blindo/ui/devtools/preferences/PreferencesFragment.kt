package com.pbaltazar.blindo.ui.devtools.preferences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pbaltazar.blindo.databinding.DevtoolsPreferencesBinding
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import com.pbaltazar.blindo.utils.preferences.UserPreferences
import org.koin.android.ext.android.inject

class PreferencesFragment : BlindoFragment<DevtoolsPreferencesBinding>() {

    private val userPreferences: UserPreferences by inject()

    private lateinit var isPrivacyPolicyAccepted: TextView

    override val isSearchable: Boolean
        get() { return false }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DevtoolsPreferencesBinding.inflate(inflater, container, false)
        isPrivacyPolicyAccepted = binding!!.isPrivacyPolicyAccepted
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isPrivacyPolicyAccepted.text = "Is privacy policy accepted: ${userPreferences.isPrivacyPolicyAccepted()}"
    }
}
