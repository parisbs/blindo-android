package com.pbaltazar.blindo.ui.filter

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.pbaltazar.blindo.MainNavigationDirections
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.utils.constants.ARGUMENT_REQUIRE_REFRESH_FILTERS
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment

abstract class FilterableFragment<VB : ViewBinding> : BlindoFragment<VB>() {

    abstract val filtersSet: FiltersSet

    abstract fun onFiltersChange(isChanged: Boolean)

    override fun getMenuResId(): Int = R.menu.filterable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribeFilters()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filter -> {
                findNavController().navigate(
                    MainNavigationDirections.actionGlobalToFilters(filtersSet)
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun subscribeFilters() = findNavController().currentBackStackEntry?.savedStateHandle
        ?.getLiveData<Boolean>(ARGUMENT_REQUIRE_REFRESH_FILTERS)?.observe(this, Observer {
            onFiltersChange(it)
        })
}
