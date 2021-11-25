package com.pbaltazar.blindo.ui.user.profile.publicprofile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.navArgs
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentUserProfileBinding
import com.pbaltazar.blindo.ui.user.profile.publicprofile.pages.TabsCounterBadgeListener
import com.pbaltazar.blindo.ui.user.profile.publicprofile.pages.UserProfilePagerHelper
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserProfileFragment : BlindoFragment<FragmentUserProfileBinding>() {

    private val userProfileViewModel: UserProfileViewModel by viewModel()
    private val userProfileFragmentArgs: UserProfileFragmentArgs by navArgs()

    override val isSearchable: Boolean
        get() = false

    private lateinit var userProfileViewPager: ViewPager
    private lateinit var userProfileTabs: TabLayout

    private lateinit var userProfilePagerAdapter: UserProfilePagerAdapter

    private val titles: List<Int> = listOf(
        R.string.appdetails__packs_tab,
        R.string.appdetails__comments_tab
    )

    private val tabsCounterBadgeListener: TabsCounterBadgeListener = object : TabsCounterBadgeListener {
        override fun updatePacksCounterBadge(count: Int) =
            refreshCounterBadges(0, count)

        override fun updateRatingsCounterBadge(count: Int) =
            refreshCounterBadges(1, count)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userProfileViewModel.setUser(userProfileFragmentArgs.user)
        UserProfilePagerHelper.userProfileViewModel = userProfileViewModel
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = userProfileFragmentArgs.user.name
            subtitle = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        userProfileViewPager = binding!!.userProfileViewPager
        userProfileTabs = binding!!.userProfileTabs
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        UserProfilePagerHelper.tabsCounterBadgeListener = tabsCounterBadgeListener

        setupUi()
    }

    private fun refreshCounterBadges(tabIndex: Int, count: Int) {
        if (this@UserProfileFragment::userProfileTabs.isInitialized) {
            userProfileTabs.getTabAt(tabIndex)?.text = getString(
                titles.get(tabIndex),
                count
            )
        }
    }

    private fun setupUi() {
        userProfilePagerAdapter = UserProfilePagerAdapter(requireContext(), childFragmentManager, titles)

        userProfileTabs.apply {
            setupWithViewPager(
                userProfileViewPager.apply {
                    adapter = userProfilePagerAdapter
                }
            )

            for (i in 0 until (titles.size - 1)) {
                getTabAt(i)?.also {
                    ViewCompat.setAccessibilityLiveRegion(it.view, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE)
                }
            }
        }
    }
}
