package com.pbaltazar.blindo.utils.billing.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pbaltazar.blindo.entities.BlindoPurchase
import com.pbaltazar.blindo.entities.Membership
import com.pbaltazar.blindo.entities.Sku
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.errors.AuthenticationProviderException
import com.pbaltazar.blindo.entities.errors.BillingException
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.entities.responses.AuthenticationProviderResponse
import com.pbaltazar.blindo.entities.responses.BillingResponse
import com.pbaltazar.blindo.usecases.MutationProcessMembership
import com.pbaltazar.blindo.usecases.QueryGetMembership
import com.pbaltazar.blindo.utils.authentication.provider.AuthenticationProvider
import com.pbaltazar.blindo.utils.billing.BillingManager
import com.pbaltazar.blindo.utils.constants.MONTHLY_SUBSCRIPTION_SKU
import com.pbaltazar.blindo.utils.extensions.isExpired
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class BillingViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val authenticationProvider: AuthenticationProvider,
    private val billingManager: BillingManager,
    private val queryGetMembership: QueryGetMembership,
    private val mutationProcessMembership: MutationProcessMembership
) : ViewModel() {

    private val connected = MutableLiveData<BillingConnection>()
    val isConnected: LiveData<BillingConnection> get() = connected

    private val availableSkus = MutableLiveData<AvailableSkus>()
    val skus: LiveData<AvailableSkus> get() = availableSkus

    private val currentMembership = MutableLiveData<ActiveMembership>()
    val membership: LiveData<ActiveMembership> get() = currentMembership

    init {
        if (billingManager.isConnected().not()) {
            startConnection()
        }
    }

    fun isServiceConnected(): Boolean = billingManager.isConnected()

    fun startConnection() = viewModelScope.launch(backgroundDispatcher) {
        when (val billingResponse = billingManager.startConnection()) {
            is BillingResponse.Success -> connected.postValue(BillingConnection.Connected)
            is BillingResponse.Error -> when (val billingException = billingResponse.error) {
                is BillingException.InstanceError -> connected.postValue(BillingConnection.BadRequest(billingException.error))
                is BillingException.FeatureNotSupported -> connected.postValue(BillingConnection.FeatureNotSupported)
                is BillingException.ServiceUnavailable -> connected.postValue(BillingConnection.ServiceUnavailable)
                is BillingException.UnknownError -> connected.postValue(BillingConnection.BadRequest(billingException.error))
                else -> connected.postValue(BillingConnection.UnknownError)
            }
        }
    }

    fun closeConnection() {
        connected.postValue(BillingConnection.Disconnected)
        billingManager.closeConnection()
    }

    fun getMembership(onlyFromPlayStore: Boolean = false) = viewModelScope.launch(backgroundDispatcher) {
        authenticationProvider.getUser()?.also {
            when (val tokenResponse = authenticationProvider.getIdToken()) {
                is AuthenticationProviderResponse.Success -> if (onlyFromPlayStore)
                    getMembershipFromPlayStore(tokenResponse.data)
                        else
                    getMembershipFromServer(tokenResponse.data)
                is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                    is AuthenticationProviderException.Error -> currentMembership.postValue(ActiveMembership.NetworkError(tokenError.error))
                    else -> currentMembership.postValue(ActiveMembership.InvalidIdToken)
                }
            }
        } ?: currentMembership.postValue(ActiveMembership.NotSignedIn)
    }

    private fun getMembershipFromServer(idToken: String) = viewModelScope.launch(backgroundDispatcher) {
        when (val apiResponse = queryGetMembership(idToken)) {
            is ApiResponse.Success -> apiResponse.data.also { membership ->
                if (membership.isExpired()) {
                    getMembershipFromPlayStore(idToken)
                } else {
                    currentMembership.postValue(ActiveMembership.Success(membership))
                }
            }
            is ApiResponse.Error -> when (val apiError = apiResponse.error) {
                is ApiException.EmptyResponse -> getMembershipFromPlayStore(idToken)
                is ApiException.WithErrors -> currentMembership.postValue(ActiveMembership.BadRequest(apiError.errorsList))
                is ApiException.CallFailure -> currentMembership.postValue(ActiveMembership.NetworkError(apiError.error))
            }
        }
    }

    private fun processMembershipOnServer(blindoPurchase: BlindoPurchase, idToken: String) = viewModelScope.launch(backgroundDispatcher) {
        when (val apiResponse = mutationProcessMembership(blindoPurchase, idToken)) {
            is ApiResponse.Success -> currentMembership.postValue(
                ActiveMembership.Success(apiResponse.data)
            )
            is ApiResponse.Error -> when (val apiError = apiResponse.error) {
                is ApiException.EmptyResponse -> currentMembership.postValue(ActiveMembership.UnknownError)
                is ApiException.WithErrors -> currentMembership.postValue(ActiveMembership.BadRequest(apiError.errorsList))
                is ApiException.CallFailure -> currentMembership.postValue(ActiveMembership.NetworkError(apiError.error))
            }
        }
    }

    fun getSkus() = viewModelScope.launch(backgroundDispatcher) {
        if (isServiceConnected()) {
            when (val billingResponse = billingManager.getSkus(
                listOf(
                    MONTHLY_SUBSCRIPTION_SKU
                )
            )) {
                is BillingResponse.Success -> {
                    val skusMap = mutableMapOf<String, Sku>()
                    billingResponse.data.forEach { sku ->
                        skusMap.put(sku.id, sku)
                    }
                    if (skusMap.size > 0) {
                        availableSkus.postValue(AvailableSkus.Success(skusMap))
                    } else {
                        availableSkus.postValue(AvailableSkus.Empty)
                    }
                }
                is BillingResponse.Error -> when (val billingException = billingResponse.error) {
                    is BillingException.EmptyResponse -> availableSkus.postValue(AvailableSkus.Empty)
                    is BillingException.FeatureNotSupported -> availableSkus.postValue(AvailableSkus.FeatureNotSupported)
                    is BillingException.ServiceUnavailable -> availableSkus.postValue(AvailableSkus.ServiceUnavailable)
                    is BillingException.InstanceError-> availableSkus.postValue(AvailableSkus.BadRequest(billingException.error))
                    is BillingException.UnknownError -> availableSkus.postValue(AvailableSkus.BadRequest(billingException.error))
                    else -> availableSkus.postValue(AvailableSkus.UnknownError)
                }
            }
        } else {
            availableSkus.postValue(AvailableSkus.Disconnected)
        }
    }

    fun launchPurchase(activity: AppCompatActivity, sku: Sku) = viewModelScope.launch(backgroundDispatcher) {
        if (isServiceConnected()) {
            authenticationProvider.getUser()?.also {
                when (val tokenResponse = authenticationProvider.getIdToken()) {
                    is AuthenticationProviderResponse.Success -> when (val billingResponse = billingManager.launchBilling(activity, sku)) {
                        is BillingResponse.Success -> billingResponse.data.firstOrNull()?.also { blindoPurchase ->
                            if (blindoPurchase.isAcknowledged.not()) {
                                acknowledgePurchase(blindoPurchase, tokenResponse.data)
                            } else {
                                processMembershipOnServer(blindoPurchase, tokenResponse.data)
                            }
                        } ?: currentMembership.postValue(ActiveMembership.NotPurchased)
                        is BillingResponse.Error -> when (val billingException = billingResponse.error) {
                            is BillingException.CanceledByUser -> currentMembership.postValue(ActiveMembership.CanceledByUser)
                            is BillingException.EmptyResponse -> currentMembership.postValue(ActiveMembership.NotPurchased)
                            is BillingException.FeatureNotSupported -> currentMembership.postValue(ActiveMembership.FeatureNotSupported)
                            is BillingException.ServiceUnavailable -> currentMembership.postValue(ActiveMembership.ServiceUnavailable)
                            is BillingException.InstanceError -> currentMembership.postValue(ActiveMembership.BadRequest(listOf(billingException.error)))
                            is BillingException.UnknownError -> currentMembership.postValue(ActiveMembership.BadRequest(
                                listOf(billingException.error)))
                            else -> currentMembership.postValue(ActiveMembership.UnknownError)
                        }
                    }
                    is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                        is AuthenticationProviderException.Error -> currentMembership.postValue(ActiveMembership.NetworkError(tokenError.error))
                        is AuthenticationProviderException.NotSignedIn -> currentMembership.postValue(ActiveMembership.NotSignedIn)
                        else -> currentMembership.postValue(ActiveMembership.InvalidIdToken)
                    }
                }
            } ?: currentMembership.postValue(ActiveMembership.NotSignedIn)
        } else {
            currentMembership.postValue(ActiveMembership.Disconnected)
        }
    }

    private fun getMembershipFromPlayStore(idToken: String) = viewModelScope.launch(backgroundDispatcher) {
        when (val billingResponse = billingManager.getBillings()) {
            is BillingResponse.Success -> billingResponse.data.firstOrNull()?.also { blindoPurchase ->
                if (blindoPurchase.isAcknowledged.not()) {
                    acknowledgePurchase(blindoPurchase, idToken)
                } else {
                    processMembershipOnServer(blindoPurchase, idToken)
                }
            } ?: currentMembership.postValue(ActiveMembership.NotPurchased)
            is BillingResponse.Error -> when (val billingException = billingResponse.error) {
                is BillingException.EmptyResponse -> currentMembership.postValue(ActiveMembership.NotPurchased)
                is BillingException.CanceledByUser -> currentMembership.postValue(ActiveMembership.CanceledByUser)
                is BillingException.InstanceError -> currentMembership.postValue(ActiveMembership.BadRequest(listOf(billingException.error)))
                is BillingException.ServiceUnavailable -> currentMembership.postValue(ActiveMembership.ServiceUnavailable)
                is BillingException.FeatureNotSupported -> currentMembership.postValue(ActiveMembership.FeatureNotSupported)
                is BillingException.UnknownError -> currentMembership.postValue(ActiveMembership.BadRequest(listOf(billingException.error)))
                else -> currentMembership.postValue(ActiveMembership.UnknownError)
            }
        }
    }

    fun acknowledgePurchase(blindoPurchase: BlindoPurchase, idToken: String) = viewModelScope.launch(backgroundDispatcher) {
        if (isServiceConnected()) {
            when (billingManager.acknowledgeBilling(blindoPurchase)) {
                is BillingResponse.Success -> processMembershipOnServer(blindoPurchase, idToken)
                is BillingResponse.Error -> currentMembership.postValue(ActiveMembership.PurchasedButNotAcknowledged(blindoPurchase))
            }
        } else {
            currentMembership.postValue(ActiveMembership.PurchasedButNotAcknowledged(blindoPurchase))
        }
    }

    sealed class BillingConnection {
        object Connected : BillingConnection()
        object Disconnected : BillingConnection()
        object FeatureNotSupported : BillingConnection()
        object ServiceUnavailable : BillingConnection()
        class BadRequest(val reason: String) : BillingConnection()
        object UnknownError : BillingConnection()
    }

    sealed class AvailableSkus {
        class Success(val skus: Map<String, Sku>): AvailableSkus()
        object Empty: AvailableSkus()
        object Disconnected : AvailableSkus()
        object FeatureNotSupported: AvailableSkus()
        object ServiceUnavailable: AvailableSkus()
        class BadRequest(val reason: String): AvailableSkus()
        object UnknownError : AvailableSkus()
    }

    sealed class ActiveMembership {
            class Success(val membership: Membership) : ActiveMembership()
        class PurchasedButNotAcknowledged(val blindoPurchase: BlindoPurchase) : ActiveMembership()
            object NotPurchased : ActiveMembership()
        object Disconnected : ActiveMembership()
            object CanceledByUser : ActiveMembership()
            object NotSignedIn : ActiveMembership()
            object InvalidIdToken : ActiveMembership()
            class BadRequest(val errors: List<String>) : ActiveMembership()
            object FeatureNotSupported : ActiveMembership()
            object ServiceUnavailable : ActiveMembership()
            object UnknownError : ActiveMembership()
            class NetworkError(val throwable: Throwable) : ActiveMembership()
        }
    }
