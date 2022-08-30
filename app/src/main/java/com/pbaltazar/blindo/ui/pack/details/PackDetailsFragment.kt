package com.pbaltazar.blindo.ui.pack.details

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.blindo.apollito.utils.extensions.toTimeAgo
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.material.snackbar.Snackbar
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentPackDetailsBinding
import com.pbaltazar.blindo.entities.InstallablePack
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.entities.responses.AdsResponse
import com.pbaltazar.blindo.graphql.type.SupportedScreenreadersEnum
import com.pbaltazar.blindo.utils.ads.AdsManager
import com.pbaltazar.blindo.utils.ads.ui.AdsViewModel
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import com.pbaltazar.blindo.utils.constants.AUTH_CANCELED_ON_DIALOG
import com.pbaltazar.blindo.utils.constants.INTERTITIAL_ADS_MINIMUM_VISUALIZATION
import com.pbaltazar.blindo.utils.extensions.getTalkbackInstallableFileUri
import com.pbaltazar.blindo.utils.extensions.installTalkbackPack
import com.pbaltazar.blindo.utils.extensions.isNullOrEmptyOrBlank
import com.pbaltazar.blindo.utils.extensions.saveTalkbackInstallableFile
import com.pbaltazar.blindo.utils.log.BlindoLogger
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class PackDetailsFragment : AuthenticableFragment<FragmentPackDetailsBinding>() {

    private val packDetailsViewModel: PackDetailsViewModel by viewModel()
    private val adsViewModel: AdsViewModel by sharedViewModel()
    private val packDetailsFragmentArgs: PackDetailsFragmentArgs by navArgs()

    private lateinit var userPhoto: ImageView
    private lateinit var authorInfo: TextView
    private lateinit var dateInfo: TextView
    private lateinit var downloadInfo: TextView
    private lateinit var labelsInfo: TextView
    private lateinit var translateCheckBox: CheckBox
    private lateinit var translateCheckLabel: TextView
    private lateinit var installPack: ImageButton

    private lateinit var packFile: Uri
    private var translateTo: Boolean = false
    private var language: String = ""
    set(value) {
        field = value
        translateCheckLabel.text = getString(
            R.string.packdetails__translate_to,
            Locale.Builder().setLanguage(value).build().displayLanguage
        )
    }

    private var interstitialAd: InterstitialAd? = null
    private var installPressedAt: Long = 0L

    private lateinit var adMessage: Snackbar

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        packDetailsViewModel.setTargetPack(packDetailsFragmentArgs.pack)
        subscribeUser()
        subscribeAdsConsentStatus()
        subscribeInterstitialAd()
        subscribeDownload()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            packDetailsFragmentArgs.pack.app?.also { app ->
                title = app.packageLabel
                subtitle = if (app.category.isNullOrEmptyOrBlank().not())
                    app.category
                else
                    app.packageName
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPackDetailsBinding.inflate(inflater, container, false)
        userPhoto = binding!!.userPhoto
        authorInfo = binding!!.authorInfo
        dateInfo = binding!!.dateInfo
        downloadInfo = binding!!.downloadInfo
        labelsInfo = binding!!.labelsInfo
        translateCheckBox = binding!!.translateCheckBox
        translateCheckLabel = binding!!.translateCheckLabel
        installPack = binding!!.installPack
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeAuth()
        setupUi()
    }

    private fun subscribeAuth() = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
        AUTH_CANCELED_ON_DIALOG)?.observe(viewLifecycleOwner) {
        if (it.not()) {
            launchLoginScreen()
        }
    }

    override fun onSubscribeUser(user: User?) {
        installPack.isEnabled = true
        user?.also { currentUser ->
            if (packDetailsFragmentArgs.pack.language == language) {
                translateCheckBox.isEnabled = false
            } else {
                translateCheckBox.apply {
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            if (currentUser.isPremium) {
                                translateTo = true
                            } else {
                                translateTo= false
                                translateCheckBox.isChecked = false
                                findNavController().navigate(
                                    PackDetailsFragmentDirections.actionFromPackDetailsToRequiresPremium()
                                )
                            }
                        } else {
                            translateTo = false
                        }
                    }
                    isEnabled = true
                }
            }
            if (currentUser.isPremium.not()) {
                adsViewModel.updateAdsConsentStatus()
            }
            installPack.setOnClickListener {
                if (adMessage.isShown) {
                    adMessage.dismiss()
                }
                val pack = packDetailsViewModel.getTargetPack() ?: packDetailsFragmentArgs.pack
                pack.getTalkbackInstallableFileUri(requireContext())?.also { packFile ->
                    this.packFile = packFile
                    installPack()
                } ?: run {
                    installPack.apply {
                        contentDescription = getString(R.string.viewstate__loading_title)
                        isEnabled = false
                    }
                    packDetailsViewModel.downloadPack(
                        InstallablePack(
                            pack = pack,
                            targetScreenreaders = SupportedScreenreadersEnum.TALKBACK,
                            translateTo = if (translateTo)
                                language
                            else
                                null
                        )
                    )
                }
            }
        } ?: installPack.setOnClickListener {
            findNavController().navigate(
                PackDetailsFragmentDirections.actionFromPackDetailsToRequiresAuth()
            )
        }
    }

    private fun subscribeDownload() = packDetailsViewModel.installablePack.observe(this) {
        installPack.apply {
            contentDescription = getString(R.string.packdetails__install_button)
            isEnabled = true
        }
        when (val response = it) {
            is PackDetailsViewModel.DownloadPackViewState.Success -> {
                response.installablePack.saveTalkbackInstallableFile(requireContext())?.also { packFile ->
                    this.packFile = packFile
                    installPack()
                }
            }
            is PackDetailsViewModel.DownloadPackViewState.Error -> {
                BlindoLogger.e(response.errorMessage)
                Snackbar.make(
                    installPack,
                    response.errorMessage,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupUi() {
        packDetailsViewModel.getTargetPack()?.also { pack ->
            Glide.with(requireContext())
                .load(pack.user?.picture)
                .placeholder(R.mipmap.default_user_picture)
                .centerCrop()
                .into(userPhoto)
            authorInfo.apply {
                text = pack.user?.name ?: getString(R.string.appcomment_unknown_author)
                pack.user?.also { user ->
                    setOnClickListener {
                        this@PackDetailsFragment.findNavController().navigate(
                            PackDetailsFragmentDirections.actionFromPackDetailsToPublicUserProfile(user)
                        )
                    }
                }
            }

            dateInfo.text = getString(
                R.string.packdetails__date_info,
                pack.createdAt.toTimeAgo(),
                pack.updatedAt?.toTimeAgo() ?: getString(R.string.appcomment_unknown_author)
            )
            downloadInfo.text = resources.getQuantityString(
                R.plurals.packdetails__download_info,
                pack.downloads.toInt(),
                pack.downloads
            )
            labelsInfo.text = resources.getQuantityString(
                R.plurals.packdetails__labels_info,
                pack.numberOfLabels,
                pack.numberOfLabels,
                pack.language.takeUnless { it.isNullOrEmptyOrBlank() }?.let { lan ->
                    Locale.Builder().setLanguage(lan).build().displayLanguage
                } ?: getString(R.string.appcomment_unknown_author)
            )
        }
        adMessage = Snackbar.make(
            installPack,
            getString(
                R.string.packdetails__ad_minimum_visualization,
                (INTERTITIAL_ADS_MINIMUM_VISUALIZATION / 1000).toInt()
            ),
            Snackbar.LENGTH_INDEFINITE
        ).setAction(
            getString(R.string.packdetails__get_premium)
        ) {
            findNavController().navigate(
                PackDetailsFragmentDirections.actionFromPackDetailsToPremium()
            )
        }
        installPack.isEnabled = false
        language = Locale.getDefault().language
        translateCheckBox.isEnabled = true
    }

    private fun subscribeAdsConsentStatus() = adsViewModel.adsConsentStatus.observe(this) {
        when (it) {
            is AdsResponse.Success -> when (val status = it.data) {
                AdsManager.Companion.ConsentStatus.UNKNOWN, AdsManager.Companion.ConsentStatus.ADS_FREE -> findNavController().navigate(
                    PackDetailsFragmentDirections.actionFromPackDetailsToAdsSettings(status.name, false)
                )
                else -> loadInterstitialAd()
            }
            is AdsResponse.Error -> findNavController().navigate(
                PackDetailsFragmentDirections.actionFromPackDetailsToAdsSettings(
                    AdsManager.Companion.ConsentStatus.INTERNET_ERROR_OR_ADS_BLOCKER.name,
                    false
                )
            )
        }
    }

    private fun subscribeInterstitialAd() = adsViewModel.interstitialAd.observe(this) {
        interstitialAd = it
    }

    private fun loadInterstitialAd() {
        adsViewModel.getInterstitialAd(requireContext(), object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                super.onAdShowedFullScreenContent()
                installPressedAt = System.currentTimeMillis()
            }

            override fun onAdDismissedFullScreenContent() {
                super.onAdDismissedFullScreenContent()
                interstitialAd = null
                loadInterstitialAd()
                if (System.currentTimeMillis() - installPressedAt < INTERTITIAL_ADS_MINIMUM_VISUALIZATION) {
                    adMessage.show()
                } else {
                    packFile.installTalkbackPack(installPack)
                }
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                super.onAdFailedToShowFullScreenContent(error)
                findNavController().navigate(
                    PackDetailsFragmentDirections.actionFromPackDetailsToAdsSettings(AdsManager.Companion.ConsentStatus.INTERNET_ERROR_OR_ADS_BLOCKER.name, false)
                )
            }
        })
    }

    private fun installPack() {
        if (getUser()?.isPremium == true) {
            packFile.installTalkbackPack(installPack)
        } else {
            interstitialAd?.show(requireActivity()) ?: findNavController().navigate(
                PackDetailsFragmentDirections.actionFromPackDetailsToAdsSettings(AdsManager.Companion.ConsentStatus.INTERNET_ERROR_OR_ADS_BLOCKER.name, false)
            )
        }
    }
}
