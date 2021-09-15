package com.pbaltazar.blindo.utils.core

import android.os.Build

object Capabilities {

    fun isAtLeastAndroid8(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}
