package com.pbaltazar.blindo.ui.coins

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.pbaltazar.blindo.databinding.FragmentCoinsBinding
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.purchases.enums.ProductType
import com.pbaltazar.blindo.entities.purchases.inapp.InApp
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import com.pbaltazar.blindo.utils.billing.ui.BillingViewModel
import com.pbaltazar.blindo.utils.constants.AUTH_CANCELED_ON_DIALOG
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CoinsFragment : AuthenticableFragment<FragmentCoinsBinding>() {

    private val billingViewModel: BillingViewModel by sharedViewModel()

    private lateinit var currentCoins: TextView
    private lateinit var history: ImageButton
    private lateinit var coinsContainer: RecyclerView

    private val coinsAdapter: CoinsAdapter = CoinsAdapter { item: InApp ->
        onInAppClickListener(item)
    }

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeAuth()
        subscribeInApps()
        billingViewModel.askForPurchases(ProductType.INAPP)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
            billingViewModel.getAvailableInApps()
        }
    }

    override fun onSubscribeUser() {
        super.onSubscribeUser()
        getUser()?.also { user: User ->
            currentCoins.text = resources.getQuantityString(
                com.pbaltazar.blindo.R.plurals.coins__current_coins,
                user.coinsLeft,
                user.coinsLeft
            )
        } ?: findNavController().navigate(
            CoinsFragmentDirections.actionFromCoinsToRequiresAuth()
        )
    }

    private fun subscribeAuth() = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
        AUTH_CANCELED_ON_DIALOG
    )?.observe(this, Observer {
        if (it.not()) {
            requireAuthenticableActivity.loginScreen.launch(Unit)
        } else {
            findNavController().popBackStack()
        }
    })

    private fun subscribeInApps() = billingViewModel.inApps.observe(this, Observer {
        when (val response = it) {
            is BillingViewModel.AvailableProducts.Success -> response.products.mapNotNull { it as InApp }.also { inApps ->
                if (coinsAdapter.itemCount > 0) {
                    coinsAdapter.clearItems()
                }
                coinsAdapter.appendItems(inApps)
            }
            else -> Unit
        }
    })

    private fun setupUi() {
        ViewCompat.setAccessibilityHeading(currentCoins, true)
        coinsContainer.adapter = coinsAdapter
    }

    private fun onInAppClickListener(inApp: InApp) = billingViewModel.launchPurchase(
        requireActivity() as AppCompatActivity,
        listOf(inApp)
    )
}
