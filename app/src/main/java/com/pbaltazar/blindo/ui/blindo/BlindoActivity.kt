package com.pbaltazar.blindo.ui.blindo

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.SearchView
import android.widget.TextView
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
import com.google.ads.consent.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.pbaltazar.blindo.MainNavigationDirections
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ActivityBlindoBinding
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.ads.ui.AdsViewModel
import com.pbaltazar.blindo.utils.analytics.AnalyticsManager
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableActivity
import com.pbaltazar.blindo.utils.billing.ui.BillingViewModel
import com.pbaltazar.blindo.utils.constants.*
import com.pbaltazar.blindo.utils.extensions.gone
import com.pbaltazar.blindo.utils.extensions.visible
import com.pbaltazar.blindo.utils.messaging.ui.MessagingViewModel
import com.pbaltazar.blindo.utils.updates.UpdateManager
import org.koin.androidx.viewmodel.ext.android.viewModel

class BlindoActivity : AuthenticableActivity() {

    private val adsViewModel: AdsViewModel by viewModel()
    private val billingViewModel: BillingViewModel by viewModel()
    private val messagingViewModel: MessagingViewModel by viewModel()
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

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var emailVerificationMessage: Snackbar

    private var isWaitingForSplash: Boolean = true
    private var isAdBannerLoaded: Boolean = false

    private val adsFreeScreens: List<Int> = listOf(
        R.id.navTutorial,
        R.id.navAdsSettings,
        R.id.navPremium,
        R.id.navAbout,
        R.id.dialogRequiresAuth,
        R.id.dialogRequiresPremium,
        R.id.dialogClearSearchHistory
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlindoBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        toolbar = binding!!.appBar.toolbar
        setSupportActionBar(toolbar)

        adsViewModel.initializeAdsManager(this)
        AnalyticsManager.initialize()
        UpdateManager.initialize(this)

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
        subscribeMessagingToken()
    }

    override fun onResume() {
        super.onResume()
        UpdateManager.checkForUpdates()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.blindo, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchBox = (menu.findItem(R.id.searchApps).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
        }
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

    override fun onDestroy() {
        super.onDestroy()
        binding = null
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
            else -> Unit
        }
    }

    override fun onSubscribeUser() {
        getUser()?.also { currentUser ->
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
                        MainNavigationDirections.actionGlobalToUserProfile()
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
            if (currentUser.isVerified.not()) {
                emailVerificationMessage.show()
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
            headerSignIn.visibility = View.VISIBLE
            headerUserPicture.visibility = View.GONE
            headerUserCrown.visibility = View.GONE
            headerUserName.visibility = View.GONE
            headerUserProfile.visibility = View.GONE
            headerSignOut.visibility = View.GONE
        }
    }

    private fun subscribeMessagingToken() = messagingViewModel.messagingToken.observe(this, Observer {
        when (val response = it) {
            is MessagingViewModel.MessagingToken.Success -> getDevice()?.also { device ->
                updateDevice(
                    device.copy(
                        gcmToken = response.token
                    )
                )
            }
            is MessagingViewModel.MessagingToken.Error -> {}
        }
    })

    private fun onDestinationChangedListener(controller: NavController, destination: NavDestination, arguments: Bundle?) {
        when (destination.id) {
            R.id.navSplash -> {
                isWaitingForSplash = true
                adBanner.visibility = View.GONE
                toolbar.visibility = View.GONE
            }
            else -> {
                isWaitingForSplash = false
                toolbar.visible()
                refreshAdsBanner(destination.id)
                refreshUi(destination.id)
            }
        }
    }

    private fun refreshAdsBanner(destinationId: Int) {
        if (adsFreeScreens.contains(destinationId)) {
            adBanner.gone()
        } else {
            if (getUser()?.isPremium?.not() ?: true) {
                if (isAdBannerLoaded.not()) {
                    adsViewModel.updateAdsConsentStatus()
                } else {
                    adBanner.visible()
                }
            } else {
                adBanner.gone()
            }
        }
    }

    private fun refreshUi(destinationId: Int) {
        when (destinationId) {
            R.id.navHome, R.id.navLocalApps, R.id.navBackup, R.id.navUserComments, R.id.navSli -> {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                supportActionBar?.subtitle = null
            }
            R.id.navUserProfile -> drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        }
    }

    private fun setupUi() {
        headerSignIn.setOnClickListener {
            loginScreen.launch(Unit)
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
