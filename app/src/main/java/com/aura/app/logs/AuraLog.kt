package com.aura.app.logs

import android.util.Log

object AuraLog {
    private const val TAG = "AURA"

    fun d(message: String) {
        Log.d(TAG, message)
    }

    fun e(message: String, error: Throwable? = null) {
        Log.e(TAG, message, error)
    }
}
