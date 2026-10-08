package com.aura.app.voice

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

/** Wraps Android's built-in Text-to-Speech. Must be used from the main thread. */
class SpeechOutput(context: Context) {

    interface Listener {
        fun onStarted()
        fun onFinished()
        fun onFailed(message: String)
    }

    private val handler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var ready = false
    private var currentId: String? = null
    private var currentListener: Listener? = null

    /** If the voice never starts, give up instead of leaving the orb stuck. */
    private val startTimeout = Runnable {
        val l = currentListener
        if (l != null) {
            currentListener = null
            currentId = null
            tts?.stop()
            l.onFailed("Voice did not start. Showing text only.")
        }
    }

    private fun complete(utteranceId: String?, error: String?) {
        handler.post {
            if (utteranceId == null || utteranceId != currentId) return@post
            handler.removeCallbacks(startTimeout)
            val l = currentListener
            currentListener = null
            currentId = null
            if (error == null) l?.onFinished() else l?.onFailed(error)
        }
    }

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            handler.post {
                if (utteranceId != null && utteranceId == currentId) {
                    handler.removeCallbacks(startTimeout)
                    currentListener?.onStarted()
                }
            }
        }

        override fun onDone(utteranceId: String?) {
            complete(utteranceId, null)
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            complete(utteranceId, "Voice output failed.")
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            complete(utteranceId, "Voice output failed.")
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {}
    }

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) tts?.setOnUtteranceProgressListener(progressListener)
        }
    }

    fun speak(text: String, languageTag: String, listener: Listener) {
        val engine = tts
        if (!ready || engine == null) {
            listener.onFailed("Voice engine is not ready. Showing text only.")
            return
        }
        if (!applyLanguage(engine, languageTag)) {
            listener.onFailed("Voice for this language is not installed. Showing text only.")
            return
        }
        // A slightly lower pitch gives a deeper tone. True male-voice selection comes later.
        engine.setPitch(0.85f)
        engine.setSpeechRate(1.0f)

        val id = UUID.randomUUID().toString()
        currentId = id
        currentListener = listener
        val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
        if (result == TextToSpeech.ERROR) {
            currentId = null
            currentListener = null
            listener.onFailed("Voice output failed.")
            return
        }
        handler.removeCallbacks(startTimeout)
        handler.postDelayed(startTimeout, 10_000)
    }

    /** Immediate stop. Used by the STOP button. */
    fun stop() {
        handler.removeCallbacks(startTimeout)
        currentListener = null
        currentId = null
        tts?.stop()
    }

    fun release() {
        stop()
        tts?.shutdown()
        tts = null
        ready = false
    }

    private fun applyLanguage(engine: TextToSpeech, languageTag: String): Boolean {
        val wanted = Locale.forLanguageTag(languageTag)
        val candidates = mutableListOf(wanted, Locale.forLanguageTag(wanted.language))
        if (wanted.language == "bn") candidates.add(Locale.forLanguageTag("bn-IN"))
        for (locale in candidates) {
            val r = engine.setLanguage(locale)
            if (r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED) {
                return true
            }
        }
        return false
    }
}
