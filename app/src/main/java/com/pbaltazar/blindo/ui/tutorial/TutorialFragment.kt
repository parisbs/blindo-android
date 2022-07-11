package com.pbaltazar.blindo.ui.tutorial

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.*
import androidx.core.text.HtmlCompat
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.analytics.FirebaseAnalytics
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentTutorialBinding
import com.pbaltazar.blindo.utils.analytics.AnalyticsManager
import com.pbaltazar.blindo.utils.authentication.ui.AuthenticableFragment
import com.pbaltazar.blindo.utils.constants.TERMS_AND_CONDITIONS_LINK
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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

    override fun onSubscribeUser() {
        getUser()?.also {
            if (currentStep == 7) {
                tutorialViewModel.setStep(currentStep + 1)
            }
        }
    }

    private fun subscribePrivacyPolicy() = tutorialViewModel.isPrivacyPolicyAccepted.observe(this, Observer {
        isPrivacyPolicyAccepted = it
    })

    private fun subscribeStep() = tutorialViewModel.step.observe(this, Observer {
        currentStep = it
        when (currentStep) {
            0 -> setStep(currentStep, R.string.tutorial__step_welcome)
            1 -> setStep(currentStep, R.string.tutorial__step_home)
            2 -> setStep(currentStep, R.string.tutorial__step_app_details)
            3 -> setStep(currentStep, R.string.tutorial__step_local_apps)
            4 -> setStep(currentStep, R.string.tutorial__step_sli)
            5 -> setStep(currentStep, R.string.vision__introduce)
            6 -> if (isPrivacyPolicyAccepted) {
                tutorialViewModel.setStep(currentStep + 1)
            } else {
                setStep(currentStep, R.string.tutorial__step_privacy_policy_terms_conditions, R.string.tutorial__action_accept)
            }
            7 -> if (getUser() != null) {
                tutorialViewModel.setStep(currentStep + 1)
            } else {
                setStep(currentStep, R.string.tutorial__step_account, R.string.tutorial__action_sign_in)
            }
            8 -> getUser()?.also { user ->
                if (user.isPremium) {
                    setStep(currentStep + 1, R.string.tutorial__step_finish, R.string.tutorial__action_finish)
                } else {
                    setStep(currentStep, R.string.tutorial__step_premium, R.string.tutorial__action_get_premium)
                }
            } ?: setStep(currentStep + 1, R.string.tutorial__step_finish, R.string.tutorial__action_finish)
            9 -> setStep(currentStep, R.string.tutorial__step_finish, R.string.tutorial__action_finish)
        }
    })

    private fun setStep(step: Int, description: Int, label: Int? = null) {
        dataList.visibility = View.GONE
        checkboxContainer.visibility = View.GONE
        stepInfo.apply {
            text = getString(description)
            requestFocus()
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }
        nextButton.apply {
            when (label) {
                R.string.tutorial__action_accept -> {
                    omitButton.visibility = View.GONE
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
                                visibility = View.VISIBLE
                                adapter = arrayAdapter
                            }
                        }
                    }
                    checkboxContainer.visibility = View.VISIBLE
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
                        loginScreen.launch(Unit)
                    }
                    omitButton.apply {
                        visibility = View.VISIBLE
                        setOnClickListener {
                            tutorialViewModel.setStep(9)
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
                        visibility = View.VISIBLE
                        setOnClickListener {
                            tutorialViewModel.setStep(9)
                        }
                    }
                }
                R.string.tutorial__action_finish -> {
                    tutorialViewModel.disableFirstRun()
                    omitButton.visibility = View.INVISIBLE
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
