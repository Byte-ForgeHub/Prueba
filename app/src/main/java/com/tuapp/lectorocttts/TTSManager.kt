package com.tuapp.lectorocttts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

class TTSManager(context: Context) {

    private var tts: TextToSpeech = TextToSpeech(context) { status ->
        isReady = status == TextToSpeech.SUCCESS
    }

    var isReady = false

    fun speak(text: String) {
        if (isReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    fun setLanguage(locale: Locale): Boolean {
        return if (isReady) {
            tts.setLanguage(locale) == TextToSpeech.LANG_AVAILABLE
        } else false
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
