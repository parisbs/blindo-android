package com.pbaltazar.blindo.ui.tutorial

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.*
import androidx.core.text.HtmlCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentTutorialBinding
import com.pbaltazar.blindo.entities.User
import com.pbaltazar.blindo.utils.accessibility.AccessibilityCapabilities
import com.pbaltazar.blindo.utils.analytics.AnalyticsManager
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import com.pbaltazar.blindo.utils.constants.TERMS_AND_CONDITIONS_LINK
import com.pbaltazar.blindo.utils.extensions.gone
import com.pbaltazar.blindo.utils.extensions.invisible
import com.pbaltazar.blindo.utils.extensions.visible
import org.koin.androidx.viewmodel.ext.android.viewModel

class TutorialFragment : AuthenticableFragment<FragmentTutorialBinding>() {

    private val tutorialViewModel: TutorialViewModel by viewModel()
    private val tutorialFragmentArgs: TutorialFragmentArgs by navArgs()

    private lateinit var stepInfo: TextView
    private lateinit var dataList: ListView
    private lateinit var checkboxContainer: LinearLayout
    private lateinit var checkBox: CheckBox
    private lateinit var checkboxText: TextView
    private lateinit var omitButton: Button
    private lateinit var nextButton: Button

    private var isPrivacyPolicyAccepted: Boolean = false
    private var currentStep: Int = 0

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribePrivacyPolicy()
        subscribeUser()
        subscribeStep()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentTutorialBinding.inflate(inflater, container, false)
        stepInfo = binding!!.stepInfo
        dataList = binding!!.dataList
        checkboxContainer = binding!!.checkboxContainer
        checkBox = binding!!.checkBox
        checkboxText = binding!!.checkboxText
        omitButton = binding!!.omitButton
        nextButton = binding!!.nextButton
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AnalyticsManager.registerEventWithoutParams(FirebaseAnalytics.Event.TUTORIAL_BEGIN)
        tutorialViewModel.verifyIsPrivacyPolicyAccepted()
        omitButton.setOnClickListener {
            tutorialViewModel.setStep(5)
        }
        tutorialViewModel.setStep(tutorialFragmentArgs.step)
    }

    override fun onResume() {
        super.onResume()
        when (currentStep) {
            7 -> if (AccessibilityCapabilities.isBlindoVisionEnabled()) {
                tutorialViewModel.setStep(currentStep + 1)
            }
            else -> Unit
        }
    }

    override fun onSubscribeUser(user: User?) {
        user?.also {
            if (currentStep == 9) {
                tutorialViewModel.setStep(currentStep + 1)
            }
        }
    }

    private fun subscribePrivacyPolicy() = tutorialViewModel.isPrivacyPolicyAccepted.observe(this) {
        isPrivacyPolicyAccepted = it
    }

    private fun subscribeStep() = tutorialViewModel.step.observe(this) {
        currentStep = it
        when (currentStep) {
            0 -> setStep(currentStep, R.string.tutorial__step_welcome)
            1 -> setStep(currentStep, R.string.tutorial__step_home)
            2 -> setStep(currentStep, R.string.tutorial__step_app_details)
            3 -> setStep(currentStep, R.string.tutorial__step_local_apps)
            4 -> setStep(currentStep, R.string.tutorial__step_sli)
            5 -> setStep(currentStep, R.string.vision__introduce)
            6 -> setStep(currentStep, R.string.vision__how_to_use)
            7 -> setStep(currentStep, R.string.vision__ready_to_enable, R.string.vision__enable_now)
            8 -> if (isPrivacyPolicyAccepted) {
                tutorialViewModel.setStep(currentStep + 1)
            } else {
                setStep(
                    currentStep,
                    R.string.tutorial__step_privacy_policy_terms_conditions,
                    R.string.tutorial__action_accept
                )
            }
            9 -> if (getUser() != null) {
                tutorialViewModel.setStep(currentStep + 1)
            } else {
                setStep(currentStep, R.string.tutorial__step_account, R.string.tutorial__action_sign_in)
            }
            10 -> getUser()?.also { user ->
                if (user.isPremium) {
                    setStep(currentStep + 1, R.string.tutorial__step_finish, R.string.tutorial__action_finish)
                } else {
                    setStep(currentStep, R.string.tutorial__step_premium, R.string.tutorial__action_get_premium)
                }
            } ?: setStep(currentStep + 1, R.string.tutorial__step_finish, R.string.tutorial__action_finish)
            11 -> setStep(currentStep, R.string.tutorial__step_finish, R.string.tutorial__action_finish)
        }
    }

    private fun setStep(step: Int, description: Int, label: Int? = null) {
        dataList.gone()
        checkboxContainer.gone()
        stepInfo.apply {
            text = getString(description)
            requestFocus()
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }
        omitButton.apply {
            if (TextUtils.equals(text, getString(R.string.tutorial__action_omit)).not()) {
                text = getString(R.string.tutorial__action_omit)
            }
            when (description) {
                R.string.vision__introduce -> setOnClickListener {
                    tutorialViewModel.setStep(8)
                }
                else -> Unit
            }
        }
        nextButton.apply {
            when (label) {
                R.string.vision__enable_now -> {
                    omitButton.apply {
                        text = getString(R.string.vision__later)
                        setOnClickListener {
                            tutorialViewModel.setStep(step + 1)
                        }
                    }
                    text = getString(label)
                    setOnClickListener {
                        Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            if (resolveActivity(requireActivity().packageManager) == null) {
                                nextButton.isEnabled = false
                                Snackbar.make(
                                    nextButton,
                                    getString(R.string.vision__unable_to_open_accessibility_settings),
                                    Snackbar.LENGTH_LONG
                                ).show()
                            } else {
                                startActivity(this)
                            }
                        }
                    }
                }
                R.string.tutorial__action_accept -> {
                    omitButton.gone()
                    checkboxText.apply {
                        movementMethod = LinkMovementMethod.getInstance()
                        text = HtmlCompat.fromHtml(
                            getString(
                                R.string.tutorial__action_accept_privacy_policy_terms_conditions,
                                getString(R.string.privacy_policy_link),
                                TERMS_AND_CONDITIONS_LINK
                            ),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    }
                    isEnabled = false
                    resources.getStringArray(R.array.tutorial__privacy_policy_terms_conditions_data).also {
                        ArrayAdapter<String>(
                            this@TutorialFragment.requireContext(),
                            R.layout.item_privacy_policy_data,
                            R.id.privacyText
                        ).also { arrayAdapter ->
                            arrayAdapter.addAll(it.toList())
                            dataList.apply {
                                visible()
                                adapter = arrayAdapter
                            }
                        }
                    }
                    checkboxContainer.visible()
                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        isEnabled = isChecked
                    }
                    text = getString(label)
                    setOnClickListener {
                        tutorialViewModel.setIsVisionIntroduced(true)
                        tutorialViewModel.acceptPrivacyPolicy()
                        tutorialViewModel.setStep(step + 1)
                    }
                }
                R.string.tutorial__action_sign_in -> {
                    text = getString(label)
                    setOnClickListener {
                        launchLoginScreen()
                    }
                    omitButton.apply {
                        visible()
                        setOnClickListener {
                            tutorialViewModel.setStep(11)
                        }
                    }
                }
                R.string.tutorial__action_get_premium -> {
                    text = getString(label)
                    setOnClickListener {
                        this@TutorialFragment.findNavController().navigate(
                            TutorialFragmentDirections.actionFromTutorialToPremium()
                        )
                    }
                    omitButton.apply {
                        visible()
                        setOnClickListener {
                            tutorialViewModel.setStep(11)
                        }
                    }
                }
                R.string.tutorial__action_finish -> {
                    tutorialViewModel.disableFirstRun()
                    omitButton.invisible()
                    text = getString(label)
                    setOnClickListener {
                        AnalyticsManager.registerEventWithoutParams(FirebaseAnalytics.Event.TUTORIAL_COMPLETE)
                        this@TutorialFragment.findNavController().navigate(
                            TutorialFragmentDirections.actionFromTutorialToHome()
                        )
                    }
                }
                else -> {
                    text = getString(R.string.tutorial__action_next)
                    setOnClickListener {
                        tutorialViewModel.setStep(step + 1)
                    }
                }
            }
        }
    }
}
