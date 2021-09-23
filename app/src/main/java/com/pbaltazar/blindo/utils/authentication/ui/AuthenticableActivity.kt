package com.pbaltazar.blindo.utils.authentication.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.pbaltazar.blindo.entities.Device
import com.pbaltazar.blindo.entities.User
import org.koin.androidx.viewmodel.ext.android.viewModel

open class AuthenticableActivity : AppCompatActivity(),
    AuthenticableCallbacks {

    private val authenticationViewModel: AuthenticationViewModel by viewModel()

    private var user: User? = null
    private var device: Device? = null

    val loginScreen = registerForActivityResult(AuthenticationContract()) { signedUser ->
        setUser(signedUser)
    }

    override fun onDestroy() {
        super.onDestroy()
        loginScreen.unregister()
    }

    fun subscribeUser() = authenticationViewModel.user.observe(this, Observer {
        user = it
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

    fun subscribeIsValidationEmailSent() = authenticationViewModel.isValidationEmailSent.observe(this, Observer {
        onIsValidationEmailSent(it)
    })

    fun sendVerificationEmail() = authenticationViewModel.sendVerificationEmail()

    fun propagateVerifiedStatus() = authenticationViewModel.propagateVerifiedStatus()

    fun subscribeDevice() = authenticationViewModel.device.observe(this, Observer {
        device = it
        onSubscribeDevice()
    })

    fun getDevice(): Device? = device

    fun subscribeDeviceAuthentication() = authenticationViewModel.deviceAuthentication.observe(this, Observer {
        onSubscribeDeviceAuthentication(it)
    })

    fun authenticateDevice(device: Device) = authenticationViewModel.authenticateDevice(device)

    fun subscribeDeviceUpdate() = authenticationViewModel.deviceUpdate.observe(this, Observer {
        onSubscribeDeviceUpdate(it)
    })

    fun updateDevice(device: Device) = authenticationViewModel.updateDevice(device)

    fun signOut(propagateSignOutStateToViewModel: Boolean = true) = authenticationViewModel.signOut(propagateSignOutStateToViewModel)
}
