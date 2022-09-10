package com.pbaltazar.blindo.ui.permissions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PermissionsViewModel : ViewModel() {

    val permissionsToGrant: MutableList<Permission> = mutableListOf()

    private val _currentPermission = MutableLiveData<Int>()
    val currentPermission: LiveData<Int> get() = _currentPermission

    fun setCurrentPermission(permissionIndex: Int) {
        _currentPermission.postValue(permissionIndex)
    }
}
