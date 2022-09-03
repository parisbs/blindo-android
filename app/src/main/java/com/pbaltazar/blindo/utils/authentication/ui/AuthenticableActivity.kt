package com.pbaltazar.blindo.utils.authentication.ui

import android.annotation.SuppressLint
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.pbaltazar.blindo.entities.Device
import com.pbaltazar.blindo.entities.User
import org.koin.androidx.viewmodel.ext.android.viewModel

open class AuthenticableActivity : AppCompatActivity(),
    AuthenticableCallbacks {

    private val authenticationViewModel: AuthenticationViewModel by viewModel()

    private val loginScreen = registerForActivityResult(AuthenticationContract()) { signedUser ->
        authenticationViewModel.setUser(signedUser)
    }

    val hardwareFingerprint: String @SuppressLint("HardwareIds")
    get() {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun onDestroy() {
        loginScreen.unregister()
        super.onDestroy()
    }

    fun launchLoginScreen() {
        loginScreen.launch(Unit)
    }

    fun getUser(): User? = authenticationViewModel.user.value

    fun subscribeUser() = authenticationViewModel.user.observe(this) {
        onSubscribeUser(it)
    }

    fun subscribeAuthentication() = authenticationViewModel.authentication.observe(this) {
        onSubscribeAuthentication(it)
    }

    fun authenticateUser() = authenticationViewModel.authenticateUser()

    fun subscribeUserUpdates() = authenticationViewModel.userUpdates.observe(this) {
        onSubscribeUserUpdates(it)
    }

    fun updateUser(user: User) = authenticationViewModel.updateUser(user)

    fun setUserCoinsLeft(coinsLeft: Int) = authenticationViewModel.setUserCoinsLeft(coinsLeft)

    fun updateUserCoinsBalance() = authenticationViewModel.updateLocalUserCoinsBalance()

    fun setIsUserPremium(isUserPremium: Boolean) = authenticationViewModel.setIsUserPremium(isUserPremium)

    fun subscribeIsValidationEmailSent() = authenticationViewModel.isValidationEmailSent.observe(this) {
        onIsValidationEmailSent(it)
    }

    fun sendVerificationEmail() = authenticationViewModel.sendVerificationEmail()

    fun propagateVerifiedStatus() = authenticationViewModel.propagateVerifiedStatus()

    fun getDevice(): Device? = authenticationViewModel.device.value

    fun subscribeDevice() = authenticationViewModel.device.observe(this) {
        onSubscribeDevice(it)
    }

    fun subscribeDeviceRegistration() = authenticationViewModel.deviceRegistration.observe(this) {
        onSubscribeDeviceRegistration(it)
    }

    fun registerDevice(device: Device) =
        authenticationViewModel.registerDevice(device.copy(
            hardwareFingerprint = hardwareFingerprint
        ))

    fun subscribeDeviceUpdates() = authenticationViewModel.deviceUpdates.observe(this) {
        onSubscribeDeviceUpdates(it)
    }

    fun updateDevice(device: Device) =
        authenticationViewModel.updateDevice(device.copy(
            hardwareFingerprint = hardwareFingerprint
        ))

    fun saveDeviceMessagingToken(messagingToken: String) =
        authenticationViewModel.saveDeviceMessagingToken(messagingToken)

    fun getLatestStoragedDeviceMessagingToken(): String? =
        authenticationViewModel.getLatestStoragedDeviceMessagingToken()

    fun signOut(propagateSignOutStateToViewModel: Boolean = true) = authenticationViewModel.signOut(propagateSignOutStateToViewModel)
}
