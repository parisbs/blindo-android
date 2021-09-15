package com.pbaltazar.blindo.utils.log

import timber.log.Timber

object BlindoLogger {

    private const val TAG = "BlindoApp"

    val log: Timber.Tree = Timber.tag(TAG)
}
