package com.pbaltazar.blindo.ui.filter

import android.os.Bundle
import android.view.MenuItem
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.pbaltazar.blindo.MainNavigationDirections
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.utils.constants.ARGUMENT_REQUIRE_REFRESH_FILTERS
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment

abstract class FilterableFragment<VB : ViewBinding> : BlindoFragment<VB>() {

    abstract val filtersSet: FiltersSet

    abstract fun onFiltersChange(isChanged: Boolean)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menuResId = R.menu.filterable
        subscribeFilters()
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = when (menuItem.itemId) {
        R.id.filter -> {
            findNavController().navigate(
                MainNavigationDirections.actionGlobalToFilters(filtersSet)
            )
            true
        }
        else -> super.onMenuItemSelected(menuItem)
    }

    private fun subscribeFilters() = findNavController().currentBackStackEntry?.savedStateHandle
        ?.getLiveData<Boolean>(ARGUMENT_REQUIRE_REFRESH_FILTERS)?.observe(this) {
            onFiltersChange(it)
        }
}
