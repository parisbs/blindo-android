package com.pbaltazar.blindo.ui.app.details.pages

import com.pbaltazar.blindo.entities.App
import com.pbaltazar.blindo.ui.app.details.AppViewModel

interface AppViewModelListener {
    fun getCurrentApp(): App? = null
    fun getAppViewModel(): AppViewModel? = null
}
