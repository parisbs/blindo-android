package com.pbaltazar.blindo.utils.authentication.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.pbaltazar.blindo.entities.User

class AuthenticationContract : ActivityResultContract<Unit, User?>() {

    companion object {
        const val ACTION_AUTHENTICATE_BLINDO_USER = "com.blindoapp.intent.action.AUTHENTICATE_USER"
        const val EXTRA_SIGNED_USER = "com.blindoapp.intent.extra.SIGNED_USER"
    }

    override fun createIntent(context: Context, input: Unit): Intent = Intent(ACTION_AUTHENTICATE_BLINDO_USER)

    override fun parseResult(resultCode: Int, intent: Intent?): User? = when (resultCode) {
        Activity.RESULT_OK -> intent?.getParcelableExtra(EXTRA_SIGNED_USER)
        else -> null
    }
}
