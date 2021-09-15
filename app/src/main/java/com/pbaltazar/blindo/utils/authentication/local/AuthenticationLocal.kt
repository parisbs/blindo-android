package com.pbaltazar.blindo.utils.authentication.local

import com.pbaltazar.blindo.entities.User

interface AuthenticationLocal {

    fun registerLocalAccount(user: User): Boolean

    fun getLocalAccount(): User?

    fun updateLocalAccount(user: User): User?

    fun setLocalAccountIsVerified(isVerified: Boolean): User?

    fun setLocalAccountIsPremium(isPremium: Boolean): User?

    fun unregisterLocalAccount()
}
