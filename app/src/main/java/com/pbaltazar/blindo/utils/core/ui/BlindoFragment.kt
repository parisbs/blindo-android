package com.pbaltazar.blindo.utils.core.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.pbaltazar.blindo.R

abstract class BlindoFragment<VB: ViewBinding> : Fragment(),
    MenuInflator {

    protected var binding: VB? = null

    abstract val isSearchable: Boolean

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (hasMenuRes()) {
            inflater.inflate(getMenuResId(), menu)
        }
        menu.findItem(R.id.searchApps).isVisible = isSearchable
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    fun hasMenuRes(): Boolean =
        getMenuResId() != View.NO_ID
    }
