package com.pbaltazar.blindo.ui.dialog.search

import android.app.Dialog
import android.os.Bundle
import android.provider.SearchRecentSuggestions
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.utils.search.RecentSearchesProvider

class ClearSearchHistory : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = activity?.let {
        AlertDialog.Builder(it)
            .setTitle(R.string.settings__clear_search_history_title)
            .setMessage(R.string.settings__clear_search_history_message)
            .setPositiveButton(
                R.string.settings__clear_search_history_clear
            ) { _, _ ->
                SearchRecentSuggestions(it, RecentSearchesProvider.AUTHORITY, RecentSearchesProvider.MODE)
                    .clearHistory()
                findNavController().popBackStack()
            }
            .setNegativeButton(
                R.string.settings__clear_search_history_no
            ) { _, _ ->
                findNavController().popBackStack()
            }
            .create()
    } ?: throw IllegalStateException("${requireContext()} must have non null activity")
}
