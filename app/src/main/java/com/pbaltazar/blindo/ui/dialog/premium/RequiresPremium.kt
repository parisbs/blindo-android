package com.pbaltazar.blindo.ui.dialog.premium

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.pbaltazar.blindo.R

class RequiresPremium : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = activity?.let {
        AlertDialog.Builder(it)
            .setTitle(R.string.dialogrequirespremium__title)
            .setMessage(R.string.dialogrequirespremium__message)
            .setPositiveButton(
                R.string.dialogrequirespremium__get_premium
            ) { _, _ ->
                findNavController().navigate(
                    RequiresPremiumDirections.actionFromRequiresPremiumToPremium()
                )
            }
            .setNegativeButton(
                R.string.dialogrequirespremium__later
            ) { _, _ ->
                findNavController().popBackStack()
            }
            .create()
    } ?: throw IllegalStateException("${requireContext()} must have non null activity")
}
