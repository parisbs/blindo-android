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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.pbaltazar.blindo.R
import com.pbaltazar.blindo.databinding.FragmentPermissionsBinding
import com.pbaltazar.blindo.utils.core.ui.BlindoFragment
import com.wizeline.viewstate.ViewState
import org.koin.androidx.viewmodel.ext.android.viewModel

class PermissionsFragment : BlindoFragment<FragmentPermissionsBinding>() {

    private val permissionsViewModel: PermissionsViewModel by viewModel()
    private val permissionsFragmentArgs: PermissionsFragmentArgs by navArgs()

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            goToNextPermission()
        } else {
            summary.apply {
                requestFocus()
                sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
            }
        }
    }

    private lateinit var permissionsViewState: ViewState
    private lateinit var summary: TextView
    private lateinit var denyPermissions: Button
    private lateinit var acceptPermissions: Button

    private val expectedPermissions: List<String> = listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override val isSearchable: Boolean
        get() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionsFragmentArgs.permissions.split(",").forEach { permission ->
            if (expectedPermissions.contains(permission)) {
                permissionsViewModel.permissionsToGrant.add(Permission(
                    name = permission,
                    description = getPermissionDescription(permission)
                ))
            } else throw IllegalArgumentException("Invalid permission to request.")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        permissionsViewState = binding!!.permissionsViewState
        summary = binding!!.summary
        denyPermissions = binding!!.denyPermissions
        acceptPermissions = binding!!.acceptPermissions
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        subscribeCurrentPermission()
        goToNextPermission()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        android.R.id.home -> goBack()
        else -> super.onOptionsItemSelected(item)
    }

    private fun subscribeCurrentPermission() = permissionsViewModel.currentPermission.observe(viewLifecycleOwner) {
        if (it >= 0 && it < permissionsViewModel.permissionsToGrant.size) {
            val permission = permissionsViewModel.permissionsToGrant[it]
            if (ContextCompat.checkSelfPermission(requireContext(), permission.name) == PackageManager.PERMISSION_GRANTED) {
                goToNextPermission()
            } else setPermissionInfo(permission)
        } else goBack()
    }

    private fun setPermissionInfo(permission: Permission) {
        summary.apply {
            text = permission.description
            requestFocus()
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }
        acceptPermissions.setOnClickListener {
            requestPermissionLauncher.launch(permission.name)
        }
    }

    private fun goToNextPermission() =
        permissionsViewModel.setCurrentPermission(permissionsViewModel.currentPermission.value?.plus(1) ?: 0)

    private fun goBack(): Boolean = if (permissionsFragmentArgs.shouldReturnToApp.not()) {
        requireActivity().finish()
        true
    } else findNavController().popBackStack()

    private fun setupUi() {
        if (permissionsFragmentArgs.shouldReturnToApp.not()) {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
        }
        denyPermissions.setOnClickListener {
            goToNextPermission()
        }
    }

    private fun getPermissionDescription(permission: String): String = when (permission) {
        Manifest.permission.READ_EXTERNAL_STORAGE -> getString(R.string.permissions__read_external_storage_description)
        else -> throw IllegalArgumentException("Invalid permission to request.")
    }
}
