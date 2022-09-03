package com.pbaltazar.blindo.utils.authentication.ui

import androidx.viewbinding.ViewBinding
import com.pbaltazar.blindo.entities.Device
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

abstract class AuthenticableFragment<VB : ViewBinding> : BlindoFragment<VB>(),
    AuthenticableCallbacks {

    private val authenticationViewModel: AuthenticationViewModel by sharedViewModel()

    val authenticableActivity: AuthenticableActivity? get() {
        return if (requireActivity() is AuthenticableActivity) {
            requireActivity() as AuthenticableActivity
        } else {
            null
        }
    }

    val requireAuthenticableActivity: AuthenticableActivity get() {
        return authenticableActivity ?: throw NullPointerException("Parent AuthenticableActivity is null.")
    }

    fun launchLoginScreen() = requireAuthenticableActivity.launchLoginScreen()

    fun getUser(): User? = requireAuthenticableActivity.getUser()

    fun subscribeUser() = authenticationViewModel.user.observe(this) {
        authenticationViewModel.propagateVerifiedStatus()
        onSubscribeUser(it)
    }

    fun subscribeAuthentication() = authenticationViewModel.authentication.observe(this) {
        onSubscribeAuthentication(it)
    }

    fun authenticateUser() = requireAuthenticableActivity.authenticateUser()

    fun subscribeUserUpdates() = authenticationViewModel.userUpdates.observe(this) {
        onSubscribeUserUpdates(it)
    }

    fun updateUser(user: User) = requireAuthenticableActivity.updateUser(user)

    fun setUserCoinsLeft(coinsLeft: Int) = requireAuthenticableActivity.setUserCoinsLeft(coinsLeft)

    fun updateUserCoinsBalance() = requireAuthenticableActivity.updateUserCoinsBalance()

    fun setIsUserPremium(isUserPremium: Boolean) = requireAuthenticableActivity.setIsUserPremium(isUserPremium)

    fun getDevice(): Device? = requireAuthenticableActivity.getDevice()

    fun subscribeDevice() = authenticationViewModel.device.observe(this) {
        onSubscribeDevice(it)
    }

    fun subscribeDeviceRegistration() = authenticationViewModel.deviceRegistration.observe(this) {
        onSubscribeDeviceRegistration(it)
    }

    fun registerDevice(device: Device) =
        requireAuthenticableActivity.registerDevice(device)

    fun subscribeDeviceUpdates() = authenticationViewModel.deviceUpdates.observe(this) {
        onSubscribeDeviceUpdates(it)
    }

    fun updateDevice(device: Device) =
        requireAuthenticableActivity.updateDevice(device)

    fun saveDeviceMessagingToken(messagingToken: String) =
        requireAuthenticableActivity.saveDeviceMessagingToken(messagingToken)

    fun getLatestStoragedDeviceMessagingToken(): String? =
        requireAuthenticableActivity.getLatestStoragedDeviceMessagingToken()

    fun signOut(propagateSignOutStateToViewModel: Boolean = true) = requireAuthenticableActivity.signOut(propagateSignOutStateToViewModel)
}
