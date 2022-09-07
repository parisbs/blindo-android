package com.pbaltazar.blindo.ui.devtools.billing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pbaltazar.blindo.databinding.DevtoolsBillingBinding
import com.pbaltazar.blindo.utils.billing.ui.BilleableFragment
import com.pbaltazar.blindo.utils.billing.ui.BillingViewModel

class BillingFragment : BilleableFragment<DevtoolsBillingBinding>() {

    private lateinit var billingClientStatus: TextView

    override val isSearchable: Boolean
        get() { return false }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DevtoolsBillingBinding.inflate(inflater, container, false)
        billingClientStatus = binding!!.billingClientStatus
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeBillingConnection()
    }

    override fun onBillingConnection(billingConnection: BillingViewModel.BillingConnection) {
        billingClientStatus.text = "Billing client state: ${billingConnection::class.simpleName}"
    }
}
