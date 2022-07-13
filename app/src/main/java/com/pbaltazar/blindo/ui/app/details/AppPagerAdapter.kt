package com.pbaltazar.blindo.ui.app.details

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pbaltazar.blindo.ui.app.details.pages.packs.AppPacksFragment
import com.pbaltazar.blindo.ui.app.details.pages.ratings.AppRatingsFragment
import com.pbaltazar.blindo.ui.app.details.pages.statistics.AppStatisticsFragment

class AppPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> AppStatisticsFragment()
        1 -> AppPacksFragment()
        2 -> AppRatingsFragment()
        else -> throw RuntimeException("Unexpected tab index")
    }
}
