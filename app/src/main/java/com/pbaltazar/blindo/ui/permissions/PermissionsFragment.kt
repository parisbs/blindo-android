package com.pbaltazar.blindo.ui.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import com.pbaltazar.blindo.databinding.FragmentPermissionsBinding
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import com.wizeline.viewstate.ViewState

class PermissionsFragment : BlindoFragment<FragmentPermissionsBinding>() {

    private val permissionsRequestCode: Int = 1234
    private val permissionsToGrant: Array<String> = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private lateinit var permissionsViewState: ViewState
    private lateinit var summary: TextView
    private lateinit var acceptPermissions: Button

    override val isSearchable: Boolean
        get() = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        permissionsViewState = binding!!.permissionsViewState
        summary = binding!!.summary
        acceptPermissions = binding!!.acceptPermissions
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })
        acceptPermissions.apply {
            setOnClickListener { _ ->
                requestPermissions(permissionsToGrant, permissionsRequestCode)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        summary.apply {
            requestFocus()
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> {
            requireActivity().finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            permissionsRequestCode -> if (grantResults.isNotEmpty()) {
                var areGranted: Boolean = true
                for (result in grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        areGranted = false
                        break
                    }
                }
                if (areGranted) {
                    requireActivity().finish()
                }
            }
        }
    }
}
