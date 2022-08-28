package com.pbaltazar.blindo.ui.dialog.auth

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.utils.constants.AUTH_CANCELED_ON_DIALOG

class RequiresAuth : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = activity?.let {
        AlertDialog.Builder(it)
            .setTitle(R.string.dialogrequiresauth__title)
            .setMessage(R.string.dialogrequiresauth__message)
            .setPositiveButton(
                R.string.dialogrequiresauth__continue
            ) { _, _ ->
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    AUTH_CANCELED_ON_DIALOG,
                    false
                )
                findNavController().popBackStack()
            }
            .setNegativeButton(
                R.string.dialogrequiresauth__later
            ) { _, _ ->
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    AUTH_CANCELED_ON_DIALOG,
                    true
                )
                findNavController().popBackStack()
            }
            .create()
    } ?: throw IllegalStateException("${requireContext()} must have non null activity")
}
