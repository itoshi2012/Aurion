package com.aura.app.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.aura.app.logs.AuraLog

/** Wraps Android's built-in SpeechRecognizer. Must be used from the main thread. */
class SpeechInput(
    private val context: Context,
    private val listener: Listener
) {
    interface Listener {
        fun onPartialText(text: String)
        fun onFinalText(text: String)
        fun onError(message: String)
    }

    private val handler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun start(languageTag: String) {
        stop()

        val r: SpeechRecognizer = try {
            SpeechRecognizer.createSpeechRecognizer(context)
        } catch (e: Exception) {
            AuraLog.e("createSpeechRecognizer failed", e)
            listener.onError("Could not start speech recognition on this phone.")
            return
        }
        recognizer = r

        r.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onEvent(eventType: Int, params: Bundle?) {}

            override fun onError(error: Int) {
                if (recognizer !== r) return
                AuraLog.d("Speech error code: $error")
                dispose(r)
                listener.onError(describe(error))
            }

            override fun onResults(results: Bundle?) {
                if (recognizer !== r) return
                val text = firstResult(results)
                dispose(r)
                if (text.isNullOrBlank()) {
                    listener.onError("I did not catch that. Tap the orb and try again.")
                } else {
                    listener.onFinalText(text)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                if (recognizer !== r) return
                val text = firstResult(partialResults)
                if (!text.isNullOrBlank()) listener.onPartialText(text)
            }
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        try {
            r.startListening(intent)
        } catch (e: Exception) {
            AuraLog.e("startListening failed", e)
            dispose(r)
            listener.onError("Could not start listening. Please try again.")
        }
    }

    /** User tapped again: stop recording and deliver what was heard so far. */
    fun finish() {
        try {
            recognizer?.stopListening()
        } catch (e: Exception) {
            AuraLog.e("stopListening failed", e)
        }
    }

    /** Immediate cancel. Used by the STOP button. */
    fun stop() {
        val r = recognizer ?: return
        recognizer = null
        try {
            r.cancel()
        } catch (e: Exception) {
            AuraLog.e("cancel failed", e)
        }
        try {
            r.destroy()
        } catch (e: Exception) {
            AuraLog.e("destroy failed", e)
        }
    }

    private fun dispose(r: SpeechRecognizer) {
        if (recognizer === r) recognizer = null
        handler.post {
            try {
                r.destroy()
            } catch (e: Exception) {
                AuraLog.e("destroy failed", e)
            }
        }
    }

    private fun firstResult(bundle: Bundle?): String? =
        bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()

    private fun describe(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_NO_MATCH,
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
            "I did not hear anything. Tap the orb and try again."
        SpeechRecognizer.ERROR_AUDIO ->
            "Microphone problem. Please try again."
        SpeechRecognizer.ERROR_CLIENT ->
            "Listening was interrupted. Tap the orb and try again."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
            "Microphone permission is missing."
        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
        SpeechRecognizer.ERROR_SERVER ->
            "Speech recognition needs internet for this language right now."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
            "Speech recognizer is busy. Try again in a moment."
        SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
        SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE ->
            "This language is not available for speech recognition on your phone."
        else ->
            "Speech recognition error ($error). Please try again."
    }
}
