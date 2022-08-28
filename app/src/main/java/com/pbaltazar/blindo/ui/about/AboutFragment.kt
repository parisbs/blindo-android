package com.pbaltazar.blindo.ui.about

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import com.bumptech.glide.Glide
import com.pbaltazar.blindo.BuildConfig
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentAboutBinding
import com.pbaltazar.blindo.utils.constants.TERMS_AND_CONDITIONS_LINK
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment

class AboutFragment : BlindoFragment<FragmentAboutBinding>() {

    private lateinit var blindoIcon: ImageView
    private lateinit var blindoVersion: TextView
    private lateinit var termsConditionsPrivacyPolicy: TextView
    private lateinit var contactUs: TextView

    override val isSearchable: Boolean
        get() = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        blindoIcon = binding!!.blindoIcon
        blindoVersion = binding!!.blindoVersion
        termsConditionsPrivacyPolicy = binding!!.termsConditionsPrivacyPolicy
        contactUs = binding!!.contactUs
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
    }

    private fun setupUi() {
        Glide.with(requireContext())
            .load(R.mipmap.ic_launcher)
            .centerCrop()
            .into(blindoIcon)
        blindoVersion.text = getString(
            R.string.about__blindo_version,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )
        termsConditionsPrivacyPolicy.apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = HtmlCompat.fromHtml(
                getString(
                    R.string.about__terms_conditions_privacy_policy,
                    TERMS_AND_CONDITIONS_LINK,
                    getString(R.string.privacy_policy_link)
                ),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
        contactUs.apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = HtmlCompat.fromHtml(
                getString(R.string.about__contact_us),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }
    }
}
