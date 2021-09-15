package com.pbaltazar.blindo.utils.authentication.ui

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.TextView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.pbaltazar.blindo.BuildConfig
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.ActivityLoginBinding
import com.pbaltazar.blindo.entities.Device
import com.pbaltazar.blindo.utils.constants.TERMS_AND_CONDITIONS_LINK
import com.pbaltazar.blindo.utils.extensions.toUiFormat
import timber.log.Timber
import java.util.*

class LoginActivity : AuthenticableActivity() {

    private var binding: ActivityLoginBinding? = null

    private val signInScreen = registerForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> authenticateUser()
            else -> result.idpResponse?.also { response ->
                setStepAutenticationErrors(
                    response.error?.localizedMessage ?: getString(R.string.auth__error_authentication)
                )
            } ?: setStepAutenticationErrors(getString(R.string.auth__error_authentication))
        }
    }

    private lateinit var authStep: TextView
    private lateinit var authNotice: TextView
    private lateinit var authSkipButton: Button
    private lateinit var authActionButton: Button

    private var currentStep: Int = 0
    private var currentStepText: String = ""
    set(value) {
        field = value
        authStep.text = field
    }
    private var currentStepNotice: String = ""
    set(value) {
        field = value
        authNotice.text = field
    }
    private var currentButtonLabel: String = ""
    set(value) {
        field = value
        authActionButton.text = field
    }

    private val PROVIDERS = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().setRequireName(true).build(),
        AuthUI.IdpConfig.GoogleBuilder().build(),
        AuthUI.IdpConfig.FacebookBuilder().build(),
        AuthUI.IdpConfig.TwitterBuilder().build()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        authStep = binding!!.authStep
        authNotice = binding!!.authNotice
        authSkipButton = binding!!.authSkipButton
        authActionButton = binding!!.authActionButton

        setupUi()
        subscribeUser()
        subscribeAuthentication()
        subscribeDeviceAuthentication()
    }

    override fun onDestroy() {
        super.onDestroy()
        signInScreen.unregister()
        binding = null
    }

    override fun onSubscribeUser() {
        if (currentStep == 1) {
            getUser()?.also { user ->
                if (user.isVerified) {
                    setStepCongrats()
                } else {
                    currentStepNotice = getString(
                        R.string.auth__step_verify_account_unverified_yet,
                        Date(System.currentTimeMillis()).toUiFormat()
                    )
                    authNotice.apply {
                        requestFocus()
                        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
                    }
                }
            }
        }
    }

    override fun onSubscribeAuthentication(userAuthentication: AuthenticationViewModel.UserAuthentication) {
        when (val response = userAuthentication) {
            is AuthenticationViewModel.UserAuthentication.Success -> response.user.also {
                setStepLoading()
                authenticateDevice(
                    Device(
                        hardwareFingerprint = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID),
                        name = if (Build.MODEL.lowercase().startsWith(Build.MANUFACTURER.lowercase()))
                            Build.MODEL.substring(Build.MANUFACTURER.length).let { model ->
                                "${Build.MANUFACTURER.split("_").mapNotNull { manufacturerPart ->
                                    manufacturerPart.lowercase().replaceFirstChar { it.uppercase() }
                                }.joinToString(" ")} ${model.replaceFirst(Regex("^[-_\\s]"), "")}"
                            }
                        else
                            "${Build.MANUFACTURER.split("_").mapNotNull { manufacturerPart ->
                                manufacturerPart.lowercase().replaceFirstChar { it.uppercase() }
                            }.joinToString(" ")} ${Build.MODEL}",
                        language = Locale.getDefault().language,
                        country = Locale.getDefault().country
                    )
                )
            }
            is AuthenticationViewModel.UserAuthentication.BadRequest -> setStepAutenticationErrors(response.errors.joinToString(", "))
            is AuthenticationViewModel.UserAuthentication.NetworkEror -> setStepAutenticationErrors(response.throwable.localizedMessage ?: response.throwable.toString())
            else -> setStepAutenticationErrors("Unable to sign in")
        }
    }

    override fun onSubscribeUserUpdate(userUpdate: AuthenticationViewModel.UserUpdate) {
        // Not required
    }

    override fun onIsValidationEmailSent(isValidationEmailSent: Boolean) {
        // Not required
    }

    override fun onSubscribeDevice() {
        // Not required
    }

    override fun onSubscribeDeviceAuthentication(deviceAuthentication: AuthenticationViewModel.DeviceAuthentication) {
        when (val response = deviceAuthentication) {
            is AuthenticationViewModel.DeviceAuthentication.Success -> {
                getUser()?.also { authenticatedUser ->
                    if (authenticatedUser.isVerified) {
                        setStepCongrats()
                    } else {
                        sendVerificationEmail()
                        setStepEmailVerification()
                    }
                }
            }
            is AuthenticationViewModel.DeviceAuthentication.BadRequest -> setStepRegisterDeviceError(response.errors.joinToString(", "))
            is AuthenticationViewModel.DeviceAuthentication.NetworkError -> setStepRegisterDeviceError(response.throwable.localizedMessage ?: response.throwable.toString())
            else -> setStepRegisterDeviceError("Unable to register your device")
        }
    }

    override fun onSubscribeDeviceUpdate(deviceUpdate: AuthenticationViewModel.DeviceUpdate) {
        // Not required
    }

    private fun setStepLoading() {
        currentStepText = getString(R.string.viewstate__loading_title)
        currentStepNotice = ""
        currentButtonLabel = getString(R.string.auth__continue_button)
        authActionButton.isEnabled = false
        authStep.apply {
            requestFocus()
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }
    }

    private fun setStepAutenticationErrors(errorMessage: String) {
        currentStepText = getString(R.string.auth__error_authentication)
        currentStepNotice = errorMessage
        currentButtonLabel = getString(R.string.auth__retry_button)
        authActionButton.isEnabled = true
        currentStep = 0
        authStep.apply {
            requestFocus()
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }
    }

    private fun setStepRegisterDeviceError(errorMessage: String) {
        Timber.e(errorMessage)
        signOut(false)
        currentStepText = getString(R.string.auth__device_error)
        currentStepNotice = errorMessage
        currentButtonLabel = getString(R.string.auth__cancel_button)
        authActionButton.isEnabled = true
        currentStep = 0
        authStep.apply {
            requestFocus()
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }
    }

    private fun setStepEmailVerification() {
        currentStepText = getString(R.string.auth__step_verify_account)
        currentStepNotice = getString(R.string.auth__step_verify_account_notice)
        authSkipButton.visibility = View.VISIBLE
        currentButtonLabel = getString(R.string.auth__continue_button)
        authActionButton.isEnabled = true
        currentStep = 1
        authStep.apply {
            requestFocus()
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }
    }

    private fun setStepCongrats() {
        currentStepNotice = ""
        currentStepText = getString(
            R.string.auth__step_congrats,
            getUser()?.name ?: ""
        )
        authSkipButton.visibility = View.INVISIBLE
        currentButtonLabel = getString(R.string.auth__continue_button)
        authActionButton.isEnabled = true
        currentStep = 2
        authStep.apply {
            requestFocus()
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }
    }

    private fun launchSignIn() {
        setStepLoading()
        signInScreen.launch(AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setLogo(R.mipmap.ic_launcher)
            .setTosAndPrivacyPolicyUrls(TERMS_AND_CONDITIONS_LINK, getString(R.string.privacy_policy_link))
            .setAvailableProviders(PROVIDERS)
            .setIsSmartLockEnabled(BuildConfig.DEBUG.not())
            .build())
    }

    private fun setupUi() {
        authSkipButton.setOnClickListener {
            setStepCongrats()
        }
        authActionButton.setOnClickListener {
            when (currentStep) {
                0 -> launchSignIn()
                1 -> propagateVerifiedStatus()
                2 -> {
                    getUser()?.also { user ->
                        setResult(Activity.RESULT_OK, Intent().apply {
                            putExtra(AuthenticationContract.EXTRA_SIGNED_USER, user)
                        })
                    } ?: setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        }
    }
}
