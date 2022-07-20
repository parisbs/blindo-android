package com.pbaltazar.blindo.ui.blindo

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.pbaltazar.blindo.MainNavigationDirections
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ActivityBlindoBinding
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.purchases.Purchase
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.ads.ui.AdsViewModel
import com.pbaltazar.blindo.utils.analytics.AnalyticsManager
import com.pbaltazar.blindo.utils.billing.ui.BilleableActivity
import com.pbaltazar.blindo.utils.billing.ui.BillingViewModel
import com.pbaltazar.blindo.utils.constants.ACTIONS_HOST
import com.pbaltazar.blindo.utils.constants.ARGUMENT_CONSENT_STATUS
import com.pbaltazar.blindo.utils.constants.REQUEST_PERMISSIONS_ACTION
import com.pbaltazar.blindo.utils.extensions.gone
import com.pbaltazar.blindo.utils.extensions.visible
import com.pbaltazar.blindo.utils.messaging.MessagingManager
import com.pbaltazar.blindo.utils.notifications.NotificationsManager
import com.pbaltazar.blindo.utils.updates.UpdateManager
import org.koin.androidx.viewmodel.ext.android.viewModel

class BlindoActivity : BilleableActivity() {

    private val adsViewModel: AdsViewModel by viewModel()
    private var binding: ActivityBlindoBinding? = null

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var blindocoordinator: CoordinatorLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navView: NavigationView
    private lateinit var adBanner: AdView
    private lateinit var navController: NavController
    private lateinit var headerSignIn: TextView
    private lateinit var headerUserPicture: AppCompatImageView
    private lateinit var headerUserCrown: AppCompatImageView
    private lateinit var headerUserName: TextView
    private lateinit var headerUserProfile: TextView
    private lateinit var headerSignOut: TextView
    private lateinit var searchBox: SearchView
    private var searchMenuItem: MenuItem? = null

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var emailVerificationMessage: Snackbar

    private var isWaitingForSplash: Boolean = true
    private var isAdBannerLoaded: Boolean = false

    private var currentDestinationId: Int = 0
    private val preferencesScreens: List<Int> = listOf(
        R.id.settingsEntryPoint,
        R.id.settingsBlindoVision,
        R.id.settingsAds
    )
    private val adsFreeScreens: List<Int> = listOf(
        R.id.navCoins,
        R.id.navMembership,
        R.id.settingsEntryPoint,
        R.id.settingsBlindoVision,
        R.id.settingsAds,
        R.id.navTutorial,
        R.id.navAbout,
        R.id.dialogClearSearchHistory,
        R.id.dialogClearCache,
        R.id.dialogRequiresAuth,
        R.id.dialogRequiresPremium,
        R.id.navPermissions,
        R.id.navVisionResults
    )

    private val recentPurchases: MutableList<Purchase> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlindoBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        toolbar = binding!!.appBar.toolbar
        setSupportActionBar(toolbar)

        AnalyticsManager.initialize()
        NotificationsManager.initialize(this)
        adsViewModel.initializeAdsManager(this)
        UpdateManager.initialize(this)
        MessagingManager.initialize(this)
        registerPurchasesNotificationChannel()

