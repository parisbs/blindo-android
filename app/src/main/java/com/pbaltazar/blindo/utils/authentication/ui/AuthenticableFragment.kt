package com.pbaltazar.blindo.utils.authentication.ui

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.pbaltazar.blindo.entities.User
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class AuthenticableFragment : Fragment() {

    private val authenticationViewModel: AuthenticationViewModel by sharedViewModel()

    private var user: User? = null

    val loginScreen = registerForActivityResult(AuthenticationContract()) { signedUser ->
        setUser(signedUser)
    }

    abstract fun onSubscribeUser()

    fun subscribeUser() = authenticationViewModel.user.observe(this, Observer {
        user = it
        authenticationViewModel.propagateVerifiedStatus()
        onSubscribeUser()
    })

    fun getUser(): User? = user

    fun setUser(user: User?) = authenticationViewModel.setUser(user)

    abstract fun onSubscribeAuthentication(userAuthentication: AuthenticationViewModel.UserAuthentication)

    fun subscribeAuthentication() = authenticationViewModel.authentication.observe(this, Observer {
        onSubscribeAuthentication(it)
    })

    fun authenticateUser() = authenticationViewModel.authenticateUser()

    abstract fun onSubscribeUserUpdate(userUpdate: AuthenticationViewModel.UserUpdate)

    fun subscribeUserUpdate() = authenticationViewModel.userUpdate.observe(this, Observer {
        onSubscribeUserUpdate(it)
    })

    fun updateUser(user: User) = authenticationViewModel.updateUser(user)

    fun setIsUserPremium(isUserPremium: Boolean) = authenticationViewModel.setIsUserPremium(isUserPremium)

    fun signOut(propagateSignOutStateToViewModel: Boolean = true) = authenticationViewModel.signOut(propagateSignOutStateToViewModel)
}
