package com.blindo.screenshotwatcher

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.internal.ChannelFlow
import java.lang.ref.WeakReference

class ScreenshotWatcherDelegate(
    private val contextReference: WeakReference<Context>,
    private val listener: ScreenshotWatcherListener
) {

    companion object {
        private const val TAG = "ScreenshotWatcher"
    }

    private var job: Job? = null

    constructor(
        context: Context,
        listener: ScreenshotWatcherListener
    ) : this(WeakReference(context), listener)

    @Suppress("unused")
    constructor(
        context: Context,
        onScreenCaptured: (uri: Uri) -> Unit
    ) : this(
        WeakReference(context),
        object : ScreenshotWatcherListener {
            override fun onScreenCaptured(uri: Uri) {
                onScreenCaptured(uri)
            }
        }
    )

    @FlowPreview
    fun startScreenshotWatcher() {
        job = CoroutineScope(Dispatchers.Main).launch {
            createContentObserverFlow()
                .debounce(500)
                .collectLatest { uri: Uri ->
                    contextReference.get()?.let { context ->
                        onContentChanged(context, uri)
                    }
                }
        }
        debugLog("Screenshot watching started on ${getPublicScreenshotDirectoryName()} path")
    }

    fun stopScreenshotWatcher() {
        job?.cancel()
        debugLog("Screenshot watching stoped")
    }

    fun createContentObserverFlow() = channelFlow {
        val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                uri?.let { _ ->
                    trySend(uri)
                }
            }
        }
        contextReference.get()
            ?.contentResolver
            ?.registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                true,
                contentObserver
            )
        awaitClose {
            contextReference.get()
                ?.contentResolver
                ?.unregisterContentObserver(contentObserver)
        }
    }

    private fun onContentChanged(context: Context, uri: Uri) {
        if (isReadExternalStoragePermissionGranted()) {
            val path = getFilePathFromContentResolver(context, uri)

            path?.let { p ->
                if (isScreenshotPath(p)) {
                    debugLog("Screenshot detected: $p")
                    onScreenCaptured(uri)
                }
            }
        } else {
            Log.w(TAG, "Read external storage permission required")
        }
    }

    private fun onScreenCaptured(uri: Uri) {
        listener.onScreenCaptured(uri)
    }

    private fun isScreenshotPath(path: String?): Boolean {
        val lowercasePath = path?.lowercase()
        val screenshotDirectory = getPublicScreenshotDirectoryName().lowercase()
        return lowercasePath?.contains(screenshotDirectory) == true ||
            lowercasePath?.contains("screenshot") == true
    }

    private fun getPublicScreenshotDirectoryName(): String =
        Environment.DIRECTORY_SCREENSHOTS

    private fun getFilePathFromContentResolver(context: Context, uri: Uri): String? {
        try {
            context.contentResolver.query(
                uri,
                arrayOf(
                    MediaStore.Images.Media.DATA
                ),
                null,
                null,
                null
            )?.let { cursor ->
                cursor.moveToFirst()
                val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
                cursor.close()
                return path
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message ?: "")
        }
        return null
    }

    private fun isReadExternalStoragePermissionGranted(): Boolean {
        return contextReference.get()?.let { context ->
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        } ?: run {
            false
        }
    }

    private fun debugLog(message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, message)
        }
    }

    interface ScreenshotWatcherListener {
        fun onScreenCaptured(uri: Uri)
    }
}
