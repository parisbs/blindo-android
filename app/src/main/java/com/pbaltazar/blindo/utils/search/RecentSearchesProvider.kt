package com.pbaltazar.blindo.utils.search

import android.content.SearchRecentSuggestionsProvider

class RecentSearchesProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY: String = "com.pbaltazar.blindo.utils.search.RecentSearchesProvider"
        const val MODE: Int = DATABASE_MODE_QUERIES
    }
}
