package com.example.accessibilityoverlaybubble_xml.data

import android.content.Context

class PreferencesManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "assistant_prefs"
        private const val KEY_USER_TONE = "user_tone"
        private const val KEY_ARTEFACT_ID = "artefact_id"
    }

    private val sharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUserTone(tone: UserTone) {
        sharedPreferences.edit()
            .putString(KEY_USER_TONE, tone.name)
            .apply()
    }

    fun getUserTone(): UserTone {
        val savedTone = sharedPreferences.getString(KEY_USER_TONE, UserTone.CASUAL.name)
        return try {
            UserTone.valueOf(savedTone ?: UserTone.CASUAL.name)
        } catch (e: IllegalArgumentException) {
            UserTone.CASUAL
        }
    }

    fun saveArtefactId(artefactId: String) {
        sharedPreferences.edit()
            .putString(KEY_ARTEFACT_ID, artefactId.trim())
            .apply()
    }

    fun getArtefactId(): String {
        return sharedPreferences.getString(KEY_ARTEFACT_ID, "").orEmpty()
    }
}