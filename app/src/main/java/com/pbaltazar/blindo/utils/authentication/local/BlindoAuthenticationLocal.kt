package com.pbaltazar.blindo.utils.authentication.local

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.entities.User

class BlindoAuthenticationLocal(
    private val context: Context,
    private val accountManager: AccountManager
) : AuthenticationLocal {

    private val ID = "id"
    private val SUB = "sub"
    private val EMAIL = "email"
    private val NAME = "name"
    private val PICTURE = "picture"
    private val IS_VERIFIED = "isVerified"
    private val IS_PREMIUM = "isPremium"

    override fun registerLocalAccount(user: User): Boolean = Account(
        user.email,
        context.getString(R.string.account_type)
    ).let { account ->
        Bundle().let { extra ->
            extra.putString(ID, user.id)
            extra.putString(SUB, user.sub)
            extra.putString(EMAIL, user.email)
            extra.putString(NAME, user.name)
            extra.putString(PICTURE, user.picture)
            extra.putString(IS_VERIFIED, user.isVerified.toString())
            extra.putString(IS_PREMIUM, user.isPremium.toString())
            accountManager.addAccountExplicitly(account, user.id, extra)
        }
    }

    override fun getLocalAccount(): User? = accountManager.getAccountsByType(
        context.getString(R.string.account_type)
    ).lastOrNull()?.let { account ->
        User(
            id = accountManager.getUserData(account, ID),
            sub = accountManager.getUserData(account, SUB),
            email = accountManager.getUserData(account, EMAIL),
            name = accountManager.getUserData(account, NAME),
            picture = accountManager.getUserData(account, PICTURE),
            isVerified = accountManager.getUserData(account, IS_VERIFIED).toBoolean(),
            isPremium = accountManager.getUserData(account, IS_PREMIUM).toBoolean()
        )
    }

    override fun updateLocalAccount(user: User): User? = accountManager.getAccountsByType(
        context.getString(R.string.account_type)
    ).lastOrNull()?.let { account ->
        accountManager.setUserData(account, ID, user.id)
        accountManager.setUserData(account, SUB, user.sub)
        accountManager.setUserData(account, EMAIL, user.email)
        accountManager.setUserData(account, NAME, user.name)
        accountManager.setUserData(account, PICTURE, user.picture)
        accountManager.setUserData(account, IS_PREMIUM, user.isPremium.toString())
        getLocalAccount()
    }

    override fun setLocalAccountIsVerified(isVerified: Boolean): User? = accountManager.getAccountsByType(
        context.getString(R.string.account_type)
    ).lastOrNull()?.let { account ->
        accountManager.setUserData(account, IS_VERIFIED, isVerified.toString())
        getLocalAccount()
    }

    override fun setLocalAccountIsPremium(isPremium: Boolean): User? = accountManager.getAccountsByType(
        context.getString(R.string.account_type)
    ).lastOrNull()?.let { account ->
        accountManager.setUserData(account, IS_PREMIUM, isPremium.toString())
        getLocalAccount()
    }

    override fun unregisterLocalAccount() {
        accountManager.getAccountsByType(context.getString(R.string.account_type)).forEach { account ->
            accountManager.removeAccountExplicitly(account)
        }
    }
}
