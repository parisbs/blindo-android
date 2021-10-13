package com.pbaltazar.blindo.utils.core.ui

import android.view.Menu
import android.view.MenuInflater
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.pbaltazar.blindo.R

abstract class BlindoFragment<VB: ViewBinding> : Fragment() {

    protected var binding: VB? = null

    abstract val isSearchable: Boolean

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.searchApps).setVisible(isSearchable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
