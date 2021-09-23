package com.pbaltazar.blindo.ui.sli

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentSliBinding
import com.pbaltazar.blindo.entities.Pack
import com.pbaltazar.blindo.graphql.type.SupportedScreenreadersEnum
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticationViewModel
import com.pbaltazar.blindo.utils.constants.AUTH_CANCELED_ON_DIALOG
import com.pbaltazar.blindo.utils.extensions.countApps
import com.pbaltazar.blindo.utils.extensions.countLabels
import com.pbaltazar.blindo.utils.extensions.installTalkbackPack
import com.pbaltazar.blindo.utils.extensions.saveTalkbackInstallableFile
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class SliFragment : AuthenticableFragment() {

    private val sliViewModel: SliViewModel by viewModel()
    private var binding: FragmentSliBinding? = null

    private lateinit var preferUserLabels: Switch
    private lateinit var translateLabel: TextView
    private lateinit var translate: Switch
    private lateinit var sliInfo: TextView
    private lateinit var sliAction: Button

    private lateinit var apps: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeUser()
        subscribeInstalledApps()
        subscribeInstallablePack()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSliBinding.inflate(inflater, container, false)
        preferUserLabels = binding!!.preferUserLabels
        translateLabel = binding!!.translateLabel
        translate = binding!!.translate
        sliInfo = binding!!.sliInfo
        sliAction = binding!!.sliAction
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeAuth()
        setupUi()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onSubscribeUser() {
        if (getUser() == null) {
            findNavController().navigate(
                SliFragmentDirections.actionFromSliToRequiresAuth()
            )
        }
    }

    private fun subscribeAuth() = findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
        AUTH_CANCELED_ON_DIALOG)?.observe(this, Observer {
        if (it.not()) {
            loginScreen.launch(Unit)
        } else {
            findNavController().popBackStack()
        }
    })

    private fun subscribeInstalledApps() = sliViewModel.apps.observe(this, Observer {
        when (val response = it) {
            is SliViewModel.LocalApps.Success -> {
                apps = response.apps
                sliInfo.text = getString(
                    R.string.sli__prepared_apps,
                    apps.size,
                    Locale.getDefault().displayLanguage,
                    if (preferUserLabels.isChecked)
                        getString(R.string.sli__prefering_backup)
                    else
                        getString(R.string.sli__not_prefer_backup),
                    if (translate.isChecked)
                        getString(R.string.sli__requesting_translation)
                    else
                        getString(R.string.sli__not_translation)
                )
                sliAction.apply {
                    text = getString(R.string.sli__action_continue)
                    isEnabled = true
                    setOnClickListener {
                        sliAction.apply {
                            text = getString(R.string.viewstate__loading_title)
                            isEnabled = false
                        }
                        sliViewModel.launchSli(
                            apps,
                            Locale.getDefault().language,
                            SupportedScreenreadersEnum.TALKBACK,
                            preferUserLabels.isChecked,
                            translate.isChecked
                        )
                    }
                }
            }
            is SliViewModel.LocalApps.Empty -> {
                sliInfo.text = getString(R.string.sli__empty_local_apps)
                sliAction.apply {
                    text = getString(R.string.sli__action_retry)
                    isEnabled = true
                    setOnClickListener {
                        sliViewModel.getInstalledApps()
                    }
                }
            }
            is SliViewModel.LocalApps.Error -> {
                sliInfo.text = response.errorMessage
                sliAction.isEnabled = false
            }
        }
    })

    private fun subscribeInstallablePack() = sliViewModel.installable.observe(this, Observer {
        when (val response = it) {
            is SliViewModel.SliResult.Success -> {
                response.installablePack.copy(
                    pack = Pack(
                        hash = response.installablePack.installable.hashCode().toString()
                    )
                )
                    .saveTalkbackInstallableFile(requireContext())?.also { installablePack ->
                        sliInfo.text = getString(
                            R.string.sli__sli_result,
                            response.installablePack.countLabels(),
                            Locale.Builder().setLanguage(
                                response.installablePack.translateTo
                            ).build().displayName,
                            response.installablePack.countApps()
                        )
                        sliAction.apply {
                            text = getString(R.string.sli__action_install)
                            isEnabled = true
                            setOnClickListener {
                                installablePack.installTalkbackPack(sliAction)
                            }
                        }
                    } ?: run {
                    sliInfo.text = getString(R.string.sli__unable_to_save)
                    sliAction.apply {
                        text = getString(R.string.sli__action_install)
                        isEnabled = false
                    }
                }
            }
            is SliViewModel.SliResult.Empty -> {
                sliInfo.text = getString(R.string.sli__empty_sli_result)
                sliAction.apply {
                    text = getString(R.string.sli__action_install)
                    isEnabled = false
                }
            }
            is SliViewModel.SliResult.Error -> {
                sliInfo.text = response.errorMessage
                sliAction.apply {
                    text = getString(R.string.sli__action_install)
                    isEnabled = false
                }
            }
        }
    })

    private fun setupUi() {
        if (Build.VERSION.SDK_INT >= 22) {
            preferUserLabels.accessibilityTraversalAfter = R.id.preferUserLabelsLabel
            translateLabel.accessibilityTraversalAfter = R.id.preferUserLabels
            translate.accessibilityTraversalAfter = R.id.translateLabel
        }
        sliAction.setOnClickListener {
            getUser()?.also { user ->
                if (user.isPremium) {
                    sliAction.apply {
                        text = getString(R.string.viewstate__loading_title)
                        isEnabled = false
                    }
                    preferUserLabels.isEnabled = false
                    translate.isEnabled = false
                    sliViewModel.getInstalledApps()
                } else {
                    findNavController().navigate(
                        SliFragmentDirections.actionFromSliToRequiresPremium()
                    )
                }
            } ?: findNavController().navigate(
                SliFragmentDirections.actionFromSliToRequiresAuth()
            )
        }
    }
}
