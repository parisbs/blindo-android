package com.pbaltazar.blindo.utils.billing.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.pbaltazar.blindo.entities.Coin
import com.pbaltazar.blindo.entities.Membership
import com.pbaltazar.blindo.entities.errors.ApiException
import com.pbaltazar.blindo.entities.errors.AuthenticationProviderException
import com.pbaltazar.blindo.entities.errors.BillingException
import com.pbaltazar.blindo.entities.filters.sorts.CoinSort
import com.pbaltazar.blindo.entities.inputs.ListCoinsInput
import com.pbaltazar.blindo.entities.inputs.ProcessPurchaseInput
import com.pbaltazar.blindo.entities.purchases.Product
import com.pbaltazar.blindo.entities.purchases.Purchase
import com.pbaltazar.blindo.entities.purchases.enums.ProductType
import com.pbaltazar.blindo.entities.purchases.inapp.InApp
import com.pbaltazar.blindo.entities.purchases.subscriptions.Subscription
import com.pbaltazar.blindo.entities.responses.ApiResponse
import com.pbaltazar.blindo.entities.responses.AuthenticationProviderResponse
import com.pbaltazar.blindo.entities.responses.BillingResponse
import com.pbaltazar.blindo.usecases.MutationProcessPurchase
import com.pbaltazar.blindo.usecases.QueryGetMembership
import com.pbaltazar.blindo.usecases.QueryListCoins
import com.pbaltazar.blindo.utils.authentication.provider.AuthenticationProvider
import com.pbaltazar.blindo.utils.billing.BillingManager
import com.pbaltazar.blindo.utils.preferences.UserPreferences
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@Suppress("unused")
class BillingViewModel(
    private val backgroundDispatcher: CoroutineContext,
    private val userPreferences: UserPreferences,
    private val authenticationProvider: AuthenticationProvider,
    private val billingManager: BillingManager,
    private val queryListCoins: QueryListCoins,
    private val queryGetMembership: QueryGetMembership,
    private val mutationProcessPurchase: MutationProcessPurchase
) : ViewModel() {

    val isConnected: LiveData<BillingConnection> = billingManager.connectionStateFlow().map {
        when (val billingResponse = it) {
            is BillingResponse.Success -> BillingConnection.Connected
            is BillingResponse.Error -> when (val billingException = billingResponse.error) {
                is BillingException.InstanceError -> BillingConnection.Error(billingException.error)
                is BillingException.FeatureNotSupported -> BillingConnection.FeatureNotSupported
                is BillingException.ServiceUnavailable -> BillingConnection.ServiceUnavailable
                is BillingException.UnknownError -> BillingConnection.Error(billingException.error)
                else -> BillingConnection.Disconnected
            }
        }
    }.asLiveData()

    private val _inApps = MutableLiveData<AvailableProducts>()
    val inApps: LiveData<AvailableProducts> get() = _inApps

    private val _subscriptions = MutableLiveData<AvailableProducts>()
    val subscriptions: LiveData<AvailableProducts> get() = _subscriptions

    val purchases: LiveData<Purchases> = billingManager.purchasesFlow().map { purchases ->
        when (purchases) {
            is BillingResponse.Success -> Purchases.Success(
                purchases.data.filter { purchase ->
                    inAppsToPurchase.map { it.id }.contains(purchase.productId) ||
                        subscriptionsToPurchase.map { it.id }.contains(purchase.productId)
                }
            )
            is BillingResponse.Error -> when (val billingException= purchases.error) {
                is BillingException.EmptyResponse -> Purchases.Empty
                is BillingException.CanceledByUser -> Purchases.CanceledByUser
                is BillingException.InstanceError -> Purchases.Error(billingException.error)
                is BillingException.FeatureNotSupported -> Purchases.FeatureNotSupported
                is BillingException.ServiceUnavailable -> Purchases.ServiceUnavailable
                is BillingException.Disconnected -> Purchases.Disconnected
                is BillingException.UnknownError -> Purchases.Error(billingException.error)
            }
        }
    }.asLiveData()

    val consumption: LiveData<Consumption> = billingManager.consumptionFlow().map {
        when (val billingResponse = it) {
            is BillingResponse.Success -> Consumption.Success
            is BillingResponse.Error -> when (val billingException = billingResponse.error) {
                is BillingException.FeatureNotSupported -> Consumption.FeatureNotSupported
                is BillingException.InstanceError -> Consumption.Error(billingException.error)
                is BillingException.ServiceUnavailable -> Consumption.ServiceUnavailable
                is BillingException.Disconnected -> Consumption.Disconnected
                is BillingException.UnknownError -> Consumption.Error(billingException.error)
                else -> Consumption.Disconnected
            }
        }
    }.asLiveData()

    private val _coins = MutableLiveData<PurchasedCoin>()
    val coins: LiveData<PurchasedCoin> get() = _coins

    private val _coinsHistory = MutableLiveData<CoinsHistory>()
    val coinsHistory: LiveData<CoinsHistory> get() = _coinsHistory

    private val _membership = MutableLiveData<PurchasedMembership>()
    val membership: LiveData<PurchasedMembership> get() = _membership

    private val inAppsToPurchase: List<InApp> = listOf(
        InApp(
            id = "blindo_coins_100"
        ),
        InApp(
            id = "blindo_coins_500"
        )
    )

    private val subscriptionsToPurchase: List<Subscription> = listOf(
        Subscription(
            id = "blindo_membership"
        )
    )

    fun isServiceConnected(): Boolean = billingManager.isConnected()

    fun startConnection() = billingManager.startConnection()

    fun closeConnection() = billingManager.closeConnection()

    private fun getListCoinsInput(idToken: String): ListCoinsInput = ListCoinsInput(
        sort = listOf(CoinSort.CREATED_AT_DESC),
        first = 50,
        idToken = idToken
    )

    fun getCoinsHistory() = viewModelScope.launch(backgroundDispatcher) {
        authenticationProvider.getUser()?.also {
            when (val tokenResponse = authenticationProvider.getIdToken()) {
                is AuthenticationProviderResponse.Success -> when (val apiResponse = queryListCoins(getListCoinsInput(tokenResponse.data))) {
                    is ApiResponse.Success -> apiResponse.data.also { coins ->
                        _coinsHistory.postValue(CoinsHistory.Success(coins))
                    }
                    is ApiResponse.Error -> when (val apiException = apiResponse.error) {
                        is ApiException.EmptyResponse -> _coinsHistory.postValue(CoinsHistory.Empty)
                        is ApiException.WithErrors -> _coinsHistory.postValue(CoinsHistory.Error(apiException.errorsList.joinToString("\n")))
                        is ApiException.CallFailure -> _coinsHistory.postValue(CoinsHistory.Error(apiException.error.localizedMessage ?: apiException.error.toString()))
                    }
                }
                is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                    is AuthenticationProviderException.Error -> _coinsHistory.postValue(CoinsHistory.Error(tokenError.error.localizedMessage ?: tokenError.error.toString()))
                    is AuthenticationProviderException.NotSignedIn -> _coinsHistory.postValue(CoinsHistory.NotSignedIn)
                    else -> _coinsHistory.postValue(CoinsHistory.InvalidIdToken)
                }
            }
        } ?: _coinsHistory.postValue(CoinsHistory.NotSignedIn)
    }

    fun getMembership() = viewModelScope.launch(backgroundDispatcher) {
        authenticationProvider.getUser()?.also {
            when (val tokenResponse = authenticationProvider.getIdToken()) {
                is AuthenticationProviderResponse.Success -> when (val apiResponse = queryGetMembership(tokenResponse.data)) {
                    is ApiResponse.Success -> apiResponse.data.also { membership ->
                        _membership.postValue(PurchasedMembership.Success(membership))
                    }
                    is ApiResponse.Error -> when (val apiError = apiResponse.error) {
                        is ApiException.EmptyResponse -> _membership.postValue(PurchasedMembership.Empty)
                        is ApiException.WithErrors -> _membership.postValue(PurchasedMembership.Error(apiError.errorsList.joinToString("\n")))
                        is ApiException.CallFailure -> _membership.postValue(PurchasedMembership.Error(apiError.error.localizedMessage ?: apiError.error.toString()))
                    }
                }
                is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                    is AuthenticationProviderException.Error -> _membership.postValue(PurchasedMembership.Error(tokenError.error.localizedMessage ?: tokenError.error.toString()))
                    else -> _membership.postValue(PurchasedMembership.InvalidIdToken)
                }
            }
        } ?: _membership.postValue(PurchasedMembership.NotSignedIn)
    }

    private fun getProcessPurchaseInput(purchase: Purchase): ProcessPurchaseInput = ProcessPurchaseInput(
        kind = purchase.type,
        productId = purchase.productId,
        token = purchase.token
    )

    fun sendInAppPurchaseToApi(purchase: Purchase) = viewModelScope.launch(backgroundDispatcher) {
        authenticationProvider.getUser()?.also {
            when (val tokenResponse = authenticationProvider.getIdToken()) {
                is AuthenticationProviderResponse.Success -> when (val apiResponse = mutationProcessPurchase(getProcessPurchaseInput(purchase), tokenResponse.data)) {
                    is ApiResponse.Success -> apiResponse.data.coin?.also { coin ->
                        _coins.postValue(PurchasedCoin.Success(coin))
                    } ?: _coins.postValue(PurchasedCoin.Empty)
                    is ApiResponse.Error -> when (val apiError = apiResponse.error) {
                        is ApiException.EmptyResponse -> _coins.postValue(PurchasedCoin.Empty)
                        is ApiException.WithErrors -> _coins.postValue(PurchasedCoin.Error(apiError.errorsList.joinToString("\n")))
                        is ApiException.CallFailure -> _coins.postValue(PurchasedCoin.Error(apiError.error.localizedMessage ?: apiError.error.toString()))
                    }
                }
                is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                    is AuthenticationProviderException.Error -> _coins.postValue(PurchasedCoin.Error(tokenError.error.localizedMessage ?: tokenError.error.toString()))
                    else -> _coins.postValue(PurchasedCoin.InvalidIdToken)
                }
            }
        } ?: _coins.postValue(PurchasedCoin.NotSignedIn)
    }

    fun sendSubscriptionPurchaseToApi(purchase: Purchase) = viewModelScope.launch(backgroundDispatcher) {
        authenticationProvider.getUser()?.also {
            when (val tokenResponse = authenticationProvider.getIdToken()) {
                is AuthenticationProviderResponse.Success -> when (val apiResponse = mutationProcessPurchase(getProcessPurchaseInput(purchase), tokenResponse.data)) {
                    is ApiResponse.Success -> apiResponse.data.membership?.also { membership ->
                        _membership.postValue(PurchasedMembership.Success(membership))
                    } ?: _membership.postValue(PurchasedMembership.Empty)
                    is ApiResponse.Error -> when (val apiError = apiResponse.error) {
                        is ApiException.EmptyResponse -> _membership.postValue(PurchasedMembership.Empty)
                        is ApiException.WithErrors -> _membership.postValue(PurchasedMembership.Error(apiError.errorsList.joinToString("\n")))
                        is ApiException.CallFailure -> _membership.postValue(PurchasedMembership.Error(apiError.error.localizedMessage ?: apiError.error.toString()))
                    }
                }
                is AuthenticationProviderResponse.Error -> when (val tokenError = tokenResponse.error) {
                    is AuthenticationProviderException.Error -> _membership.postValue(PurchasedMembership.Error(tokenError.error.localizedMessage ?: tokenError.error.toString()))
                    else -> _membership.postValue(PurchasedMembership.InvalidIdToken)
                }
            }
        } ?: _membership.postValue(PurchasedMembership.NotSignedIn)
    }

    fun sendPurchaseToApi(purchase: Purchase) = viewModelScope.launch(backgroundDispatcher) {
        when (purchase.type) {
            ProductType.INAPP -> sendInAppPurchaseToApi(purchase)
            ProductType.SUBSCRIPTION -> sendSubscriptionPurchaseToApi(purchase)
        }
    }

    fun getAvailableInApps() = viewModelScope.launch(backgroundDispatcher) {
        when (val billingResponse = billingManager.getProductDetails(inAppsToPurchase)) {
            is BillingResponse.Success -> _inApps.postValue(AvailableProducts.Success(billingResponse.data))
            is BillingResponse.Error -> when (val billingException = billingResponse.error) {
                is BillingException.EmptyResponse -> _inApps.postValue(AvailableProducts.Empty)
                is BillingException.InstanceError -> _inApps.postValue(AvailableProducts.Error(billingException.error))
                is BillingException.FeatureNotSupported -> _inApps.postValue(AvailableProducts.FeatureNotSupported)
                is BillingException.ServiceUnavailable -> _inApps.postValue(AvailableProducts.ServiceUnavailable)
                is BillingException.Disconnected -> _inApps.postValue(AvailableProducts.Disconnected)
                is BillingException.UnknownError -> _inApps.postValue(AvailableProducts.Error(billingException.error))
                else -> _inApps.postValue(AvailableProducts.Error(billingException.toString()))
            }
        }
    }

    fun getAvailableSubscriptions() = viewModelScope.launch(backgroundDispatcher) {
        when (val billingResponse = billingManager.getProductDetails(subscriptionsToPurchase)) {
            is BillingResponse.Success -> _subscriptions.postValue(AvailableProducts.Success(billingResponse.data))
            is BillingResponse.Error -> when (val billingException = billingResponse.error) {
                is BillingException.EmptyResponse -> _subscriptions.postValue(AvailableProducts.Empty)
                is BillingException.InstanceError -> _subscriptions.postValue(AvailableProducts.Error(billingException.error))
                is BillingException.FeatureNotSupported -> _subscriptions.postValue(AvailableProducts.FeatureNotSupported)
                is BillingException.ServiceUnavailable -> _subscriptions.postValue(AvailableProducts.ServiceUnavailable)
                is BillingException.Disconnected -> _subscriptions.postValue(AvailableProducts.Disconnected)
                is BillingException.UnknownError -> _subscriptions.postValue(AvailableProducts.Error(billingException.error))
                else -> _subscriptions.postValue(AvailableProducts.Error(billingException.toString()))
            }
        }
    }

    private fun getAvailableProducts(productType: ProductType) = viewModelScope.launch(backgroundDispatcher) {
        when (productType) {
            ProductType.INAPP -> getAvailableInApps()
            ProductType.SUBSCRIPTION -> getAvailableSubscriptions()
        }
    }

    fun launchPurchase(activity: AppCompatActivity, products: List<Product>) = viewModelScope.launch(backgroundDispatcher) {
        when (val billingResponse = billingManager.launchBilling(activity, products)) {
            is BillingResponse.Success -> {}
            is BillingResponse.Error -> {}
        }
    }

    fun askForPurchases(productType: ProductType) = billingManager.askForPurchases(productType)

    fun consumePurchase(token: String) = billingManager.consumePurchase(token)

    sealed class BillingConnection {
        object Connected : BillingConnection()
        object Disconnected : BillingConnection()
        object FeatureNotSupported : BillingConnection()
        object ServiceUnavailable : BillingConnection()
        class Error(val reason: String) : BillingConnection()
    }

    sealed class AvailableProducts {
        class Success(val products: List<Product>): AvailableProducts()
        object Empty: AvailableProducts()
        class Error(val reason: String): AvailableProducts()
        object FeatureNotSupported: AvailableProducts()
        object ServiceUnavailable: AvailableProducts()
        object Disconnected : AvailableProducts()
    }

    sealed class Purchases {
        class Success(val purchases: List<Purchase>): Purchases()
        object Empty: Purchases()
        object CanceledByUser: Purchases()
        class Error(val reason: String): Purchases()
        object FeatureNotSupported: Purchases()
        object ServiceUnavailable: Purchases()
        object Disconnected: Purchases()
    }

    sealed class Consumption {
        object Success: Consumption()
        object FeatureNotSupported: Consumption()
        class Error(val reason: String): Consumption()
        object ServiceUnavailable: Consumption()
        object Disconnected: Consumption()
    }

    sealed class PurchasedCoin {
        class Success(val coin: Coin): PurchasedCoin()
        object Empty: PurchasedCoin()
        class Error(val reason: String): PurchasedCoin()
        object NotSignedIn : PurchasedCoin()
        object InvalidIdToken : PurchasedCoin()
    }

    sealed class CoinsHistory {
        class Success(val coins: List<Coin>): CoinsHistory()
        object Empty: CoinsHistory()
        class Error(val reason: String): CoinsHistory()
        object NotSignedIn: CoinsHistory()
        object InvalidIdToken: CoinsHistory()
    }

    sealed class PurchasedMembership {
            class Success(val membership: Membership) : PurchasedMembership()
        object Empty: PurchasedMembership()
            class Error(val reason: String) : PurchasedMembership()
        object NotSignedIn : PurchasedMembership()
        object InvalidIdToken : PurchasedMembership()
        }
    }
