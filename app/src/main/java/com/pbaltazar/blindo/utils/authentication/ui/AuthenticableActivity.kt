package com.pbaltazar.blindo.utils.authentication.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.pbaltazar.blindo.entities.Device
import com.pbaltazar.blindo.entities.User
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class AuthenticableActivity : AppCompatActivity() {

    private val authenticationViewModel: AuthenticationViewModel by viewModel()

    private var user: User? = null
    private var device: Device? = null

    val loginScreen = registerForActivityResult(AuthenticationContract()) { signedUser ->
        setUser(signedUser)
    }

    abstract fun onSubscribeUser()

    fun subscribeUser() = authenticationViewModel.user.observe(this, Observer {
        user = it
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

    abstract fun onIsValidationEmailSent(isValidationEmailSent: Boolean)

    fun subscribeIsValidationEmailSent() = authenticationViewModel.isValidationEmailSent.observe(this, Observer {
        onIsValidationEmailSent(it)
    })

    fun sendVerificationEmail() = authenticationViewModel.sendVerificationEmail()

    fun propagateVerifiedStatus() = authenticationViewModel.propagateVerifiedStatus()

    abstract fun onSubscribeDevice()

    fun subscribeDevice() = authenticationViewModel.device.observe(this, Observer {
        device = it
        onSubscribeDevice()
    })

    fun getDevice(): Device? = device

    abstract fun onSubscribeDeviceAuthentication(deviceAuthentication: AuthenticationViewModel.DeviceAuthentication)

    fun subscribeDeviceAuthentication() = authenticationViewModel.deviceAuthentication.observe(this, Observer {
        onSubscribeDeviceAuthentication(it)
    })

    fun authenticateDevice(device: Device) = authenticationViewModel.authenticateDevice(device)

    abstract fun onSubscribeDeviceUpdate(deviceUpdate: AuthenticationViewModel.DeviceUpdate)

    fun subscribeDeviceUpdate() = authenticationViewModel.deviceUpdate.observe(this, Observer {
        onSubscribeDeviceUpdate(it)
    })

    fun updateDevice(device: Device) = authenticationViewModel.updateDevice(device)

    fun signOut(propagateSignOutStateToViewModel: Boolean = true) = authenticationViewModel.signOut(propagateSignOutStateToViewModel)
}
