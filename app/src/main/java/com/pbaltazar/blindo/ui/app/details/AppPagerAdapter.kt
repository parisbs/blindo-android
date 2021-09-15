package com.pbaltazar.blindo.ui.app.details

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.blindoapp.uitools.fragment.SectionsPagerAdapter
import com.pbaltazar.blindo.ui.app.details.pages.packs.AppPacksFragment
import com.pbaltazar.blindo.ui.app.details.pages.ratings.AppRatingsFragment
import com.pbaltazar.blindo.ui.app.details.pages.statistics.AppStatisticsFragment
import com.pbaltazar.blindo.utils.constants.APP_DETAILS_TAB_TITLES

class AppPagerAdapter(
    context: Context,
    fragmentManager: FragmentManager
) : SectionsPagerAdapter    (
    context,
    APP_DETAILS_TAB_TITLES.size,
    fragmentManager
) {

    override fun titleResourceIdResolver(position: Int): Int = APP_DETAILS_TAB_TITLES.get(position)

    override fun fragmentResolver(position: Int): Fragment = when (position) {
        0 -> AppStatisticsFragment()
        1 -> AppPacksFragment()
        2 -> AppRatingsFragment()
        else -> throw RuntimeException("Unexpected tab index")
    }
}
