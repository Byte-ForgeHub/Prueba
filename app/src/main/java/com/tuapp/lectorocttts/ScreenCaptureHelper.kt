package com.tuapp.lectorocttts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.DisplayMetrics
import android.view.WindowManager

class ScreenCaptureHelper(private val activity: Activity) {

    companion object {
        const val REQUEST_CODE_CAPTURE = 1001
    }

    private val projectionManager = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null

    fun startProjectionRequest() {
        val intent = projectionManager.createScreenCaptureIntent()
        activity.startActivityForResult(intent, REQUEST_CODE_CAPTURE)
    }

    fun onPermissionResult(resultCode: Int, data: Intent) {
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        createVirtualDisplay()
    }

    private fun createVirtualDisplay() {
        val windowManager = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)

        // Aquí se implementará la captura real de pantalla en pasos siguientes
    }

    fun stopProjection() {
        virtualDisplay?.release()
        mediaProjection?.stop()
    }
}