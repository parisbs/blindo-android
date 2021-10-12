package com.pbaltazar.blindo.ui.filter

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.pbaltazar.blindo.MainNavigationDirections
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.utils.constants.ARGUMENT_REQUIRE_REFRESH_FILTERS

abstract class FilterableFragment : Fragment() {

    abstract val filtersSet: FiltersSet

    abstract fun onFiltersChange(isChanged: Boolean)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        subscribeFilters()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.filterable, menu)
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
