package com.pbaltazar.blindo.ui.user.profile.publicprofile

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.blindoapp.uitools.fragment.SectionsPagerAdapter
import com.pbaltazar.blindo.ui.user.profile.publicprofile.pages.packs.UserPacksFragment
import com.pbaltazar.blindo.ui.user.profile.publicprofile.pages.ratings.UserRatingsFragment

class UserProfilePagerAdapter(
    context: Context,
    fragmentManager: FragmentManager,
    private val titles: List<Int>
) : SectionsPagerAdapter(
    context,
    titles.size,
    fragmentManager
) {

    override fun titleResourceIdResolver(position: Int): Int = titles[position]

    override fun fragmentResolver(position: Int): Fragment = when (position) {
        0 -> UserPacksFragment()
        1 -> UserRatingsFragment()
        else -> throw RuntimeException("Unexpected tab index")
    }
}
