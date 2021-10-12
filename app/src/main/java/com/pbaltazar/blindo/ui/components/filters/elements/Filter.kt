package com.pbaltazar.blindo.ui.components.filters.elements

interface Filter {

    private object Properties {
        var preferencesKey: String? = null
    }

    fun getPreferencesKey(): String? = Properties.preferencesKey

    fun setPreferencesKey(preferencesKey: String) {
        Properties.preferencesKey = preferencesKey
    }
}
