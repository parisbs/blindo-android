package com.pbaltazar.blindo.ui.pack.upload

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ActivityUploadPackBinding
import com.pbaltazar.blindo.entities.Label
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.ads.ui.AdsViewModel
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableActivity
import com.pbaltazar.blindo.utils.extensions.countApps
import com.pbaltazar.blindo.utils.extensions.getLanguages
import com.pbaltazar.blindo.utils.extensions.toHumanReadable
import org.koin.androidx.viewmodel.ext.android.viewModel

class UploadPackActivity : AuthenticableActivity() {

    private val uploadPackViewModel: UploadPackViewModel by viewModel()
    private val adsViewModel: AdsViewModel by viewModel()
    private var binding: ActivityUploadPackBinding? = null

    private lateinit var toolbar: Toolbar
    private lateinit var adBanner: AdView
    private lateinit var resume: TextView
    private lateinit var result: TextView
    private lateinit var send: ImageButton

    private lateinit var labels: List<Label>

    private var isReady: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadPackBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        toolbar = binding!!.toolbar
        setSupportActionBar(toolbar)

        adsViewModel.initializeAdsManager(this)

        adBanner = binding!!.content.adBanner
        resume = binding!!.content.resume
        result = binding!!.content.result
        send = binding!!.content.send
        send.isEnabled = false

        subscribeUser()
        subscribeAdsConsentStatus()
        subscribeIsAdsClientInitialized()
        subscribeLabels()
        subscribeProcessResult()
    }

    override fun onPostResume() {
        super.onPostResume()
        authenticateUser()
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }

    override fun onSubscribeUser(user: User?) {
        user?.also { currentUser ->
            supportActionBar?.subtitle = getString(
                R.string.uploadpack__user,
                currentUser.name
            )
            if (currentUser.isPremium.not()) {
                adsViewModel.updateAdsConsentStatus()
            } else {
                adBanner.visibility = View.GONE
                if (isReady.not()) {
                    processIntent()
                }
            }
        } ?: run {
            launchLoginScreen()
        }
    }

    private fun subscribeAdsConsentStatus() = adsViewModel.adsConsentStatus.observe(this, Observer {
        when (it) {
            is AdsViewModel.AdsConsentStatus.Success -> when (it.status) {
                AdsManager.ConsentStatus.ADS_FREE -> {
                    resume.text = getString(
                        R.string.ads__current_status,
                        getString(R.string.ads__ads_free_but_not_purchased)
                    )
                    send.isEnabled = false
                }
                AdsManager.ConsentStatus.UNKNOWN -> {
                    resume.text = getString(
                        R.string.ads__current_status,
                        getString(R.string.ads__unknown_consent)
                    )
                    send.isEnabled = false
                }
                else -> adsViewModel.initializeAdsClient()
            }
            is AdsViewModel.AdsConsentStatus.Failure -> {
                resume.text = it.reason
                send.isEnabled = false
            }
        }
    })

    private fun subscribeIsAdsClientInitialized() = adsViewModel.isAdsClientInitialized.observe(this, Observer {
        if (it) {
            loadAds()
        } else {
            resume.text = getString(
                R.string.ads__current_status,
                getString(R.string.ads__ads_blocker_or_internet_error)
            )
            send.isEnabled = false
        }
    })

    private fun subscribeLabels() = uploadPackViewModel.labels.observe(this, Observer {
        when (val response = it) {
            is UploadPackViewModel.LabelsViewState.Success -> {
                isReady = true
                labels = response.labels
                val languages = labels.getLanguages()
                resume.text = getString(
                    R.string.uploadpack__resume,
                    labels.size,
                    labels.countApps(),
                    languages.size,
                    languages.toHumanReadable()
                )
                send.apply {
                    contentDescription = getString(R.string.uploadpack__action_send)
                    isEnabled = true
                    setOnClickListener {
                        send.isEnabled = false
                        contentDescription = getString(R.string.viewstate__loading_title)
                        result.apply {
                            text = getString(R.string.uploadpack__result_processing)
                            visibility = View.VISIBLE
                        }
                        uploadPackViewModel.processPacks(labels)
                    }
                }
            }
            is UploadPackViewModel.LabelsViewState.Empty -> setErrorState(getString(R.string.uploadpack__resume_empty))
            is UploadPackViewModel.LabelsViewState.Error -> setErrorState(response.errorMessage)
        }
    })

    private fun subscribeProcessResult() = uploadPackViewModel.results.observe(this, Observer {
        when (val response = it) {
            is UploadPackViewModel.ProcessPacksViewState.Success -> {
                result.text = getString(
                    R.string.uploadpack__result_ok,
                    response.result.createdOrUpdated,
                    response.result.skipedOrDuplicated,
                    response.result.withErrors
                )
                send.apply {
                    setImageResource(R.drawable.ic_done_black_24dp)
                    contentDescription = getString(R.string.uploadpack__action_done)
                    isEnabled = true
                    setOnClickListener {
                        finishAffinity()
                    }
                }
            }
            is UploadPackViewModel.ProcessPacksViewState.Error -> setErrorOnProcessState(response.errorMessage)
        }
    })

    private fun processIntent() {
        when {
            intent?.action == Intent.ACTION_SEND -> if (intent.hasExtra(Intent.EXTRA_STREAM)) {
                if ("application/json" == intent.type) {
                    intent.extras?.get(Intent.EXTRA_STREAM)?.toString()?.also { fileUri ->
                        uploadPackViewModel.processLabelsUri(Uri.parse(fileUri))
                    }
                }
            }
        }
    }

    private fun setErrorState(errorMessage: String) {
        resume.text = errorMessage
        send.apply {
            contentDescription = getString(R.string.uploadpack__action_retry)
            isEnabled = true
            setOnClickListener {
                send.isEnabled = false
                contentDescription = getString(R.string.viewstate__loading_title)
                processIntent()
            }
        }
    }

    private fun setErrorOnProcessState(errorMessage: String) {
        result.text = errorMessage
        send.apply {
            contentDescription = getString(R.string.uploadpack__action_retry)
            setOnClickListener {
                send.isEnabled = false
                contentDescription = getString(R.string.viewstate__loading_title)
                uploadPackViewModel.processPacks(labels)
            }
        }
    }

    private fun loadAds() {
        adsViewModel.getBannerAd(adBanner, object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                if (isReady.not()) {
                    processIntent()
                }
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                super.onAdFailedToLoad(error)
                resume.text = getString(
                    R.string.ads__current_status,
                    getString(R.string.ads__ads_blocker_or_internet_error)
                )
                result.apply {
                    visibility = View.VISIBLE
                    text = error.message
                }
                send.isEnabled = false
            }
        })
    }
}
