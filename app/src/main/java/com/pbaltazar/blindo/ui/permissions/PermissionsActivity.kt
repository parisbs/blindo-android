package com.pbaltazar.blindo.ui.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.pbaltazar.blindo.R
import com.wizeline.viewstate.State
import com.wizeline.viewstate.ViewState

class PermissionsActivity : AppCompatActivity() {

    private val permissionsRequestCode: Int = 1234
    private val permissionsToGrant: Array<String> = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private lateinit var permissionsViewState: ViewState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        permissionsViewState = findViewById(R.id.permissionsViewState)

        findViewById<Button>(R.id.continueBtn).apply {
            setOnClickListener {
                requestPermissions(permissionsToGrant, permissionsRequestCode)
            }
        }

        permissionsViewState.setState(State.CONTENT)
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
                    finish()
                }
            }
        }
    }
}
