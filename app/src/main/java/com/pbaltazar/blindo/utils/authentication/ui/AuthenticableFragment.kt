package com.pbaltazar.blindo.utils.authentication.ui

import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class AuthenticableFragment<VB : ViewBinding> : BlindoFragment<VB>(),
    AuthenticableCallbacks {

    private val authenticationViewModel: AuthenticationViewModel by sharedViewModel()

    private var user: User? = null

    val loginScreen = registerForActivityResult(AuthenticationContract()) { signedUser ->
        setUser(signedUser)
    }

    override fun onDestroy() {
        super.onDestroy()
        loginScreen.unregister()
    }

    fun subscribeUser() = authenticationViewModel.user.observe(this, Observer {
        user = it
        authenticationViewModel.propagateVerifiedStatus()
        onSubscribeUser()
    })

    fun getUser(): User? = user

    fun setUser(user: User?) = authenticationViewModel.setUser(user)

    fun subscribeAuthentication() = authenticationViewModel.authentication.observe(this, Observer {
        onSubscribeAuthentication(it)
    })

    fun authenticateUser() = authenticationViewModel.authenticateUser()

    fun subscribeUserUpdate() = authenticationViewModel.userUpdate.observe(this, Observer {
        onSubscribeUserUpdate(it)
    })

    fun updateUser(user: User) = authenticationViewModel.updateUser(user)

    fun setIsUserPremium(isUserPremium: Boolean) = authenticationViewModel.setIsUserPremium(isUserPremium)

    fun signOut(propagateSignOutStateToViewModel: Boolean = true) = authenticationViewModel.signOut(propagateSignOutStateToViewModel)
}
