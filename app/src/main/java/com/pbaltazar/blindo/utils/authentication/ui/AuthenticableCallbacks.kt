package com.pbaltazar.blindo.utils.authentication.ui

interface AuthenticableCallbacks {
    fun onSubscribeUser(): Unit = Unit
    fun onSubscribeAuthentication(userAuthentication : AuthenticationViewModel.UserAuthentication): Unit = Unit
    fun onSubscribeUserUpdate(userUpdate: AuthenticationViewModel.UserUpdate): Unit = Unit
    fun onIsValidationEmailSent(isValidationEmailSent: Boolean): Unit = Unit
    fun onSubscribeDevice(): Unit = Unit
    fun onSubscribeDeviceAuthentication(deviceAuthentication: AuthenticationViewModel.DeviceAuthentication): Unit = Unit
    fun onSubscribeDeviceUpdate(deviceUpdate: AuthenticationViewModel.DeviceUpdate): Unit = Unit
}
