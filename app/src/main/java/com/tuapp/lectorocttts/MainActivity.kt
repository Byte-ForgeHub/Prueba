package com.tuapp.lectorocttts

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Toast

class MainActivity : Activity() {

    companion object {
        const val REQUEST_CODE_CAPTURE = 1001
        var mediaProjection: MediaProjection? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mpm.createScreenCaptureIntent(), REQUEST_CODE_CAPTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CAPTURE) {
            if (resultCode == RESULT_OK && data != null) {
                val mpm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                mediaProjection = mpm.getMediaProjection(resultCode, data)

                val serviceIntent = Intent(this, FloatingButtonService::class.java)
                startForegroundService(serviceIntent)
                finish()
            } else {
                Toast.makeText(this, "Permiso de captura de pantalla denegado", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
