package com.pbaltazar.blindo.ui.blindo

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.drawerlayout.widget.DrawerLayout
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
import com.pbaltazar.blindo.entities.responses.AdsResponse
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.ads.ui.AdsViewModel
import com.pbaltazar.blindo.utils.analytics.AnalyticsManager
import com.pbaltazar.blindo.utils.billing.ui.BilleableActivity
import com.pbaltazar.blindo.utils.billing.ui.BillingViewModel
import com.pbaltazar.blindo.utils.extensions.gone
import com.pbaltazar.blindo.utils.extensions.visible
import com.pbaltazar.blindo.utils.messaging.ui.MessagingViewModel
import com.pbaltazar.blindo.utils.notifications.NotificationsManager
import com.pbaltazar.blindo.utils.updates.UpdateManager
import org.koin.androidx.viewmodel.ext.android.viewModel

class BlindoActivity : BilleableActivity(),
    MenuProvider {

    private val messagingViewModel: MessagingViewModel by viewModel()
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
        addMenuProvider(this)

        AnalyticsManager.initialize()
        NotificationsManager.initialize(this)
        UpdateManager.initialize(this)
        registerPurchasesNotificationChannel()
        messagingViewModel.initialize(this)

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

        headerSignIn = navView.getHeaderView(0).findViewById(R.id.signIn)
        headerUserPicture = navView.getHeaderView(0).findViewById(R.id.userPicture)
        headerUserCrown = navView.getHeaderView(0).findViewById(R.id.userCrown)
        headerUserName = navView.getHeaderView(0).findViewById(R.id.userName)
        headerUserProfile = navView.getHeaderView(0).findViewById(R.id.userProfile)
        headerSignOut = navView.getHeaderView(0).findViewById(R.id.signOut)

        setupUi()
        subscribeUser()
        subscribeAdsConsentStatus()
        subscribeIsAdsClientInitialized()
        subscribeIsValidationEmailSent()
    }

    override fun onResume() {
        super.onResume()
        UpdateManager.checkForUpdates()
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.blindo, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchMenuItem = menu.findItem(R.id.searchApps)
        searchBox = (menu.findItem(R.id.searchApps).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }
        hideSearchBoxInPreferencesScreens()
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
        android.R.id.home -> navController.navigateUp(appBarConfiguration)
        else -> false
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        when (intent.action) {
            Intent.ACTION_SEARCH -> intent.getStringExtra(SearchManager.QUERY)?.also { query ->
                navController.navigate(
                    MainNavigationDirections.actionGlobalToSearch(query)
                )
            }
            else -> navController.handleDeepLink(intent)
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
            is BillingViewModel.Purchases.Success -> purchases.purchases.onEach { purchase ->
                if (recentPurchases.contains(purchase).not()) {
                    recentPurchases.add(purchase)
                }
            }
            else -> Unit
        }
    }

    override fun onCoinsPurchased(purchasedCoin: BillingViewModel.PurchasedCoin) {
        super.onCoinsPurchased(purchasedCoin)
        when (purchasedCoin) {
            is BillingViewModel.PurchasedCoin.Success -> purchasedCoin.coin.also { coin ->
                if ((currentDestinationId == R.id.navCoins).not()) {
                    val expectedPurchase: Purchase? = recentPurchases.filter { it.token == coin.token }.takeUnless { it.isEmpty() }?.first()
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
                if ((currentDestinationId == R.id.navMembership).not()) {
                    val expectedPurchase: Purchase? = recentPurchases.filter { it.token == membership.token }.takeUnless { it.isEmpty() }?.first()
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
            searchMenuItem?.isVisible = false
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
        ).setAction(getString(R.string.header__send_verification_email)) {
            sendVerificationEmail()
        }
    }

    private fun subscribeIsAdsClientInitialized() = adsViewModel.isAdsClientInitialized.observe(this) {
        if (it) {
            if (getUser()?.isPremium != true) {
                loadAds()
            }
        }
    }

    private fun subscribeAdsConsentStatus() = adsViewModel.adsConsentStatus.observe(this) {
        when (it) {
            is AdsResponse.Success -> when (it.data) {
                AdsManager.Companion.ConsentStatus.PERSONALIZED, AdsManager.Companion.ConsentStatus.NON_PERSONALIZED, AdsManager.Companion.ConsentStatus.NON_REQUIRED -> if (getUser()?.isPremium == true) {
                    adBanner.gone()
                } else {
                    if (isWaitingForSplash.not()) {
                        adBanner.visible()
                    } else {
                        adBanner.gone()
                    }
                }
                else -> Unit
            }
            else -> Unit
        }
    }

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
                    adsViewModel.retryToLoadAds { loadAds() }
                }
            })
        }
    }
}
