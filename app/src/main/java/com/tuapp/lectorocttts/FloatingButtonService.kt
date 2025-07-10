package com.tuapp.lectorocttts

import android.app.*
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.ImageView
import androidx.core.app.NotificationCompat

class FloatingButtonService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null

    private lateinit var ocrProcessor: OCRProcessor
    private lateinit var ttsManager: TTSManager

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null

    override fun onCreate() {
        super.onCreate()
        mediaProjection = MainActivity.mediaProjection

        ocrProcessor = OCRProcessor(this)
        ttsManager = TTSManager(this)

        startForegroundServiceWithNotification()
        showFloatingButton()
    }

    override fun onDestroy() {
        super.onDestroy()
        imageReader?.close()
        virtualDisplay?.release()
        mediaProjection?.stop()
        floatingView?.let { windowManager.removeView(it) }

        ttsManager.shutdown() // ← Añadido para liberar TextToSpeech
    }

    private fun startForegroundServiceWithNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "FloatingButtonServiceChannel"
            val channel = NotificationChannel(
                channelId,
                "Floating Button Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Lector OCR TTS")
                .setContentText("Servicio en ejecución")
                .setSmallIcon(R.drawable.ic_notification)
                .build()

            startForeground(1, notification)
        }
    }

    private fun showFloatingButton() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_button_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(floatingView, params)

        val button = floatingView!!.findViewById<ImageView>(R.id.floating_button)
        button.setOnClickListener {
            captureScreen()
        }
    }

    private fun captureScreen() {
        val mediaProjection = this.mediaProjection ?: return

        val metrics = resources.displayMetrics
        val density = metrics.densityDpi
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        imageReader?.close()
        virtualDisplay?.release()

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)

        virtualDisplay = mediaProjection.createVirtualDisplay(
            "ScreenCapture",
            width,
            height,
            density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )

        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()
            image?.let {
                val planes = it.planes
                val buffer = planes[0].buffer
                val pixelStride = planes[0].pixelStride
                val rowStride = planes[0].rowStride
                val rowPadding = rowStride - pixelStride * width

                val bitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(buffer)
                it.close()

                ocrProcessor.processImage(bitmap) { text ->
                    ttsManager.speak(text)
                    bitmap.recycle() // ← Liberación de memoria añadida aquí
                }
            }
        }, null)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
