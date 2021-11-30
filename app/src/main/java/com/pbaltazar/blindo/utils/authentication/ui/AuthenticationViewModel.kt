package com.pbaltazar.blindo.utils.authentication.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.entities.Device
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.errors.AuthenticationProviderException
import com.pbaltazar.blindo.entities.inputs.UserInput
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.entities.responses.AuthenticationProviderResponse
import com.pbaltazar.blindo.usecases.*
import com.pbaltazar.blindo.utils.analytics.AnalyticsManager
import com.pbaltazar.blindo.utils.authentication.local.AuthenticationLocal
import com.pbaltazar.blindo.utils.authentication.provider.AuthenticationProvider
import com.pbaltazar.blindo.utils.extensions.getAuthenticationMethod
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AuthenticationViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val authenticationLocal : AuthenticationLocal,
    private val authenticationProvider : AuthenticationProvider,
    private val queryAuthenticateUser: QueryAuthenticateUser,
    private val mutationCreateUser: MutationCreateUser,
    private val mutationUpdateUser: MutationUpdateUser,
    private val queryGetDevice: QueryGetDevice,
    private val mutationCreateDevice: MutationCreateDevice,
    private val mutationUpdateDevice: MutationUpdateDevice
) : ViewModel() {

    private val currentUser = MutableLiveData<User?>()
    val user: LiveData<User?> get() = currentUser

    private val authenticationResult = MutableLiveData<UserAuthentication>()
    val authentication: LiveData<UserAuthentication> get() = authenticationResult

    private val updateResult = MutableLiveData<UserUpdate>()
    val userUpdate: LiveData<UserUpdate> get() = updateResult

    private val idToken = MutableLiveData<AuthenticationProviderResponse<String>>()
    val refreshedIdToken: LiveData<AuthenticationProviderResponse<String>> get() = idToken

    private val verifiedStatus = MutableLiveData<Boolean>()
    val isAccountVerified: LiveData<Boolean> get() = verifiedStatus

    private val sendResult = MutableLiveData<Boolean>()
    val isValidationEmailSent: LiveData<Boolean> get() = sendResult

    private val currentDevice = MutableLiveData<Device?>()
    val device: LiveData<Device?> get() = currentDevice

    private val deviceAuthenticationResult = MutableLiveData<DeviceAuthentication>()
    val deviceAuthentication: LiveData<DeviceAuthentication> get() = deviceAuthenticationResult

    private val deviceUpdateResult = MutableLiveData<DeviceUpdate>()
    val deviceUpdate: LiveData<DeviceUpdate> get() = deviceUpdateResult

    fun getRefreshedIdToken() = viewModelScope.launch(backgroundDispatcher) {
        idToken.postValue(authenticationProvider.getIdToken())
    }

    fun setUser(user: User?) = currentUser.postValue(user)

    fun authenticateUser() = viewModelScope.launch(backgroundDispatcher) {
        authenticationProvider.getUser()?.also { providerAccount ->
            when (val tokenResponse = authenticationProvider.getIdToken()) {
                is AuthenticationProviderResponse.Success -> authenticateUserOnServer(providerAccount, tokenResponse.data)
                is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                    is AuthenticationProviderException.Empty -> authenticationResult.postValue(UserAuthentication.InvalidIdToken)
                    is AuthenticationProviderException.NotSignedIn -> signOut()
                    is AuthenticationProviderException.UnknownError -> authenticationResult.postValue(UserAuthentication.UnknownError)
                    is AuthenticationProviderException.Error -> authenticationResult.postValue(
                        UserAuthentication.NetworkEror(
                            tokenError.error
                        )
                    )
                }
            }
        } ?: run {
            signOut()
        }
    }

    private fun authenticateUserOnServer(user: User, idToken: String) = viewModelScope.launch(backgroundDispatcher) {
        when (val apiResponse = queryAuthenticateUser(UserInput("", idToken))) {
            is ApiResponse.Success -> signIn(
                apiResponse.data.copy(
                    sub = user.sub,
                    email = user.email,
                    isVerified = user.isVerified
                )
            )
            is ApiResponse.Error -> when (val apiError = apiResponse.error) {
                is ApiException.EmptyResponse -> registerUser(user, idToken)
                is ApiException.WithErrors -> authenticationResult.postValue(UserAuthentication.BadRequest(apiError.errorsList))
                is ApiException.CallFailure -> authenticationResult.postValue(UserAuthentication.NetworkEror(apiError.error))
            }
        }
    }

    private fun registerUser(user: User, idToken: String) = viewModelScope.launch(backgroundDispatcher) {
        when (val apiResponse = mutationCreateUser(user, idToken)) {
            is ApiResponse.Success -> signIn(
                apiResponse.data.copy(
                    sub = user.sub,
                    email = user.email,
                    isVerified = user.isVerified
                )
            )
            is ApiResponse.Error -> when (val apiError = apiResponse.error) {
                is ApiException.EmptyResponse -> authenticationResult.postValue(UserAuthentication.NoUserFound)
                is ApiException.WithErrors -> authenticationResult.postValue(UserAuthentication.BadRequest(apiError.errorsList))
                is ApiException.CallFailure -> authenticationResult.postValue(UserAuthentication.NetworkEror(apiError.error))
            }
        }
    }

    fun updateUser(user: User) = viewModelScope.launch(backgroundDispatcher) {
        authenticationLocal.getLocalAccount()?.also { localAccount ->
            if (localAccount.equals(user).not()) {
                authenticationProvider.getUser()?.also { providerAccount ->
                    when (val tokenResponse = authenticationProvider.getIdToken()) {
                        is AuthenticationProviderResponse.Success -> updateUserOnServer(
                            user.copy(
                                sub = providerAccount.sub,
                                email = providerAccount.email,
                                isVerified = providerAccount.isVerified
                            ),
                            tokenResponse.data
                        )
                        is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                            is AuthenticationProviderException.Empty -> updateResult.postValue(UserUpdate.InvalidIdToken)
                            is AuthenticationProviderException.NotSignedIn -> {
                                updateResult.postValue(UserUpdate.NotSignedIn)
                                signOut(false)
                            }
                            is AuthenticationProviderException.UnknownError -> updateResult.postValue(UserUpdate.UnknownError)
                            is AuthenticationProviderException.Error -> updateResult.postValue(
                                UserUpdate.NetworkEror(
                                    tokenError.error
                                )
                            )
                        }
                    }
                } ?: run {
                    updateResult.postValue(UserUpdate.NotSignedIn)
                    signOut(false)
                }
            } else {
                updateResult.postValue(UserUpdate.Success(user))
            }
        } ?: run {
            updateResult.postValue(UserUpdate.NotSignedIn)
            signOut(false)
        }
    }

    private fun updateUserOnServer(user: User, idToken: String) = viewModelScope.launch(backgroundDispatcher) {
        when (val apiResponse = mutationUpdateUser(user, idToken)) {
            is ApiResponse.Success -> apiResponse.data.also { serverUser ->
                updateLocalAccount(serverUser.copy(
                    sub = user.sub,
                    email = user.email,
                    isVerified = user.isVerified
                ))
                updateResult.postValue(UserUpdate.Success(user))
            }
            is ApiResponse.Error -> when (val apiError = apiResponse.error) {
                is ApiException.EmptyResponse -> updateResult.postValue(UserUpdate.NoUserFound)
                is ApiException.WithErrors -> updateResult.postValue(UserUpdate.BadRequest(apiError.errorsList))
                is ApiException.CallFailure -> updateResult.postValue(UserUpdate.NetworkEror(apiError.error))
            }
        }
    }

    fun sendVerificationEmail() = viewModelScope.launch(backgroundDispatcher) {
        when (val response = authenticationProvider.sendVerificationEmail()) {
            is AuthenticationProviderResponse.Success -> sendResult.postValue(response.data)
            is AuthenticationProviderResponse.Error -> sendResult.postValue(false)
        }
    }

    fun propagateVerifiedStatus() = viewModelScope.launch(backgroundDispatcher) {
        authenticationLocal.setLocalAccountIsVerified(
            authenticationProvider.getUser()?.isVerified ?: false
        )?.also { refreshedUser ->
            verifiedStatus.postValue(refreshedUser.isVerified)
            setUser(refreshedUser)
        }
    }

    fun setDevice(device: Device?) = currentDevice.postValue(device)

    fun authenticateDevice(device: Device) = viewModelScope.launch(backgroundDispatcher) {
        authenticationProvider.getUser()?.also {
            when (val tokenResponse = authenticationProvider.getIdToken()) {
                is AuthenticationProviderResponse.Success -> getDeviceFromServer(device, tokenResponse.data)
                is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                    is AuthenticationProviderException.Empty -> {
                        deviceAuthenticationResult.postValue(DeviceAuthentication.NotAuthenticated)
                        cleanLocalUserData()
                    }
                    is AuthenticationProviderException.NotSignedIn -> {
                        deviceAuthenticationResult.postValue(DeviceAuthentication.NotAuthenticated)
                        cleanLocalUserData()
                    }
                    is AuthenticationProviderException.UnknownError -> {
                        deviceAuthenticationResult.postValue(DeviceAuthentication.NotAuthenticated)
                        cleanLocalUserData()
                    }
                    is AuthenticationProviderException.Error -> {
                        deviceAuthenticationResult.postValue(DeviceAuthentication.NetworkError(tokenError.error))
                        cleanLocalUserData()
                    }
                }
            }
        } ?: run {
            deviceAuthenticationResult.postValue(DeviceAuthentication.NotAuthenticated)
            signOut(false)
        }
    }

    private fun getDeviceFromServer(device: Device, idToken: String) = viewModelScope.launch(backgroundDispatcher) {
        when (val apiResponse = queryGetDevice(device.hardwareFingerprint, idToken)) {
            is ApiResponse.Success -> apiResponse.data.also { device ->
                setDevice(device)
                deviceAuthenticationResult.postValue(DeviceAuthentication.Success(device))
            }
            is ApiResponse.Error -> when (val  apiError = apiResponse.error) {
                is ApiException.EmptyResponse -> registerDeviceOnServer(device, idToken)
                is ApiException.WithErrors -> {
                    deviceAuthenticationResult.postValue(DeviceAuthentication.BadRequest(apiError.errorsList))
                    cleanLocalUserData()
                }
                is ApiException.CallFailure -> {
                    deviceAuthenticationResult.postValue(DeviceAuthentication.NetworkError(apiError.error))
                    cleanLocalUserData()
                }
            }
        }
    }

    private fun registerDeviceOnServer(device: Device, idToken: String) = viewModelScope.launch(backgroundDispatcher) {
        when (val apiResponse = mutationCreateDevice(device, idToken)) {
            is ApiResponse.Success -> apiResponse.data.also { device ->
                deviceAuthenticationResult.postValue(DeviceAuthentication.Success(device))
                setDevice(device)
            }
            is ApiResponse.Error -> when (val apiError = apiResponse.error) {
                is ApiException.EmptyResponse -> {
                    deviceAuthenticationResult.postValue(DeviceAuthentication.NotAuthenticated)
                    cleanLocalUserData()
                }
                is ApiException.WithErrors -> {
                    deviceAuthenticationResult.postValue(DeviceAuthentication.BadRequest(apiError.errorsList))
                    cleanLocalUserData()
                }
                is ApiException.CallFailure -> {
                    deviceAuthenticationResult.postValue(DeviceAuthentication.NetworkError(apiError.error))
                    cleanLocalUserData()
                }
            }
        }
    }

    fun updateDevice(device: Device) = viewModelScope.launch(backgroundDispatcher) {
        authenticationProvider.getUser()?.also {
            when (val tokenResponse = authenticationProvider.getIdToken()) {
                is AuthenticationProviderResponse.Success -> updateDeviceOnServer(device, tokenResponse.data)
                is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                    is AuthenticationProviderException.NotSignedIn -> deviceUpdateResult.postValue(DeviceUpdate.NotSignedIn)
                    is AuthenticationProviderException.Error -> deviceUpdateResult.postValue(DeviceUpdate.NetworkError(tokenError.error))
                    else -> deviceUpdateResult.postValue(DeviceUpdate.InvalidIdToken)
                }
            }
        } ?: deviceUpdateResult.postValue(DeviceUpdate.NotSignedIn)
    }

    private fun updateDeviceOnServer(device: Device, idToken: String) = viewModelScope.launch(backgroundDispatcher) {
        when (val apiResponse = mutationUpdateDevice(device, idToken)) {
            is ApiResponse.Success -> apiResponse.data.also { device ->
                deviceUpdateResult.postValue(DeviceUpdate.Success(device))
                setDevice(device)
            }
            is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                is ApiException.EmptyResponse -> deviceUpdateResult.postValue(DeviceUpdate.Empty)
                is ApiException.WithErrors -> deviceUpdateResult.postValue(DeviceUpdate.BadRequest(apiException.errorsList))
                is ApiException.CallFailure -> deviceUpdateResult.postValue(DeviceUpdate.NetworkError(apiException.error))
            }
        }
    }

    private fun signIn(user: User) {
        authenticationLocal.getLocalAccount()?.also { localUser ->
            if (localUser.equals(user).not()) {
                authenticationLocal.updateLocalAccount(user)
            }
        } ?: run {
            authenticationLocal.registerLocalAccount(user)
        }
        setUser(user)
        authenticationResult.postValue(UserAuthentication.Success(user))
        AnalyticsManager.registerLoginEvent(user.getAuthenticationMethod())
    }

    private fun updateLocalAccount(user: User) =
        setUser(authenticationLocal.updateLocalAccount(user) ?: user)

    fun setIsUserPremium(isUserPremium: Boolean) = setUser(authenticationLocal.setLocalAccountIsPremium(isUserPremium))

    fun signOut(propagateSignOutStateToViewModel: Boolean = true) {
        authenticationProvider.signOut()
        cleanLocalUserData()
        if (propagateSignOutStateToViewModel) {
            authenticationResult.postValue(UserAuthentication.NotSignedIn)
        }
    }

    private fun cleanLocalUserData() {
        authenticationLocal.unregisterLocalAccount()
        setUser(null)
    }

    sealed class UserAuthentication {
        class Success(val user: User) : UserAuthentication()
        class BadRequest(val errors: List<String>) : UserAuthentication()
        object NoUserFound : UserAuthentication()
        object NotSignedIn : UserAuthentication()
        object InvalidIdToken : UserAuthentication()
        object UnknownError : UserAuthentication()
        class NetworkEror(val throwable: Throwable) : UserAuthentication()
    }

    sealed class UserUpdate {
        class Success(val user: User) : UserUpdate()
        class BadRequest(val errors: List<String>) : UserUpdate()
        object NoUserFound : UserUpdate()
        object NotSignedIn : UserUpdate()
        object InvalidIdToken : UserUpdate()
        object UnknownError : UserUpdate()
        class NetworkEror(val throwable: Throwable) : UserUpdate()
    }

    sealed class DeviceAuthentication {
        class Success(val device: Device) : DeviceAuthentication()
        class BadRequest(val errors: List<String>) : DeviceAuthentication()
        object NotAuthenticated : DeviceAuthentication()
        object InvalidIdToken : DeviceAuthentication()
        class NetworkError(val throwable: Throwable) : DeviceAuthentication()
    }

    sealed class DeviceUpdate {
        class Success(val device: Device) : DeviceUpdate()
        object Empty : DeviceUpdate()
        class BadRequest(val errors: List<String>) : DeviceUpdate()
        object NotSignedIn : DeviceUpdate()
        object InvalidIdToken : DeviceUpdate()
        class NetworkError(val throwable: Throwable) : DeviceUpdate()
    }
}
