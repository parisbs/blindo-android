package com.pbaltazar.blindo.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.blindo.apollito.api.ApollitoClient
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.data.BASIC_APOLLITO_CLIENT
import org.koin.android.ext.android.inject
import org.koin.core.qualifier.named

class ClearCache : DialogFragment() {

    private val blindoApiClient: ApollitoClient by inject(named(BASIC_APOLLITO_CLIENT))

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = requireActivity().let {
        AlertDialog.Builder(it)
            .setTitle(R.string.settings__clear_cache_title)
            .setMessage(R.string.settings__clear_cache_message)
            .setPositiveButton(
                R.string.settings__clear_cache_continue,
                DialogInterface.OnClickListener { _, _ ->
                    blindoApiClient.apolloStore().clearAll()
                    findNavController().popBackStack()
                }
            )
            .setNegativeButton(
                R.string.settings__clear_cache_cancel,
                DialogInterface.OnClickListener { _, _ ->
                    findNavController().popBackStack()
                }
            )
            .create()
    }
}
