package com.tuapp.lectorocttts

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class OCRProcessor(private val context: Context) {

    fun processImage(bitmap: Bitmap, onResult: (String) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val resultText = visionText.text
                Log.d("OCRProcessor", "Texto reconocido: $resultText")
                onResult(resultText)
            }
            .addOnFailureListener { e ->
                Log.e("OCRProcessor", "Error en OCR", e)
                onResult("")
            }
    }
}