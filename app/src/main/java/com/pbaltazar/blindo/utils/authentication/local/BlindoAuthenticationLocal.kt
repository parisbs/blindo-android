package com.pbaltazar.blindo.utils.authentication.local

import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import com.pbaltazar.blindo.BuildConfig
import com.pbaltazar.blindo.entities.Device
import com.pbaltazar.blindo.entities.User

@SuppressLint("MissingPermission")
class BlindoAuthenticationLocal(
    private val accountManager: AccountManager
) : AuthenticationLocal {

    companion object {
        private const val ID = "id"
        private const val SUB = "sub"
        private const val EMAIL = "email"
        private const val NAME = "name"
        private const val PICTURE = "picture"
        private const val COINS_LEFT = "coinsLeft"
        private const val IS_VERIFIED = "isVerified"
        private const val IS_PREMIUM = "isPremium"
        private const val MESSAGING_TOKEN = "messagingToken"
    }

    override fun registerLocalAccount(user: User): Boolean = Account(
        user.email,
        BuildConfig.ACCOUNT_TYPE
    ).let { account ->
        Bundle().let { extra ->
            extra.putString(ID, user.id)
            extra.putString(SUB, user.sub)
            extra.putString(EMAIL, user.email)
            extra.putString(NAME, user.name)
            extra.putString(PICTURE, user.picture)
            extra.putInt(COINS_LEFT, user.coinsLeft)
            extra.putString(IS_VERIFIED, user.isVerified.toString())
            extra.putString(IS_PREMIUM, user.isPremium.toString())
            extra.putString(MESSAGING_TOKEN, user.device.gcmToken)
            accountManager.addAccountExplicitly(account, user.id, extra)
        }
    }

    override fun getLocalAccount(): User? = accountManager.getAccountsByType(
        BuildConfig.ACCOUNT_TYPE
    ).lastOrNull()?.let { account ->
        User(
            id = accountManager.getUserData(account, ID),
            sub = accountManager.getUserData(account, SUB),
            email = accountManager.getUserData(account, EMAIL),
            name = accountManager.getUserData(account, NAME),
            picture = accountManager.getUserData(account, PICTURE),
            coinsLeft = accountManager.getUserData(account, COINS_LEFT).let { coinsLeft ->
                coinsLeft?.toInt() ?: 0
            },
            isVerified = accountManager.getUserData(account, IS_VERIFIED).toBoolean(),
            isPremium = accountManager.getUserData(account, IS_PREMIUM).toBoolean(),
            device = Device(
                gcmToken = accountManager.getUserData(account, MESSAGING_TOKEN)
            )
        )
    }

    override fun updateLocalAccount(user: User): User? = accountManager.getAccountsByType(
        BuildConfig.ACCOUNT_TYPE
    ).lastOrNull()?.let { account ->
        accountManager.setUserData(account, ID, user.id)
        accountManager.setUserData(account, SUB, user.sub)
        accountManager.setUserData(account, EMAIL, user.email)
        accountManager.setUserData(account, NAME, user.name)
        accountManager.setUserData(account, PICTURE, user.picture)
        accountManager.setUserData(account, COINS_LEFT, user.coinsLeft.toString())
        accountManager.setUserData(account, IS_PREMIUM, user.isPremium.toString())
        getLocalAccount()
    }

    override fun setLocalAccountIsVerified(isVerified: Boolean): User? = accountManager.getAccountsByType(
        BuildConfig.ACCOUNT_TYPE
    ).lastOrNull()?.let { account ->
        accountManager.setUserData(account, IS_VERIFIED, isVerified.toString())
        getLocalAccount()
    }

    override fun setLocalAccountCoinsLeft(coinsLeft: Int): User? = accountManager.getAccountsByType(
        BuildConfig.ACCOUNT_TYPE
    ).lastOrNull()?.let { account ->
        accountManager.setUserData(account, COINS_LEFT, coinsLeft.toString())
        getLocalAccount()
    }

    override fun setLocalAccountIsPremium(isPremium: Boolean): User? = accountManager.getAccountsByType(
        BuildConfig.ACCOUNT_TYPE
    ).lastOrNull()?.let { account ->
        accountManager.setUserData(account, IS_PREMIUM, isPremium.toString())
        getLocalAccount()
    }

    override fun saveDeviceMessagingToken(messagingToken: String) {
        accountManager.getAccountsByType(BuildConfig.ACCOUNT_TYPE).lastOrNull()?.also { account ->
            accountManager.setUserData(account, MESSAGING_TOKEN, messagingToken)
        }
    }

    override fun getLatestStoragedDeviceMessagingToken(): String? = getLocalAccount()?.device?.gcmToken

    override fun unregisterLocalAccount(): Boolean {
        val isSuccess: MutableList<Boolean> = mutableListOf()
        accountManager.getAccountsByType(BuildConfig.ACCOUNT_TYPE).forEach { account ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                isSuccess.add(accountManager.removeAccountExplicitly(account))
            } else {
                @Suppress("DEPRECATION")
                accountManager.removeAccount(account, null, null)
                isSuccess.add(true)
            }
        }
        return isSuccess.contains(false).not()
    }
}
