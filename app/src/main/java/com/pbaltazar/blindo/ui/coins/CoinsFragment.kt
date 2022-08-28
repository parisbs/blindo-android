package com.pbaltazar.blindo.ui.coins

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentCoinsBinding
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.purchases.inapp.InApp
import com.pbaltazar.blindo.utils.billing.ui.BilleableFragment
import com.pbaltazar.blindo.utils.billing.ui.BillingViewModel
import com.pbaltazar.blindo.utils.constants.AUTH_CANCELED_ON_DIALOG

class CoinsFragment : BilleableFragment<FragmentCoinsBinding>() {

    private lateinit var currentCoins: TextView
    private lateinit var history: ImageButton
    private lateinit var coinsContainer: RecyclerView

    private val coinsAdapter: CoinsAdapter = CoinsAdapter { item: InApp ->
        onInAppClickListener(item)
    }
    private val coinsHistoryAdapter: CoinsHistoryAdapter = CoinsHistoryAdapter()

    private var isCoinsHistorySubscribed: Boolean = false

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeAuth()
        subscribeInAppsToPurchase()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCoinsBinding.inflate(inflater, container, false)
        currentCoins = binding!!.currentCoins
        history = binding!!.history
        coinsContainer = binding!!.coinsContainer
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        subscribeUser()
    }

    override fun onResume() {
        super.onResume()
        if (coinsAdapter.itemCount < 1) {
            getInAppsToPurchase()
        }
    }

    override fun getMenuResId(): Int = R.menu.coins

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menuProcessPurchases -> {
            askForNewInAppPurchases()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onSubscribeUser(user: User?) {
        user?.also { currentUser ->
            if (currentUser.coinsLeft < 1) {
                currentCoins.text = ""
            } else {
                currentCoins.text = resources.getQuantityString(
                    R.plurals.coins__current_coins,
                    currentUser.coinsLeft,
                    currentUser.coinsLeft
                )
            }
        } ?: findNavController().navigate(
            CoinsFragmentDirections.actionFromCoinsToRequiresAuth()
        )
    }

    private fun subscribeAuth() = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
        AUTH_CANCELED_ON_DIALOG
    )?.observe(this) {
        if (it.not()) {
            launchLoginScreen()
        } else {
            findNavController().popBackStack()
        }
    }

    override fun onInAppsToPurchase(availableProducts: BillingViewModel.AvailableProducts) {
        when (availableProducts) {
            is BillingViewModel.AvailableProducts.Success -> availableProducts.products.map { it as InApp }.also { inApps ->
                if (coinsAdapter.itemCount > 0) {
                    coinsAdapter.clearItems()
                }
                coinsAdapter.appendItems(inApps)
                coinsHistoryAdapter.coinsProducts = inApps
                if (isCoinsHistorySubscribed.not()) {
                    isCoinsHistorySubscribed = true
                    history.isEnabled = true
                    subscribeCoinsHistory()
                }
            }
            else -> Unit
        }
    }

    override fun onCoinsHistory(coinsHistory: BillingViewModel.CoinsHistory) {
        when (coinsHistory) {
            is BillingViewModel.CoinsHistory.Success -> coinsHistory.coins.also { coins ->
                if (coinsHistoryAdapter.itemCount > 0) {
                    coinsHistoryAdapter.clearItems()
                }
                coinsContainer.adapter = coinsHistoryAdapter
                coinsHistoryAdapter.appendItems(coins)
                setHistoryOnClickListenerWhenIsInHistory()
            }
            is BillingViewModel.CoinsHistory.Empty -> setErrorInCoinsHistory(R.string.coins__no_coins)
            is BillingViewModel.CoinsHistory.Error -> setErrorInCoinsHistory(coinsHistory.reason)
            else -> setErrorInCoinsHistory(coinsHistory.toString())
        }
    }

    private fun setErrorInCoinsHistory(reason: Int? = null) =
        setErrorInCoinsHistory(reason?.let { getString(it)})

    private fun setErrorInCoinsHistory(reason: String? = null) {
        if ((coinsContainer.adapter is CoinsAdapter).not()) {
            coinsContainer.adapter = coinsAdapter
        }
        setHistoryOnClickListenerWhenIsInCoinsToPurchase()
        reason?.also { showErrorMessage(it) }
    }

    private fun showErrorMessage(@StringRes reason: Int) = showErrorMessage(getString(reason))

    private fun showErrorMessage(reason: String) = Snackbar.make(
        coinsContainer,
        reason,
        Snackbar.LENGTH_LONG
    ).show()

    private fun setHistoryOnClickListenerWhenIsInHistory() {
        history.apply {
            setImageResource(R.drawable.ic_arrow_back_black_24dp)
            contentDescription = getString(R.string.coins__coins_to_purchase)
            setOnClickListener {
                coinsContainer.adapter = coinsAdapter
                setHistoryOnClickListenerWhenIsInCoinsToPurchase()
            }
        }
    }

    private fun setHistoryOnClickListenerWhenIsInCoinsToPurchase() {
        history.apply {
            setImageResource(R.drawable.ic_history_black_24dp)
            contentDescription = getString(R.string.coins__history)
            setOnClickListener {
                getCoinsHistory()
            }
        }
    }

    private fun setupUi() {
        history.isEnabled = false
        setHistoryOnClickListenerWhenIsInCoinsToPurchase()
        coinsContainer.adapter = coinsAdapter
        currentCoins.apply {
            ViewCompat.setAccessibilityHeading(this, true)
        }
    }

    private fun onInAppClickListener(inApp: InApp) = launchPurchase(listOf(inApp))
}