        drawerLayout = binding!!.drawerLayout
        blindocoordinator = binding!!.appBar.blindocoordinator
        navView = binding!!.navView
        adBanner = binding!!.appBar.content.adBanner
        navController = findNavController(R.id.navController)

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            onDestinationChangedListener(controller, destination, arguments)
        }
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navSplash,
                R.id.navHome,
                R.id.navLocalApps,
                R.id.navBackup,
                R.id.navUserComments,
                R.id.navSli
            ),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        headerSignIn = navView.getHeaderView(0).findViewById<TextView>(R.id.signIn)
        headerUserPicture = navView.getHeaderView(0).findViewById<AppCompatImageView>(R.id.userPicture)
        headerUserCrown = navView.getHeaderView(0).findViewById<AppCompatImageView>(R.id.userCrown)
        headerUserName = navView.getHeaderView(0).findViewById<TextView>(R.id.userName)
        headerUserProfile = navView.getHeaderView(0).findViewById<TextView>(R.id.userProfile)
        headerSignOut = navView.getHeaderView(0).findViewById<TextView>(R.id.signOut)

        setupUi()
        subscribeUser()
        subscribeIsValidationEmailSent()
        subscribeAdsConsentStatus()
        subscribeAdsSettings()
    }

    override fun onResume() {
        super.onResume()
        UpdateManager.checkForUpdates()
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.blindo, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchMenuItem = menu.findItem(R.id.searchApps)
        searchBox = (menu.findItem(R.id.searchApps).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }
        hideSearchBoxInPreferencesScreens()
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (searchBox.isIconified.not()) {
            searchBox.isIconified = true
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == UpdateManager.UPDATE_CHECKER_CODE) {
            if (resultCode == RESULT_CANCELED || resultCode == UpdateManager.UPDATE_FAILED) {
                UpdateManager.checkForUpdates()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.also { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            Intent.ACTION_SEARCH -> intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                navController.navigate(
                    MainNavigationDirections.actionGlobalToSearch(query)
                )
            }
            Intent.ACTION_VIEW -> intent.data?.also { uri ->
                when (uri.host ?: "") {
                    ACTIONS_HOST -> uri.path?.also { path ->
                        when (path) {
                            REQUEST_PERMISSIONS_ACTION -> navController.navigate(
                                MainNavigationDirections.actionGlobalToPermissions()
                            )
                        }
                    }
                }
            }
            else -> Unit
        }
    }

    override fun onSubscribeUser(user: User?) {
        user?.also { currentUser ->
            headerSignIn.visibility = View.GONE
            headerUserPicture.apply {
                Glide.with(this@BlindoActivity)
                    .load(currentUser.picture)
                    .placeholder(R.mipmap.default_user_picture)
                    .centerCrop()
                    .into(this)
                visibility = View.VISIBLE
            }
            headerUserName.apply {
                text = currentUser.name
                visibility = View.VISIBLE
            }
            headerUserProfile.apply {
                setOnClickListener {
                    navController.navigate(
                        MainNavigationDirections.actionGlobalToMyProfile()
                    )
                }
                visibility = View.VISIBLE
            }
            headerSignOut.apply {
                setOnClickListener {
                    if (emailVerificationMessage.isShown) {
                        emailVerificationMessage.dismiss()
                    }
                    signOut()
                }
                visibility = View.VISIBLE
            }
            refreshAdsBanner(currentDestinationId)
            if (currentUser.isVerified.not()) {
                emailVerificationMessage.show()
            }
        } ?: run {
            refreshAdsBanner(currentDestinationId)
            headerSignIn.visibility = View.VISIBLE
            headerUserPicture.visibility = View.GONE
            headerUserCrown.visibility = View.GONE
            headerUserName.visibility = View.GONE
            headerUserProfile.visibility = View.GONE
            headerSignOut.visibility = View.GONE
        }
    }

    override fun onNewPurchases(purchases: BillingViewModel.Purchases) {
        super.onNewPurchases(purchases)
        recentPurchases.clear()
        when (purchases) {
            is BillingViewModel.Purchases.Success -> purchases.purchases.also { newPurchases ->
                newPurchases.forEach { purchase ->
                    if (recentPurchases.contains(purchase).not()) {
                        recentPurchases.add(purchase)
                    }
                }
            }
            else -> Unit
        }
    }

    override fun onCoinsPurchased(purchasedCoin: BillingViewModel.PurchasedCoin) {
        super.onCoinsPurchased(purchasedCoin)
        when (purchasedCoin) {
            is BillingViewModel.PurchasedCoin.Success -> purchasedCoin.coin.also { coin ->
                if (currentDestinationId.equals(R.id.navCoins).not()) {
                    val expectedPurchase: Purchase? = recentPurchases.filter { it.token.equals(coin.token) }.takeUnless { it.isEmpty() }?.first()
                    if (expectedPurchase != null) {
                        recentPurchases.remove(expectedPurchase)
                        showNewCoinsNotification(coin)
                    }
                }
            }
            else -> Unit
        }
    }

    override fun onMembershipPurchased(purchasedMembership: BillingViewModel.PurchasedMembership) {
        super.onMembershipPurchased(purchasedMembership)
        when (purchasedMembership) {
            is BillingViewModel.PurchasedMembership.Success -> purchasedMembership.membership.also { membership ->
                if (currentDestinationId.equals(R.id.navMembership).not()) {
                    val expectedPurchase: Purchase? = recentPurchases.filter { it.token.equals(membership.token) }.takeUnless { it.isEmpty() }?.first()
                    if (expectedPurchase != null) {
                        recentPurchases.remove(expectedPurchase)
                        showNewMembershipNotification(membership)
                    }
                }
            }
            else -> Unit
        }
    }

    private fun processError(@StringRes reason: Int) = processError(getString(reason))

    private fun processError(reason: String) = Snackbar.make(
        blindocoordinator,
        reason,
        Snackbar.LENGTH_LONG
    ).show()

    private fun onDestinationChangedListener(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        currentDestinationId = destination.id
        when (currentDestinationId) {
            R.id.navSplash -> {
                isWaitingForSplash = true
                adBanner.visibility = View.GONE
                toolbar.visibility = View.GONE
            }
            else -> {
                isWaitingForSplash = false
                toolbar.visible()
                refreshAdsBanner(currentDestinationId)
                refreshUi(currentDestinationId)
            }
        }
    }

    private fun hideSearchBoxInPreferencesScreens() {
        if (preferencesScreens.contains(currentDestinationId)) {
            searchMenuItem?.setVisible(false)
        }
    }

    private fun refreshAdsBanner(destinationId: Int) {
        if (adsFreeScreens.contains(destinationId)) {
            adBanner.gone()
        } else {
            getUser()?.also { currentUser ->
                if (currentUser.isPremium) {
                    adBanner.visibility = View.GONE
                    headerUserCrown.visibility = View.VISIBLE
                } else {
                    if (isAdBannerLoaded.not()) {
                        if (isWaitingForSplash.not()) {
                            adsViewModel.updateAdsConsentStatus()
                        }
                    } else {
                        if (isWaitingForSplash) {
                            adBanner.visibility = View.GONE
                        } else {
                            adBanner.visibility = View.VISIBLE
                        }
                    }
                    headerUserCrown.visibility = View.GONE
                }
            } ?: run {
                if (isAdBannerLoaded.not()) {
                    if (isWaitingForSplash.not()) {
                        adsViewModel.updateAdsConsentStatus()
                    }
                } else {
                    if (isWaitingForSplash) {
                        adBanner.visibility = View.GONE
                    } else {
                        adBanner.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun refreshUi(destinationId: Int) {
        when (destinationId) {
            R.id.navHome, R.id.navLocalApps, R.id.navBackup, R.id.navUserComments, R.id.navSli -> {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                supportActionBar?.subtitle = null
            }
            R.id.navMyProfile -> drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }

    private fun setupUi() {
        headerSignIn.setOnClickListener {
            launchLoginScreen()
        }
        emailVerificationMessage = Snackbar.make(
            blindocoordinator,
            getString(R.string.header__email_not_verified),
            Snackbar.LENGTH_INDEFINITE
        ).setAction(getString(R.string.header__send_verification_email), {
                sendVerificationEmail()
            })
    }

    private fun subscribeAdsConsentStatus() = adsViewModel.adsConsentStatus.observe(this, Observer {
        when (it) {
            is AdsViewModel.AdsConsentStatus.Success -> when (val status = it.status) {
                AdsManager.ConsentStatus.ADS_FREE -> if (getUser()?.isPremium?.not() ?: true) {
                    if (isWaitingForSplash.not()) {
                        navController.navigate(
                            MainNavigationDirections.actionGlobalToAdsSettings(status.name, false)
                        )
                    }
                } else {
                    adBanner.visibility = View.GONE
                }
                AdsManager.ConsentStatus.UNKNOWN, AdsManager.ConsentStatus.INTERNET_ERROR_OR_ADS_BLOCKER -> if (isWaitingForSplash.not()) {
                    navController.navigate(
                        MainNavigationDirections.actionGlobalToAdsSettings(status.name, false)
                    )
                } else Unit
                else -> {
                    if (isWaitingForSplash.not()) {
                        adBanner.visibility = View.VISIBLE
                    } else {
                        adBanner.visibility = View.GONE
                    }
                    loadAds()
                }
            }
            is AdsViewModel.AdsConsentStatus.Failure -> navController.navigate(
                MainNavigationDirections.actionGlobalToAdsSettings(AdsManager.ConsentStatus.INTERNET_ERROR_OR_ADS_BLOCKER.name, false)
            )
        }
    })

    private fun subscribeAdsSettings() =
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<AdsManager.ConsentStatus>(
            ARGUMENT_CONSENT_STATUS)?.observe(this, Observer {
            if (isWaitingForSplash.not()) {
                adsViewModel.setConsentStatus(AdsViewModel.AdsConsentStatus.Success(it))
            }
        })

    override fun onIsValidationEmailSent(isValidationEmailSent: Boolean) {
        if (isValidationEmailSent) {
            Snackbar.make(
                blindocoordinator,
                getString(R.string.header__verification_email_sended),
                Snackbar.LENGTH_LONG
            ).show()
        } else {
            emailVerificationMessage.setText(getString(R.string.header__email_verification_not_sended))
                .show()
        }
    }

    private fun loadAds() {
        if (isAdBannerLoaded.not()) {
            adsViewModel.getBannerAd(adBanner, object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    isAdBannerLoaded = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    navController.navigate(
                        MainNavigationDirections.actionGlobalToAdsSettings(AdsManager.ConsentStatus.INTERNET_ERROR_OR_ADS_BLOCKER.name, false)
                    )
                }
            })
        }
    }
}
