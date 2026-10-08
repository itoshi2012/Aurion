package com.aura.app.core

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aura.app.brain.PlaceholderBrain
import com.aura.app.logs.AuraLog
import com.aura.app.voice.AuraLanguage
import com.aura.app.voice.SpeechInput
import com.aura.app.voice.SpeechOutput

/**
 * The coordinator. It connects listening -> thinking -> speaking
 * and owns the STOP switch.
 */
class AuraController(context: Context) {

    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())

    var orbState by mutableStateOf(OrbState.IDLE)
        private set

    var statusText by mutableStateOf("Tap the orb and speak")
        private set

    var transcript by mutableStateOf("")
        private set

    var response by mutableStateOf("")
        private set

    var language by mutableStateOf(AuraLanguage.BENGALI)
        private set

    private val speechOutput = SpeechOutput(appContext)

    private val speechInput = SpeechInput(appContext, object : SpeechInput.Listener {
        override fun onPartialText(text: String) {
            if (orbState == OrbState.LISTENING) transcript = text
        }

        override fun onFinalText(text: String) {
            if (orbState == OrbState.LISTENING) {
                transcript = text
                respondTo(text)
            }
        }

        override fun onError(message: String) {
            if (orbState == OrbState.LISTENING) {
                orbState = OrbState.IDLE
                statusText = message
            }
        }
    })

    fun setLanguage(newLanguage: AuraLanguage) {
        if (orbState == OrbState.IDLE) {
            language = newLanguage
            statusText = "Tap the orb and speak"
        }
    }

    /** Called when the user taps the orb (microphone permission already granted). */
    fun onOrbTap() {
        when (orbState) {
            OrbState.IDLE -> startListening()
            OrbState.LISTENING -> speechInput.finish()
            else -> statusText = "Press STOP to interrupt"
        }
    }

    fun onPermissionDenied() {
        orbState = OrbState.IDLE
        statusText = "Microphone permission denied. Enable it: Settings > Apps > AURA > Permissions."
    }

    private fun startListening() {
        if (!speechInput.isAvailable()) {
            statusText = "Speech recognition is not available on this phone."
            AuraLog.e("SpeechRecognizer not available")
            return
        }
        transcript = ""
        response = ""
        orbState = OrbState.LISTENING
        statusText = "Listening..."
        AuraLog.d("Start listening (${language.tag})")
        speechInput.start(language.tag)
    }

    private fun respondTo(userText: String) {
        orbState = OrbState.THINKING
        statusText = "Thinking..."
        handler.postDelayed({
            val reply = PlaceholderBrain.reply(userText, language)
            response = reply
            orbState = OrbState.SPEAKING
            statusText = "Speaking..."
            speechOutput.speak(reply, language.tag, object : SpeechOutput.Listener {
                override fun onStarted() {}

                override fun onFinished() {
                    orbState = OrbState.IDLE
                    statusText = "Tap the orb and speak"
                }

                override fun onFailed(message: String) {
                    orbState = OrbState.IDLE
                    statusText = message
                }
            })
        }, 800)
    }

    /** The STOP switch: stops everything immediately. */
    fun stop() {
        AuraLog.d("STOP pressed")
        handler.removeCallbacksAndMessages(null)
        speechInput.stop()
        speechOutput.stop()
        orbState = OrbState.IDLE
        statusText = "Stopped. Tap the orb to speak."
    }

    fun release() {
        stop()
        speechOutput.release()
    }
}
