package com.pbaltazar.blindo.utils.authentication.ui

import android.annotation.SuppressLint
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

@Suppress("unused")
class AuthenticationViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val authenticationLocal : AuthenticationLocal,
    private val authenticationProvider : AuthenticationProvider,
    private val queryAuthenticateUser: QueryAuthenticateUser,
    private val mutationCreateUser: MutationCreateUser,
    private val mutationUpdateUser: MutationUpdateUser,
    private val queryGetDevice: QueryGetDevice,
    private val mutationCreateDevice: MutationCreateDevice,
    private val mutationUpdateDevice: MutationUpdateDevice,
    private val queryGetUserCoinsBalance: QueryGetUserCoinsBalance
) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> get() = _user

    private val _authentication = MutableLiveData<UserAuthentication>()
    val authentication: LiveData<UserAuthentication> get() = _authentication

    private val _userUpdates = MutableLiveData<UserUpdate>()
    val userUpdates: LiveData<UserUpdate> get() = _userUpdates

    private val _idToken = MutableLiveData<AuthenticationProviderResponse<String>>()
    val idToken: LiveData<AuthenticationProviderResponse<String>> get() = _idToken

    private val _isAccountVerified = MutableLiveData<Boolean>()
    val isAccountVerified: LiveData<Boolean> get() = _isAccountVerified

    private val _isValidationEmailSent = MutableLiveData<Boolean>()
    val isValidationEmailSent: LiveData<Boolean> get() = _isValidationEmailSent

    private val _device = MutableLiveData<Device?>()
    val device: LiveData<Device?> get() = _device

    private val _deviceRegistration = MutableLiveData<DeviceRegistration>()
    val deviceRegistration: LiveData<DeviceRegistration> get() = _deviceRegistration

    private val _deviceUpdates = MutableLiveData<DeviceUpdate>()
    val deviceUpdates: LiveData<DeviceUpdate> get() = _deviceUpdates

    fun refreshIdToken() = viewModelScope.launch(backgroundDispatcher) {
        _idToken.postValue(authenticationProvider.getIdToken())
    }

    fun setUser(user: User?) = _user.postValue(user)

    fun authenticateUser() = viewModelScope.launch(backgroundDispatcher) {
        authenticationProvider.getUser()?.also { providerAccount ->
            when (val tokenResponse = authenticationProvider.getIdToken()) {
                is AuthenticationProviderResponse.Success -> authenticateUserOnServer(providerAccount, tokenResponse.data)
                is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                    is AuthenticationProviderException.Empty -> _authentication.postValue(UserAuthentication.InvalidIdToken)
                    is AuthenticationProviderException.NotSignedIn -> signOut()
                    is AuthenticationProviderException.UnknownError -> _authentication.postValue(UserAuthentication.UnknownError)
                    is AuthenticationProviderException.Error -> _authentication.postValue(
                        UserAuthentication.NetworkError(
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
                is ApiException.WithErrors -> _authentication.postValue(UserAuthentication.BadRequest(apiError.errorsList))
                is ApiException.CallFailure -> _authentication.postValue(UserAuthentication.NetworkError(apiError.error))
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
                is ApiException.EmptyResponse -> _authentication.postValue(UserAuthentication.NoUserFound)
                is ApiException.WithErrors -> _authentication.postValue(UserAuthentication.BadRequest(apiError.errorsList))
                is ApiException.CallFailure -> _authentication.postValue(UserAuthentication.NetworkError(apiError.error))
            }
        }
    }

    fun updateUser(user: User) = viewModelScope.launch(backgroundDispatcher) {
        authenticationLocal.getLocalAccount()?.also { localAccount ->
            if ((localAccount == user).not()) {
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
                            is AuthenticationProviderException.Empty -> _userUpdates.postValue(UserUpdate.InvalidIdToken)
                            is AuthenticationProviderException.NotSignedIn -> {
                                _userUpdates.postValue(UserUpdate.NotSignedIn)
                                signOut(false)
                            }
                            is AuthenticationProviderException.UnknownError -> _userUpdates.postValue(UserUpdate.UnknownError)
                            is AuthenticationProviderException.Error -> _userUpdates.postValue(
                                UserUpdate.NetworkError(
                                    tokenError.error
                                )
                            )
                        }
                    }
                } ?: run {
                    _userUpdates.postValue(UserUpdate.NotSignedIn)
                    signOut(false)
                }
            } else {
                _userUpdates.postValue(UserUpdate.Success(user))
            }
        } ?: run {
            _userUpdates.postValue(UserUpdate.NotSignedIn)
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
                _userUpdates.postValue(UserUpdate.Success(user))
            }
            is ApiResponse.Error -> when (val apiError = apiResponse.error) {
                is ApiException.EmptyResponse -> _userUpdates.postValue(UserUpdate.NoUserFound)
                is ApiException.WithErrors -> _userUpdates.postValue(UserUpdate.BadRequest(apiError.errorsList))
                is ApiException.CallFailure -> _userUpdates.postValue(UserUpdate.NetworkError(apiError.error))
            }
        }
    }

    @SuppressLint("NullSafeMutableLiveData")
    fun sendVerificationEmail() = viewModelScope.launch(backgroundDispatcher) {
        when (val response = authenticationProvider.sendVerificationEmail()) {
            is AuthenticationProviderResponse.Success -> _isValidationEmailSent.postValue(response.data)
            is AuthenticationProviderResponse.Error -> _isValidationEmailSent.postValue(false)
        }
    }

    fun propagateVerifiedStatus() = viewModelScope.launch(backgroundDispatcher) {
        authenticationLocal.setLocalAccountIsVerified(
            authenticationProvider.getUser()?.isVerified ?: false
        )?.also { refreshedUser ->
            _isAccountVerified.postValue(refreshedUser.isVerified)
            setUser(refreshedUser)
        }
    }

    private fun setDevice(device: Device?) = _device.postValue(device)

    fun registerDevice(device: Device) = viewModelScope.launch(backgroundDispatcher) {
        authenticationProvider.getUser()?.also {
            when (val tokenResponse = authenticationProvider.getIdToken()) {
                is AuthenticationProviderResponse.Success -> getDeviceFromServer(device, tokenResponse.data)
                is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                    is AuthenticationProviderException.Empty -> {
                        _deviceRegistration.postValue(DeviceRegistration.NotAuthenticated)
                        cleanLocalUserData()
                    }
                    is AuthenticationProviderException.NotSignedIn -> {
                        _deviceRegistration.postValue(DeviceRegistration.NotAuthenticated)
                        cleanLocalUserData()
                    }
                    is AuthenticationProviderException.UnknownError -> {
                        _deviceRegistration.postValue(DeviceRegistration.NotAuthenticated)
                        cleanLocalUserData()
                    }
                    is AuthenticationProviderException.Error -> {
                        _deviceRegistration.postValue(DeviceRegistration.NetworkError(tokenError.error))
                        cleanLocalUserData()
                    }
                }
            }
        } ?: run {
            _deviceRegistration.postValue(DeviceRegistration.NotAuthenticated)
            signOut(false)
        }
    }

    private fun getDeviceFromServer(device: Device, idToken: String) = viewModelScope.launch(backgroundDispatcher) {
        when (val apiResponse = queryGetDevice(device.hardwareFingerprint, idToken)) {
            is ApiResponse.Success -> apiResponse.data.also { device ->
                setDevice(device)
                _deviceRegistration.postValue(DeviceRegistration.Success(device))
            }
            is ApiResponse.Error -> when (val  apiError = apiResponse.error) {
                is ApiException.EmptyResponse -> registerDeviceOnServer(device, idToken)
                is ApiException.WithErrors -> {
                    _deviceRegistration.postValue(DeviceRegistration.BadRequest(apiError.errorsList))
                    cleanLocalUserData()
                }
                is ApiException.CallFailure -> {
                    _deviceRegistration.postValue(DeviceRegistration.NetworkError(apiError.error))
                    cleanLocalUserData()
                }
            }
        }
    }

    private fun registerDeviceOnServer(device: Device, idToken: String) = viewModelScope.launch(backgroundDispatcher) {
        when (val apiResponse = mutationCreateDevice(device, idToken)) {
            is ApiResponse.Success -> apiResponse.data.also { device ->
                _deviceRegistration.postValue(DeviceRegistration.Success(device))
                setDevice(device)
            }
            is ApiResponse.Error -> when (val apiError = apiResponse.error) {
                is ApiException.EmptyResponse -> {
                    _deviceRegistration.postValue(DeviceRegistration.NotAuthenticated)
                    cleanLocalUserData()
                }
                is ApiException.WithErrors -> {
                    _deviceRegistration.postValue(DeviceRegistration.BadRequest(apiError.errorsList))
                    cleanLocalUserData()
                }
                is ApiException.CallFailure -> {
                    _deviceRegistration.postValue(DeviceRegistration.NetworkError(apiError.error))
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
                    is AuthenticationProviderException.NotSignedIn -> _deviceUpdates.postValue(DeviceUpdate.NotSignedIn)
                    is AuthenticationProviderException.Error -> _deviceUpdates.postValue(DeviceUpdate.NetworkError(tokenError.error))
                    else -> _deviceUpdates.postValue(DeviceUpdate.InvalidIdToken)
                }
            }
        } ?: _deviceUpdates.postValue(DeviceUpdate.NotSignedIn)
    }

    private fun updateDeviceOnServer(device: Device, idToken: String) = viewModelScope.launch(backgroundDispatcher) {
        when (val apiResponse = mutationUpdateDevice(device, idToken)) {
            is ApiResponse.Success -> apiResponse.data.also { device ->
                _deviceUpdates.postValue(DeviceUpdate.Success(device))
                setDevice(device)
            }
            is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                is ApiException.EmptyResponse -> _deviceUpdates.postValue(DeviceUpdate.Empty)
                is ApiException.WithErrors -> _deviceUpdates.postValue(DeviceUpdate.BadRequest(apiException.errorsList))
                is ApiException.CallFailure -> _deviceUpdates.postValue(DeviceUpdate.NetworkError(apiException.error))
            }
        }
    }

    fun updateLocalUserCoinsBalance() = viewModelScope.launch(backgroundDispatcher) {
        authenticationProvider.getUser()?.also {
            when (val tokenResponse = authenticationProvider.getIdToken()) {
                is AuthenticationProviderResponse.Success -> when (val apiResponse = queryGetUserCoinsBalance(
                    UserInput(
                        id = "",
                        idToken = tokenResponse.data
                    )
                )) {
                    is ApiResponse.Success -> setUserCoinsLeft(apiResponse.data)
                    is ApiResponse.Error -> Unit
                }
                is AuthenticationProviderResponse.Error -> Unit
            }
        }
    }

    private fun signIn(user: User) {
        authenticationLocal.getLocalAccount()?.also { localUser ->
            if ((localUser == user).not()) {
                authenticationLocal.updateLocalAccount(user)
            }
        } ?: run {
            authenticationLocal.registerLocalAccount(user)
        }
        setUser(user)
        _authentication.postValue(UserAuthentication.Success(user))
        AnalyticsManager.registerLoginEvent(user.getAuthenticationMethod())
    }

    private fun updateLocalAccount(user: User) =
        setUser(authenticationLocal.updateLocalAccount(user) ?: user)

    fun setUserCoinsLeft(coinsLeft: Int) = setUser(authenticationLocal.setLocalAccountCoinsLeft(coinsLeft))

    fun setIsUserPremium(isUserPremium: Boolean) = setUser(authenticationLocal.setLocalAccountIsPremium(isUserPremium))

    fun signOut(propagateSignOutStateToViewModel: Boolean = true) {
        authenticationProvider.signOut()
        cleanLocalUserData()
        if (propagateSignOutStateToViewModel) {
            _authentication.postValue(UserAuthentication.NotSignedIn)
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
        class NetworkError(val throwable: Throwable) : UserAuthentication()
    }

    sealed class UserUpdate {
        class Success(val user: User) : UserUpdate()
        class BadRequest(val errors: List<String>) : UserUpdate()
        object NoUserFound : UserUpdate()
        object NotSignedIn : UserUpdate()
        object InvalidIdToken : UserUpdate()
        object UnknownError : UserUpdate()
        class NetworkError(val throwable: Throwable) : UserUpdate()
    }

    sealed class DeviceRegistration {
        class Success(val device: Device) : DeviceRegistration()
        class BadRequest(val errors: List<String>) : DeviceRegistration()
        object NotAuthenticated : DeviceRegistration()
        object InvalidIdToken : DeviceRegistration()
        class NetworkError(val throwable: Throwable) : DeviceRegistration()
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
