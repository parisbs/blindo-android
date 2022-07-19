package com.pbaltazar.blindo.utils.authentication.ui

import com.pbaltazar.blindo.entities.Device
import com.pbaltazar.blindo.entities.User

interface AuthenticableCallbacks {
    fun onSubscribeUser(user: User?): Unit = Unit
    fun onSubscribeAuthentication(userAuthentication : AuthenticationViewModel.UserAuthentication): Unit = Unit
    fun onSubscribeUserUpdates(userUpdate: AuthenticationViewModel.UserUpdate): Unit = Unit
    fun onIsValidationEmailSent(isValidationEmailSent: Boolean): Unit = Unit
    fun onSubscribeDevice(device: Device?): Unit = Unit
    fun onSubscribeDeviceRegistration(deviceRegistration: AuthenticationViewModel.DeviceRegistration): Unit = Unit
    fun onSubscribeDeviceUpdates(deviceUpdate: AuthenticationViewModel.DeviceUpdate): Unit = Unit
}
