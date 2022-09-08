package com.pbaltazar.blindo.utils.core.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.viewbinding.ViewBinding
import com.pbaltazar.blindo.R

abstract class BlindoFragment<VB: ViewBinding> : Fragment(),
    MenuProvider {

    protected var binding: VB? = null

    @MenuRes
    var menuResId: Int? = null

    abstract val isSearchable: Boolean

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MenuHost).addMenuProvider(
            this,
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuResId?.also {
            menuInflater.inflate(it, menu)
        }
        menu.findItem(R.id.searchApps).isVisible = isSearchable
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = true

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }
}
