package com.pbaltazar.blindo.utils.updates

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.pbaltazar.blindo.utils.log.BlindoLogger

object UpdateManager {

    private lateinit var activity: Activity
    private lateinit var appUpdateManager: AppUpdateManager
    private var isInitialized: Boolean = false

    const val UPDATE_CHECKER_CODE = 8738
    val UPDATE_FAILED = ActivityResult.RESULT_IN_APP_UPDATE_FAILED

    fun initialize(activity: Activity) {
        this.activity = activity
        appUpdateManager = AppUpdateManagerFactory.create(this.activity)
        isInitialized = true
    }

    fun isInitialized(): Boolean = isInitialized && this::activity.isInitialized

    private fun verifyIsInitialized() {
        if (isInitialized().not()) {
            throw RuntimeException("Update manager is not initialized")
        }
    }

    fun checkForUpdates() {
        try {
            verifyIsInitialized()
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                processAppUpdateInfo(appUpdateInfo)
            }.addOnFailureListener { exception ->
                FirebaseCrashlytics.getInstance().recordException(exception)
                BlindoLogger.log.e(exception)
            }
        } catch (e: Exception) {
            BlindoLogger.log.e(e)
        }
    }

    private fun processAppUpdateInfo(appUpdateInfo: AppUpdateInfo) {
        try {
            verifyIsInitialized()
            when (appUpdateInfo.updateAvailability()) {
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    BlindoLogger.log.i("Update available: version ${appUpdateInfo.availableVersionCode()}")
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        startUpdateFlow(appUpdateInfo, AppUpdateType.IMMEDIATE)
                    }
                }
                UpdateAvailability.UPDATE_NOT_AVAILABLE -> BlindoLogger.log.i("Last version installed")
                UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS -> startUpdateFlow(appUpdateInfo, AppUpdateType.IMMEDIATE)
                else -> throw RuntimeException("Unknown update state")
            }
        } catch (e: Exception) {
            BlindoLogger.log.e(e)
        }
    }

    private fun startUpdateFlow(appUpdateInfo: AppUpdateInfo, appUpdateType: Int) {
        try {
            verifyIsInitialized()
            appUpdateManager.startUpdateFlowForResult(
                appUpdateInfo,
                appUpdateType,
                activity,
                UPDATE_CHECKER_CODE
            )
        } catch (e: Exception) {
            BlindoLogger.log.e(e)
        }
    }
}
