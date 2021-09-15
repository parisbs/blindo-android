package com.pbaltazar.blindo.utils.authentication.local.system

import android.app.Service
import android.content.Intent
import android.os.IBinder

class AccountAuthenticatorService : Service() {

    private lateinit var accountAuthenticator: AccountAuthenticator

    override fun onCreate() {
        super.onCreate()
        accountAuthenticator = AccountAuthenticator(application)
    }

    override fun onBind(intent: Intent): IBinder = accountAuthenticator.iBinder
}
