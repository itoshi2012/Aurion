package com.aura.app.brain

import com.aura.app.voice.AuraLanguage

/** Phase 1 only: a fixed reply. No AI, no internet. Real brain comes in Phase 2. */
object PlaceholderBrain {
    fun reply(userText: String, language: AuraLanguage): String = when (language) {
        AuraLanguage.BENGALI ->
            "আপনি বলেছেন: $userText। আমি আউরা। এখন আমি শুধু শুনতে আর বলতে পারি। আমার চিন্তাশক্তি পরের ধাপে যোগ হবে।"
        AuraLanguage.ENGLISH ->
            "You said: $userText. I am AURA. Right now I can only listen and speak. My thinking power will be added in the next phase."
    }
}
