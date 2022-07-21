package com.pbaltazar.blindo.utils.log

import com.pbaltazar.blindo.BuildConfig
import timber.log.Timber

object BlindoLogger {

    private const val TAG = "BlindoApp"

    private val log: Timber.Tree = Timber.tag(TAG)

    fun e(t: Throwable) = e(t, null)

    fun e(message: String, vararg args: Any?) = e(null, message, *args)

    fun e(t: Throwable? = null, message: String? = null, vararg args: Any?) {
        log.e(t, message, *args)
    }

    fun w(t: Throwable) = w(t, null)

    fun w(message: String, vararg args: Any?) = w(null, message, *args)

    fun w(t: Throwable? = null, message: String? = null, vararg args: Any?) {
        log.w(t, message, *args)
    }

    fun i(t: Throwable) = i(t, null)

    fun i(message: String, vararg args: Any?) = i(null, message, *args)

    fun i(t: Throwable? = null, message: String? = null, vararg args: Any?) {
        if (BuildConfig.DEBUG) {
            log.i(t, message, *args)
        }
    }

    fun d(t: Throwable) = d(t, null)

    fun d(message: String, vararg args: Any?) = d(null, message, *args)

    fun d(t: Throwable? = null, message: String? = null, vararg args: Any?) {
        if (BuildConfig.DEBUG) {
            log.d(t, message, *args)
        }
    }

    fun v(t: Throwable) = v(t, null)

    fun v(message: String, vararg args: Any?) = v(null, message, *args)

    fun v(t: Throwable? = null, message: String? = null, vararg args: Any?) {
        if (BuildConfig.DEBUG) {
            log.v(t, message, *args)
        }
    }
}
