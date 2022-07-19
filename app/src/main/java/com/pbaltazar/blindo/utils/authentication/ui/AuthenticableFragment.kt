package com.pbaltazar.blindo.utils.authentication.ui

import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class AuthenticableFragment<VB : ViewBinding> : BlindoFragment<VB>(),
    AuthenticableCallbacks {

    private val authenticationViewModel: AuthenticationViewModel by sharedViewModel()

    val authenticableActivity: AuthenticableActivity? get() {
        if (requireActivity() is AuthenticableActivity) {
            return requireActivity() as AuthenticableActivity
        } else {
            return null
        }
    }

    val requireAuthenticableActivity: AuthenticableActivity get() {
        return authenticableActivity ?: throw NullPointerException("Parent AuthenticableActivity is null.")
    }

    fun launchLoginScreen() = requireAuthenticableActivity.launchLoginScreen()

    fun getUser(): User? = requireAuthenticableActivity.getUser()

    fun subscribeUser() = authenticationViewModel.user.observe(this, Observer {
        authenticationViewModel.propagateVerifiedStatus()
        onSubscribeUser(it)
    })

    fun subscribeAuthentication() = authenticationViewModel.authentication.observe(this, Observer {
        onSubscribeAuthentication(it)
    })

    fun authenticateUser() = requireAuthenticableActivity.authenticateUser()

    fun subscribeUserUpdates() = authenticationViewModel.userUpdates.observe(this, Observer {
        onSubscribeUserUpdates(it)
    })

    fun updateUser(user: User) = requireAuthenticableActivity.updateUser(user)

    fun setUserCoinsLeft(coinsLeft: Int) = requireAuthenticableActivity.setUserCoinsLeft(coinsLeft)

    fun setIsUserPremium(isUserPremium: Boolean) = requireAuthenticableActivity.setIsUserPremium(isUserPremium)

    fun signOut(propagateSignOutStateToViewModel: Boolean = true) = requireAuthenticableActivity.signOut(propagateSignOutStateToViewModel)
}
