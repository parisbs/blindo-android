package com.pbaltazar.blindo.ui.devtools.device

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pbaltazar.blindo.databinding.DevtoolsDeviceBinding
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment

class DeviceFragment : AuthenticableFragment<DevtoolsDeviceBinding>() {

    private lateinit var hardwareFingerprint: TextView
    private lateinit var messagingToken: TextView

    override val isSearchable: Boolean
        get() { return false }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DevtoolsDeviceBinding.inflate(inflater, container, false)
        hardwareFingerprint = binding!!.hardwareFingerprint
        messagingToken = binding!!.messagingToken
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hardwareFingerprint.text = "Hardware fingerprint: ${requireAuthenticableActivity.hardwareFingerprint}"
        messagingToken.text = "Messaging token: ${getLatestStoragedDeviceMessagingToken()}"
    }

}
