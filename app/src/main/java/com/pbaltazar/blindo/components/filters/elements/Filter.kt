package com.pbaltazar.blindo.components.filters.elements

interface Filter {

    private object Properties {
        var preferencesKey: String? = null
    }

    fun getPreferencesKey(): String? = Properties.preferencesKey

    fun setPreferencesKey(preferencesKey: String) {
        Properties.preferencesKey = preferencesKey
    }
}
